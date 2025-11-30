package com.home.bloodbankapplication.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.home.bloodbankapplication.Adapters.RequestAdapter;
import com.home.bloodbankapplication.DataModels.RequestDataModel;
import com.home.bloodbankapplication.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int SMS_PERMISSION_REQUEST_CODE = 1001;
    private String pendingPhoneNumber;
    private String pendingMessage;

    private Toolbar toolbar;
    private MaterialCardView locationCard;
    private RecyclerView recyclerView;
    private TextView makeRequestButton;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private RequestAdapter requestAdapter;
    private List<RequestDataModel> requestList;

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "Activity started");

        initializeViews();
        setupToolbar();
        setupDrawer();
        setupRecyclerView();
        setupClickListeners();

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        loadRequestsFromFirestore();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, send the pending SMS
                if (pendingPhoneNumber != null && pendingMessage != null) {
                    sendSMSDirectly(pendingPhoneNumber, pendingMessage);
                    // Clear pending data
                    pendingPhoneNumber = null;
                    pendingMessage = null;
                }
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendSMSDirectly(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();

            if (message.length() > 160) {
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
                Toast.makeText(this, "Message sent (multiple parts)", Toast.LENGTH_SHORT).show();
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(this, "Message sent successfully!", Toast.LENGTH_SHORT).show();
            }

            Log.d("MainActivity", "SMS sent to: " + phoneNumber);
            Log.d("MainActivity", "Message: " + message);

        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "SMS send error: " + e.getMessage());
        }
    }

    public void setPendingSMSData(String phoneNumber, String message) {
        this.pendingPhoneNumber = phoneNumber;
        this.pendingMessage = message;
    }



    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        locationCard = findViewById(R.id.location_card);
        recyclerView = findViewById(R.id.recyclerView);
        makeRequestButton = findViewById(R.id.make_request_button);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Blood Bank");
        }
    }

    private void setupDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setupRecyclerView() {
        requestList = new ArrayList<>();
        requestAdapter = new RequestAdapter(this, requestList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(requestAdapter);
    }

    private void setupClickListeners() {
        makeRequestButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MakeRequestActivity.class));
        });

        locationCard.setOnClickListener(v -> {
            showMessage("Location feature coming soon!");
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already on home, just close drawer
        }
        else if (id == R.id.nav_my_requests) {
            startActivity(new Intent(this, MyRequestsActivity.class));
        }
        else if (id == R.id.nav_donors) {
            startActivity(new Intent(this, FindDonorsActivity.class));
        }
        else if (id == R.id.nav_blood_banks) {
            startActivity(new Intent(this, BloodBanksActivity.class));
        }
        else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        }
        else if (id == R.id.nav_donation_history) {
            startActivity(new Intent(this, DonationHistoryActivity.class));
        }
        else if (id == R.id.nav_emergency_contacts) {
            startActivity(new Intent(this, EmergencyContactsActivity.class));
        }
        else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        }
        else if (id == R.id.nav_share) {
            shareApp();
        }
        else if (id == R.id.nav_logout) {
            logoutUser();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Blood Bank App");
            String shareMessage = "Download the Blood Bank App to help save lives and find blood donors in your area.\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logoutUser() {
        firebaseAuth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    private void loadRequestsFromFirestore() {
        firestore.collection("BloodRequests")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Sort by latest first
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        if (snapshots != null) {
                            requestList.clear();
                            for (QueryDocumentSnapshot doc : snapshots) {
                                RequestDataModel req = doc.toObject(RequestDataModel.class);
                                // Set the document ID as requestId
                                req.setRequestId(doc.getId());
                                requestList.add(req);
                            }
                            requestAdapter.notifyDataSetChanged();
                            Log.d(TAG, "Requests loaded: " + requestList.size());

                            if (requestList.isEmpty()) {
                                addTestData();
                            }
                        } else {
                            Log.d(TAG, "No requests found");
                            addTestData();
                        }
                    } else {
                        Log.e(TAG, "Error loading requests: ", task.getException());
                        showMessage("Failed to load requests: " + (task.getException() != null ? task.getException().getMessage() : ""));
                        addTestData();
                    }
                });
    }

    private void addTestData() {
        for (int i = 1; i <= 10; i++) {
            RequestDataModel testRequest = new RequestDataModel();
            testRequest.setMessage("Urgent need for blood donation " + i);
            testRequest.setBloodType("B+");
            testRequest.setLocation("Location " + i);
            testRequest.setTime("Just now");
            testRequest.setContact("0123456789");
            requestList.add(testRequest);
        }
        requestAdapter.notifyDataSetChanged();
        Log.d(TAG, "Test data added: " + requestList.size() + " items");
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}