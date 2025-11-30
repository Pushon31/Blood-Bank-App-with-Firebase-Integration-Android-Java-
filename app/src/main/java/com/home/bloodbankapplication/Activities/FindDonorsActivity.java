package com.home.bloodbankapplication.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.home.bloodbankapplication.Adapters.DonorAdapter;
import com.home.bloodbankapplication.DataModels.DonorDataModel;
import com.home.bloodbankapplication.R;

import java.util.ArrayList;
import java.util.List;

public class FindDonorsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Spinner bloodTypeSpinner;
    private TextInputEditText locationSearch;
    private MaterialButton searchButton;
    private RecyclerView donorsRecyclerView;
    private View emptyState;

    private DonorAdapter donorAdapter;
    private List<DonorDataModel> donorList;
    private List<DonorDataModel> allDonorsList;

    private FirebaseFirestore firestore;

    private static final String TAG = "FindDonorsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_donors);

        initializeViews();
        setupToolbar();
        setupSpinner();
        setupRecyclerView();
        setupClickListeners();

        firestore = FirebaseFirestore.getInstance();

        // Load all donors initially
        loadAllDonors();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        bloodTypeSpinner = findViewById(R.id.blood_type_spinner);
        locationSearch = findViewById(R.id.location_search);
        searchButton = findViewById(R.id.search_button);
        donorsRecyclerView = findViewById(R.id.donors_recycler_view);
        emptyState = findViewById(R.id.empty_state);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Find Blood Donors");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupSpinner() {
        // Create blood type options
        String[] bloodTypes = {
                "All Blood Types",
                "A+", "A-",
                "B+", "B-",
                "AB+", "AB-",
                "O+", "O-"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                bloodTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodTypeSpinner.setAdapter(adapter);
    }

    private void setupRecyclerView() {
        donorList = new ArrayList<>();
        allDonorsList = new ArrayList<>();

        donorAdapter = new DonorAdapter(this, donorList);
        donorsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        donorsRecyclerView.setHasFixedSize(true);
        donorsRecyclerView.setAdapter(donorAdapter);
    }

    private void setupClickListeners() {
        searchButton.setOnClickListener(v -> searchDonors());
    }

    private void loadAllDonors() {
        firestore.collection("donors")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allDonorsList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DonorDataModel donor = document.toObject(DonorDataModel.class);
                            donor.setUserId(document.getId());
                            allDonorsList.add(donor);
                        }

                        // Show all donors initially
                        donorList.clear();
                        donorList.addAll(allDonorsList);
                        donorAdapter.notifyDataSetChanged();
                        updateEmptyState();

                        Log.d(TAG, "Loaded " + allDonorsList.size() + " donors");
                    } else {
                        Log.e(TAG, "Error loading donors: ", task.getException());
                        Toast.makeText(this, "Failed to load donors", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void searchDonors() {
        String selectedBloodType = bloodTypeSpinner.getSelectedItem().toString();
        String locationQuery = locationSearch.getText().toString().trim().toLowerCase();

        donorList.clear();

        for (DonorDataModel donor : allDonorsList) {
            boolean matchesBloodType = selectedBloodType.equals("All Blood Types") ||
                    donor.getBloodGroup().equalsIgnoreCase(selectedBloodType);

            boolean matchesLocation = locationQuery.isEmpty() ||
                    (donor.getCity() != null &&
                            donor.getCity().toLowerCase().contains(locationQuery));

            if (matchesBloodType && matchesLocation) {
                donorList.add(donor);
            }
        }

        donorAdapter.notifyDataSetChanged();
        updateEmptyState();

        if (donorList.isEmpty()) {
            Toast.makeText(this, "No donors found matching your criteria", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Found " + donorList.size() + " donors", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateEmptyState() {
        if (donorList.isEmpty()) {
            donorsRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            donorsRecyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
}