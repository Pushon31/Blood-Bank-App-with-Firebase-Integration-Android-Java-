package com.home.bloodbankapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.home.bloodbankapplication.Adapters.RequestAdapter;
import com.home.bloodbankapplication.DataModels.RequestDataModel;
import com.home.bloodbankapplication.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView pickLocation;
    private RecyclerView recyclerView;
    private TextView makeRequestButton;

    private RequestAdapter requestAdapter;
    private List<RequestDataModel> requestList;

    private FirebaseFirestore firestore;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Activity started");

        toolbar = findViewById(R.id.toolbar);
        pickLocation = findViewById(R.id.pick_location);
        recyclerView = findViewById(R.id.recyclerView);
        makeRequestButton = findViewById(R.id.make_request_button);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Blood Bank");
        }

        requestList = new ArrayList<>();
        requestAdapter = new RequestAdapter(this, requestList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(requestAdapter);

        firestore = FirebaseFirestore.getInstance();

        makeRequestButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MakeRequestActivity.class));
        });

        pickLocation.setOnClickListener(v -> {
            showMessage("Location feature coming soon!");
        });

        loadRequestsFromFirestore();
    }

    private void loadRequestsFromFirestore() {
        // Adjust collection name if you used different
        firestore.collection("BloodRequests")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        if (snapshots != null) {
                            requestList.clear();
                            for (QueryDocumentSnapshot doc : snapshots) {
                                RequestDataModel req = doc.toObject(RequestDataModel.class);
                                // If you stored timestamp or id, you can also get them:
                                // e.g. req.setRequestId(doc.getId());
                                requestList.add(req);
                            }
                            requestAdapter.notifyDataSetChanged();
                            Log.d(TAG, "Requests loaded: " + requestList.size());
                        } else {
                            Log.d(TAG, "No requests found");
                        }
                    } else {
                        Log.e(TAG, "Error loading requests: ", task.getException());
                        showMessage("Failed to load requests: " + (task.getException() != null ? task.getException().getMessage() : ""));
                    }
                });
    }

    private void showMessage(String msg) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
    }
}
