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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.firestore.FirebaseFirestore;
import com.home.bloodbankapplication.DataModels.RequestDataModel;
import com.home.bloodbankapplication.R;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MakeRequestActivity extends AppCompatActivity {

    private EditText messageEt;
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

        messageEt = findViewById(R.id.message);
        selectedImage = findViewById(R.id.selected_image);
        chooseImageButton = findViewById(R.id.choose_image_button);
        postRequestButton = findViewById(R.id.post_request_button);

        chooseImageButton.setOnClickListener(v -> openImageChooser());

        postRequestButton.setOnClickListener(v -> postBloodRequest());
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
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
            String randomID = UUID.randomUUID().toString();
            StorageReference fileRef = storageReference.child(randomID);

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String imageUrl = uri.toString();
                                saveRequestToFirestore(message, imageUrl);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MakeRequestActivity.this,
                                        "Failed to get image URL: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            })
                    )
                    .addOnFailureListener(e -> {
                        Toast.makeText(MakeRequestActivity.this,
                                "Image upload failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            saveRequestToFirestore(message, "");
        }
    }

    private void saveRequestToFirestore(String message, String imageUrl) {
        // Create a map or model
        Map<String, Object> request = new HashMap<>();
        request.put("message", message);
        request.put("imageUrl", imageUrl);
        request.put("timestamp", System.currentTimeMillis());

        // Firestore collection "BloodRequests"
        firestore.collection("BloodRequests")
                .add(request)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(MakeRequestActivity.this,
                            "Request posted successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MakeRequestActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MakeRequestActivity.this,
                            "Failed to post request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
