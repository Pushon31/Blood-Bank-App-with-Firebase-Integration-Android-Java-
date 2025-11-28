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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.home.bloodbankapplication.DataModels.RequestDataModel;
import com.home.bloodbankapplication.R;

import java.util.UUID;

public class MakeRequestActivity extends AppCompatActivity {

    private EditText messageEt;
    private ImageView selectedImage;
    private Button chooseImageButton, postRequestButton;

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_request);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("BloodRequests");
        storageReference = FirebaseStorage.getInstance().getReference("RequestImages");

        // Initialize views
        messageEt = findViewById(R.id.message);
        selectedImage = findViewById(R.id.selected_image);
        chooseImageButton = findViewById(R.id.choose_image_button);
        postRequestButton = findViewById(R.id.post_request_button);

        // Choose image button
        chooseImageButton.setOnClickListener(v -> {
            openImageChooser();
        });

        // Post request button
        postRequestButton.setOnClickListener(v -> {
            postBloodRequest();
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            selectedImage.setImageURI(imageUri);
        }
    }

    private void postBloodRequest() {
        String message = messageEt.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, "Please write a message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            // Upload image first
            StorageReference fileReference = storageReference.child(UUID.randomUUID().toString());
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Image uploaded, now save request with image URL
                            saveRequestToDatabase(message, uri.toString());
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MakeRequestActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Save request without image
            saveRequestToDatabase(message, "");
        }
    }

    private void saveRequestToDatabase(String message, String imageUrl) {
        String requestId = databaseReference.push().getKey();
        RequestDataModel request = new RequestDataModel(imageUrl, message);

        if (requestId != null) {
            databaseReference.child(requestId).setValue(request)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(MakeRequestActivity.this, "Request posted successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MakeRequestActivity.this, MainActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MakeRequestActivity.this, "Failed to post request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}