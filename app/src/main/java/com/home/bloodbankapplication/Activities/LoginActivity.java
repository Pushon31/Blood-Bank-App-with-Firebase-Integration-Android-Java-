package com.home.bloodbankapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.home.bloodbankapplication.R;

public class LoginActivity extends AppCompatActivity {
    private EditText mobileEt, passwordEt;
    private Button loginButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mobileEt = findViewById(R.id.username);     // your EditText id
        passwordEt = findViewById(R.id.password);   // your EditText id
        loginButton = findViewById(R.id.login_button);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String mobile = mobileEt.getText().toString().trim();
        String password = passwordEt.getText().toString().trim();

        if (mobile.isEmpty() || password.isEmpty()) {
            showToast("Please enter mobile number and password");
            return;
        }
        if (mobile.length() != 11) {
            showToast("Mobile number must be 11 digits");
            return;
        }

        String fakeEmail = mobile + "@myapp.com";

        firebaseAuth.signInWithEmailAndPassword(fakeEmail, password)
                .addOnCompleteListener(this, new com.google.android.gms.tasks.OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                fetchDonorDataAndProceed(uid);
                            } else {
                                Log.e(TAG, "Login succeeded but user is null");
                                showToast("Login failed: unknown error");
                            }
                        } else {
                            Log.e(TAG, "Login failed: " + (task.getException() != null ? task.getException().getMessage() : ""));
                            showToast("Login failed: invalid credentials");
                        }
                    }
                });
    }

    private void fetchDonorDataAndProceed(String uid) {
        firestore.collection("donors")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // you got donor data — you can read it:
                        String name = documentSnapshot.getString("name");
                        String city = documentSnapshot.getString("city");
                        String bloodGroup = documentSnapshot.getString("bloodGroup");
                        String mobile = documentSnapshot.getString("mobile");
                        // ... other data as needed

                        Log.d(TAG, "Donor data fetched: " + name + ", " + city + ", " + bloodGroup);

                        showToast("Login successful!");
                        // pass data to MainActivity (or store in SharedPreferences etc.)
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        // optionally put extra data
                        intent.putExtra("name", name);
                        intent.putExtra("city", city);
                        intent.putExtra("bloodGroup", bloodGroup);
                        intent.putExtra("mobile", mobile);
                        startActivity(intent);
                        finish();

                    } else {
                        Log.e(TAG, "No donor document for uid: " + uid);
                        showToast("Login failed: User data not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch donor data: " + e.getMessage(), e);
                    showToast("Login failed: " + e.getMessage());
                });
    }

    private void showToast(String msg) {
        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
