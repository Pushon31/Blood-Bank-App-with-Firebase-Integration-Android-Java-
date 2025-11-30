package com.home.bloodbankapplication.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.home.bloodbankapplication.DataModels.RequestDataModel;
import com.home.bloodbankapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MakeRequestActivity extends AppCompatActivity {

    private EditText messageEt;
    private TextInputEditText bloodTypeEt, locationEt, contactEt;
    private ImageView selectedImage;
    private Button chooseImageButton, postRequestButton;

    private StorageReference storageReference;
    private FirebaseFirestore firestore;

    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_request);

        storageReference = FirebaseStorage.getInstance().getReference("RequestImages");
        firestore = FirebaseFirestore.getInstance();

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        messageEt = findViewById(R.id.message);
        bloodTypeEt = findViewById(R.id.blood_type);
        locationEt = findViewById(R.id.location);
        contactEt = findViewById(R.id.contact);
        selectedImage = findViewById(R.id.selected_image);
        chooseImageButton = findViewById(R.id.choose_image_button);
        postRequestButton = findViewById(R.id.post_request_button);
    }

    private void setupClickListeners() {
        chooseImageButton.setOnClickListener(v -> openImageChooser());
        postRequestButton.setOnClickListener(v -> postBloodRequest());
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Request Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            selectedImage.setImageURI(imageUri);
            selectedImage.setBackground(null); // Remove placeholder background
        }
    }

    private void postBloodRequest() {
        String message = messageEt.getText().toString().trim();
        String bloodType = bloodTypeEt.getText().toString().trim();
        String location = locationEt.getText().toString().trim();
        String contact = contactEt.getText().toString().trim();

        if (validateInputs(message, bloodType, location, contact)) {
            return;
        }

        // Show loading state
        postRequestButton.setEnabled(false);
        postRequestButton.setText("Posting Request...");

        if (imageUri != null) {
            uploadImageAndSaveRequest(message, bloodType, location, contact);
        } else {
            saveRequestToFirestore(message, bloodType, location, contact, "");
        }
    }

    private boolean validateInputs(String message, String bloodType, String location, String contact) {
        if (message.isEmpty()) {
            Toast.makeText(this, "Please write a message", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (bloodType.isEmpty()) {
            Toast.makeText(this, "Please enter blood type", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (location.isEmpty()) {
            Toast.makeText(this, "Please enter location", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (contact.isEmpty()) {
            Toast.makeText(this, "Please enter contact information", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void uploadImageAndSaveRequest(String message, String bloodType, String location, String contact) {
        String randomID = UUID.randomUUID().toString();
        StorageReference fileRef = storageReference.child(randomID);

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveRequestToFirestore(message, bloodType, location, contact, imageUrl);
                        })
                        .addOnFailureListener(e -> {
                            handleUploadFailure("Failed to get image URL: " + e.getMessage());
                        })
                )
                .addOnFailureListener(e -> {
                    handleUploadFailure("Image upload failed: " + e.getMessage());
                });
    }

    private void saveRequestToFirestore(String message, String bloodType, String location, String contact, String imageUrl) {
        String currentTime = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());

        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        RequestDataModel request = new RequestDataModel();
        request.setMessage(message);
        request.setBloodType(bloodType);
        request.setLocation(location);
        request.setContact(contact);
        request.setTime(currentTime);
        request.setImageUrl(imageUrl);
        request.setTimestamp(System.currentTimeMillis());

        // Add user information
        if (currentUser != null) {
            request.setUserId(currentUser.getUid());
            request.setUserEmail(currentUser.getEmail());
        }

        firestore.collection("BloodRequests")
                .add(request)
                .addOnSuccessListener(docRef -> {
                    // Set the document ID as requestId
                    request.setRequestId(docRef.getId());
                    // Update the document with requestId
                    docRef.update("requestId", docRef.getId())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(MakeRequestActivity.this,
                                        "Request posted successfully!", Toast.LENGTH_SHORT).show();
                                resetForm();
                                startActivity(new Intent(MakeRequestActivity.this, MainActivity.class));
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    handleUploadFailure("Failed to post request: " + e.getMessage());
                });
    }

    private void handleUploadFailure(String errorMessage) {
        Toast.makeText(MakeRequestActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
        postRequestButton.setEnabled(true);
        postRequestButton.setText("Post Request");
    }

    private void resetForm() {
        messageEt.setText("");
        bloodTypeEt.setText("");
        locationEt.setText("");
        contactEt.setText("");
        selectedImage.setImageResource(R.drawable.ic_add_photo);
        imageUri = null;
        postRequestButton.setEnabled(true);
        postRequestButton.setText("Post Request");
    }
}