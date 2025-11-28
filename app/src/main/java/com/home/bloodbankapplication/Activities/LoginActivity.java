package com.home.bloodbankapplication.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.home.bloodbankapplication.R;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEt, passwordEt;
    private Button loginButton;
    private TextView signUpText;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseReference = FirebaseDatabase.getInstance().getReference("Donors");

        usernameEt = findViewById(R.id.username);
        passwordEt = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        signUpText = findViewById(R.id.sign_up_text);

        loginButton.setOnClickListener(v -> {
            String mobile = usernameEt.getText().toString().trim();
            String password = passwordEt.getText().toString().trim();

            if (mobile.isEmpty() || password.isEmpty()) {
                showMessage("Please enter mobile number and password");
                return;
            }

            // Check donor in Firebase
            databaseReference.orderByChild("mobile").equalTo(mobile)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot donorSnapshot : snapshot.getChildren()) {
                                    String dbPassword = donorSnapshot.child("password").getValue(String.class);
                                    if (dbPassword != null && dbPassword.equals(password)) {
                                        // Login successful
                                        showMessage("Login successful!");
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                        return;
                                    }
                                }
                                showMessage("Invalid password");
                            } else {
                                showMessage("Donor not found");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            showMessage("Database error: " + error.getMessage());
                        }
                    });
        });

        // Sign up text click
        signUpText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        });
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}