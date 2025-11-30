package com.home.bloodbankapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.home.bloodbankapplication.Adapters.MyRequestsAdapter;
import com.home.bloodbankapplication.DataModels.RequestDataModel;
import com.home.bloodbankapplication.R;

import java.util.ArrayList;
import java.util.List;

public class MyRequestsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView activeRequestsCount, totalRequestsCount;
    private MaterialButton createNewButton;
    private RecyclerView requestsRecyclerView;
    private View emptyState, loadingProgress;

    private MyRequestsAdapter requestsAdapter;
    private List<RequestDataModel> requestsList;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private static final String TAG = "MyRequestsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadMyRequests();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        activeRequestsCount = findViewById(R.id.active_requests_count);
        totalRequestsCount = findViewById(R.id.total_requests_count);
        createNewButton = findViewById(R.id.create_new_button);
        requestsRecyclerView = findViewById(R.id.requests_recycler_view);
        emptyState = findViewById(R.id.empty_state);
        loadingProgress = findViewById(R.id.loading_progress);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Blood Requests");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        requestsList = new ArrayList<>();
        requestsAdapter = new MyRequestsAdapter(this, requestsList, new MyRequestsAdapter.RequestActionListener() {
            @Override
            public void onEditRequest(RequestDataModel request) {
                editRequest(request);
            }

            @Override
            public void onDeleteRequest(RequestDataModel request, int position) {
                deleteRequest(request, position);
            }

            @Override
            public void onRequestUpdated() {
                loadMyRequests(); // Refresh the list
            }
        });

        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        requestsRecyclerView.setHasFixedSize(true);
        requestsRecyclerView.setAdapter(requestsAdapter);
    }

    private void setupClickListeners() {
        createNewButton.setOnClickListener(v -> {
            startActivity(new Intent(MyRequestsActivity.this, MakeRequestActivity.class));
        });

        // Empty state create button
        emptyState.findViewById(R.id.create_new_button).setOnClickListener(v -> {
            startActivity(new Intent(MyRequestsActivity.this, MakeRequestActivity.class));
        });
    }

    private void loadMyRequests() {
        String currentUserId = firebaseAuth.getCurrentUser() != null ?
                firebaseAuth.getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(this, "Please login to view your requests", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadingProgress.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        requestsRecyclerView.setVisibility(View.GONE);

        firestore.collection("BloodRequests")
                .whereEqualTo("userId", currentUserId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    loadingProgress.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        requestsList.clear();
                        int activeCount = 0;

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            RequestDataModel request = document.toObject(RequestDataModel.class);
                            request.setRequestId(document.getId());
                            requestsList.add(request);

                            // Consider requests from last 7 days as active
                            if (isRequestActive(request)) {
                                activeCount++;
                            }
                        }

                        requestsAdapter.notifyDataSetChanged();
                        updateStats(activeCount, requestsList.size());
                        updateEmptyState();

                        Log.d(TAG, "Loaded " + requestsList.size() + " requests");
                    } else {
                        Log.e(TAG, "Error loading requests: ", task.getException());
                        Toast.makeText(this, "Failed to load requests", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isRequestActive(RequestDataModel request) {
        // Consider request active if it's from last 7 days
        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        return request.getTimestamp() > sevenDaysAgo;
    }

    private void updateStats(int activeCount, int totalCount) {
        activeRequestsCount.setText(String.valueOf(activeCount));
        totalRequestsCount.setText(String.valueOf(totalCount));
    }

    private void updateEmptyState() {
        if (requestsList.isEmpty()) {
            requestsRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            requestsRecyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private void editRequest(RequestDataModel request) {
        // For now, we'll show a message that edit feature is coming soon
        // In future, you can implement actual edit functionality
        Toast.makeText(this, "Edit feature coming soon!", Toast.LENGTH_SHORT).show();

        // Future implementation:
        // Intent intent = new Intent(this, EditRequestActivity.class);
        // intent.putExtra("request_id", request.getRequestId());
        // startActivity(intent);
    }

    private void deleteRequest(RequestDataModel request, int position) {
        if (request.getRequestId() != null) {
            firestore.collection("BloodRequests")
                    .document(request.getRequestId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        requestsList.remove(position);
                        requestsAdapter.notifyItemRemoved(position);
                        updateStatsAfterDeletion();
                        Toast.makeText(this, "Request deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to delete request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateStatsAfterDeletion() {
        int activeCount = 0;
        for (RequestDataModel request : requestsList) {
            if (isRequestActive(request)) {
                activeCount++;
            }
        }
        updateStats(activeCount, requestsList.size());
        updateEmptyState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from MakeRequestActivity
        loadMyRequests();
    }
}