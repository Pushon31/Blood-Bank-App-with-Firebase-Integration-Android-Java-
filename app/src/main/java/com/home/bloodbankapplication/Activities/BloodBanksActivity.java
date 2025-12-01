package com.home.bloodbankapplication.Activities;

import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.home.bloodbankapplication.Adapters.BloodBankAdapter;

import com.home.bloodbankapplication.DataModels.BloodBank;
import com.home.bloodbankapplication.R;
import java.util.ArrayList;
import java.util.List;

public class BloodBanksActivity extends AppCompatActivity {

    private RecyclerView bloodBanksRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private EditText searchEditText;

    private BloodBankAdapter bloodBankAdapter;
    private List<BloodBank> bloodBankList;
    private List<BloodBank> filteredBloodBankList;

    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_blood_banks);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupRecyclerView();
        getCurrentLocation();
        loadBloodBanksFromFirestore();
        setupSearchFunctionality();
    }

    private void initializeViews() {
        bloodBanksRecyclerView = findViewById(R.id.bloodBanksRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);
        searchEditText = findViewById(R.id.searchEditText);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        bloodBankList = new ArrayList<>();
        filteredBloodBankList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        bloodBankAdapter = new BloodBankAdapter(filteredBloodBankList);
        bloodBanksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bloodBanksRecyclerView.setAdapter(bloodBankAdapter);
    }

    private void getCurrentLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = location;
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void loadBloodBanksFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        db.collection("bloodBanks")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        bloodBankList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BloodBank bloodBank = document.toObject(BloodBank.class);
                            bloodBank.setId(document.getId());

                            // Calculate distance if location is available
                            if (currentLocation != null && bloodBank.getLatitude() != 0 && bloodBank.getLongitude() != 0) {
                                Location bankLocation = new Location("");
                                bankLocation.setLatitude(bloodBank.getLatitude());
                                bankLocation.setLongitude(bloodBank.getLongitude());
                                float distance = currentLocation.distanceTo(bankLocation) / 1000; // Convert to kilometers
                                // You can store this distance in the bloodBank object if needed
                            }

                            bloodBankList.add(bloodBank);
                        }

                        filteredBloodBankList.clear();
                        filteredBloodBankList.addAll(bloodBankList);
                        bloodBankAdapter.updateList(filteredBloodBankList);

                        if (filteredBloodBankList.isEmpty()) {
                            emptyStateText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        emptyStateText.setText("Error loading blood banks");
                        emptyStateText.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBloodBanks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterBloodBanks(String searchText) {
        filteredBloodBankList.clear();

        if (searchText.isEmpty()) {
            filteredBloodBankList.addAll(bloodBankList);
        } else {
            for (BloodBank bloodBank : bloodBankList) {
                if (bloodBank.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                        bloodBank.getAddress().toLowerCase().contains(searchText.toLowerCase()) ||
                        bloodBank.getBloodTypes().toLowerCase().contains(searchText.toLowerCase())) {
                    filteredBloodBankList.add(bloodBank);
                }
            }
        }

        bloodBankAdapter.updateList(filteredBloodBankList);

        if (filteredBloodBankList.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            emptyStateText.setVisibility(View.GONE);
        }
    }
}