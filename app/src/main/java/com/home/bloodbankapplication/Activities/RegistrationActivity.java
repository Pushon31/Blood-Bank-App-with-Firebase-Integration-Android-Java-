package com.home.bloodbankapplication.Activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.home.bloodbankapplication.R;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText nameEditText, cityEditText, bloodGroupEditText, passwordEditText, mobileNumberEditText;
    private Button submitButton;

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    // Constants for validation
    private static final int MOBILE_NUMBER_LENGTH = 11;
    private static final String TAG = "RegistrationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);




        initializeFirebaseServices();
        initializeViews();
        setupSubmitButton();
    }

    private void initializeFirebaseServices() {
        try {
            firebaseAuth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();
            databaseReference = FirebaseDatabase.getInstance().getReference("Donors");
            Log.d(TAG, "Firebase services initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization error: " + e.getMessage());
            showToast("Firebase initialization failed: " + e.getMessage());
        }
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.name);
        cityEditText = findViewById(R.id.city);
        bloodGroupEditText = findViewById(R.id.blood_group);
        passwordEditText = findViewById(R.id.password);
        mobileNumberEditText = findViewById(R.id.number);
        submitButton = findViewById(R.id.submit_button);

        if (areViewsInitialized()) {
            Log.d(TAG, "All views initialized successfully");
        } else {
            showToast("UI initialization failed - please restart the app");
            Log.e(TAG, "One or more views failed to initialize");
        }
    }

    private boolean areViewsInitialized() {
        return nameEditText != null && cityEditText != null &&
                bloodGroupEditText != null && passwordEditText != null &&
                mobileNumberEditText != null && submitButton != null;
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> {
            Log.d(TAG, "Submit button clicked");
            processRegistration();
        });
    }

    private void processRegistration() {
        String name = nameEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String bloodGroup = bloodGroupEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String mobileNumber = mobileNumberEditText.getText().toString().trim();

        Log.d(TAG, "Registration attempt - Name: " + name + ", Mobile: " + mobileNumber);

        if (!validateInputs(name, city, bloodGroup, password, mobileNumber)) {
            Log.d(TAG, "Registration Failed - Name: " + name + ", Mobile: " + mobileNumber);

            return;
        }

        registerDonor(name, city, bloodGroup, password, mobileNumber);
    }

    private boolean validateInputs(String name, String city, String bloodGroup, String password, String mobileNumber) {
        if (name.isEmpty() || city.isEmpty() || bloodGroup.isEmpty() || password.isEmpty() || mobileNumber.isEmpty()) {
            showToast("Please fill in all fields");
            return false;
        }

        if (mobileNumber.length() != MOBILE_NUMBER_LENGTH) {
            showToast("Mobile number must be " + MOBILE_NUMBER_LENGTH + " digits");
            return false;
        }

        return true;
    }

    private void registerDonor(String name, String city, String bloodGroup, String password, String mobileNumber) {
        HashMap<String, Object> donorData = createDonorData(name, city, bloodGroup, password, mobileNumber);
        Log.d(TAG, "Db instance------: " + mobileNumber);
        String fakeEmail = mobileNumber + "@myapp.com";

        firebaseAuth.createUserWithEmailAndPassword(fakeEmail, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Auth registration success
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();
                                saveDonorInFirestore(uid, name, city, bloodGroup, mobileNumber);
                            } else {
                                Log.e(TAG, "FirebaseUser is null after signup");
                                showToast("Registration failed: can't get user ID");
                            }
                        } else {
                            Log.e(TAG, "Auth signup failed: " + (task.getException() != null
                                    ? task.getException().getMessage() : ""));
                            showToast("Registration failed: " + (task.getException() != null
                                    ? task.getException().getMessage() : "Unknown error"));
                        }
                    }
                });
//        databaseReference.child(mobileNumber).setValue(donorData)
//                .addOnSuccessListener(aVoid -> {
//                    Log.d(TAG, "Donor data saved successfully for mobile: " + mobileNumber);
//                    showToast("Registration Successful!");
//                    clearFormFields();
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Failed to save donor data: " + e.getMessage());
//                    showToast("Registration Failed: " + e.getMessage());
//                });
    }
    private void saveDonorInFirestore(String uid,
                                      String name,
                                      String city,
                                      String bloodGroup,
                                      String mobileNumber) {
        Map<String, Object> donorData = new HashMap<>();
        donorData.put("name", name);
        donorData.put("city", city);
        donorData.put("bloodGroup", bloodGroup);
        donorData.put("mobile", mobileNumber);
        donorData.put("timestamp", System.currentTimeMillis());

        firestore.collection("donors")
                .document(uid)
                .set(donorData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Donor data saved successfully for UID: " + uid);
                    showToast("Registration Successful!");
                    clearFormFields();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save donor data: " + e.getMessage(), e);
                    showToast("Registration Failed: " + e.getMessage());
                });
    }

    private HashMap<String, Object> createDonorData(String name, String city, String bloodGroup, String password, String mobileNumber) {
        HashMap<String, Object> donorData = new HashMap<>();
        donorData.put("name", name);
        donorData.put("city", city);
        donorData.put("bloodGroup", bloodGroup);
        donorData.put("password", password);
        donorData.put("mobile", mobileNumber);
        donorData.put("timestamp", System.currentTimeMillis());

        return donorData;
    }

    private void clearFormFields() {
        nameEditText.setText("");
        cityEditText.setText("");
        bloodGroupEditText.setText("");
        passwordEditText.setText("");
        mobileNumberEditText.setText("");

        // Optional: Set focus back to first field
        nameEditText.requestFocus();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}