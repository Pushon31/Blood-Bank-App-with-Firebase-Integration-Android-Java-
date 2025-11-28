package com.home.bloodbankapplication.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.home.bloodbankapplication.R;

import java.util.HashMap;

public class RegistrationActivity extends AppCompatActivity {

    private EditText nameEt, cityEt, bloodGroupEt, passEt, numberEt;
    private Button submitButton;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Log.d("Registration", "Activity started");

        try {
            // Initialize Firebase
            databaseReference = FirebaseDatabase.getInstance().getReference("Donors");
            Log.d("Registration", "Firebase initialized");
        } catch (Exception e) {
            Log.e("Registration", "Firebase init error: " + e.getMessage());
            Toast.makeText(this, "Firebase error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        // Initialize views with null checks
        nameEt = findViewById(R.id.name);
        cityEt = findViewById(R.id.city);
        bloodGroupEt = findViewById(R.id.blood_group);
        passEt = findViewById(R.id.password);
        numberEt = findViewById(R.id.number);
        submitButton = findViewById(R.id.submit_button);

        if (nameEt == null || cityEt == null || bloodGroupEt == null || passEt == null || numberEt == null || submitButton == null) {
            Toast.makeText(this, "UI initialization failed", Toast.LENGTH_LONG).show();
            Log.e("Registration", "One or more views are null");
            return;
        }

        submitButton.setOnClickListener(v -> {
            Log.d("Registration", "Submit button clicked");

            String name = nameEt.getText().toString().trim();
            String city = cityEt.getText().toString().trim();
            String bloodGroup = bloodGroupEt.getText().toString().trim();
            String password = passEt.getText().toString().trim();
            String mobile = numberEt.getText().toString().trim();

            Log.d("Registration", "Data - Name: " + name + ", Mobile: " + mobile);

            // Validation
            if (name.isEmpty() || city.isEmpty() || bloodGroup.isEmpty() || password.isEmpty() || mobile.isEmpty()) {
                showMessage("All fields are required");
                return;
            }

            if (mobile.length() != 11) {
                showMessage("Mobile number must be 11 digits");
                return;
            }

            // Create data map
            HashMap<String, Object> donorData = new HashMap<>();
            donorData.put("name", name);
            donorData.put("city", city);
            donorData.put("bloodGroup", bloodGroup);
            donorData.put("password", password);
            donorData.put("mobile", mobile);

            // Save to Firebase
            databaseReference.child(mobile).setValue(donorData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Registration", "Data saved successfully");
                        showMessage("Registration Successful!");

                        // Clear fields
                        nameEt.setText("");
                        cityEt.setText("");
                        bloodGroupEt.setText("");
                        passEt.setText("");
                        numberEt.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Registration", "Firebase save error: " + e.getMessage());
                        showMessage("Registration Failed: " + e.getMessage());
                    });
        });
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}