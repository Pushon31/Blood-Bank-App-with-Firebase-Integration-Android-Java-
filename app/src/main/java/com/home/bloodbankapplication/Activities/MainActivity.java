package com.home.bloodbankapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "Activity started");

        try {
            // Initialize views with null checks
            toolbar = findViewById(R.id.toolbar);
            pickLocation = findViewById(R.id.pick_location);
            recyclerView = findViewById(R.id.recyclerView);
            makeRequestButton = findViewById(R.id.make_request_button);

            // Setup toolbar
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Blood Bank");
            }

            // Setup RecyclerView with empty list first
            requestList = new ArrayList<>();
            requestAdapter = new RequestAdapter(this, requestList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(requestAdapter);

            // Make Request Button
            makeRequestButton.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, MakeRequestActivity.class));
            });

            // Location picker
            pickLocation.setOnClickListener(v -> {
                showMessage("Location feature coming soon!");
            });

            Log.d("MainActivity", "All views initialized successfully");

        } catch (Exception e) {
            Log.e("MainActivity", "Error in onCreate: " + e.getMessage());
            e.printStackTrace();
            showMessage("App error: " + e.getMessage());
        }
    }

    private void showMessage(String msg) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
    }
}