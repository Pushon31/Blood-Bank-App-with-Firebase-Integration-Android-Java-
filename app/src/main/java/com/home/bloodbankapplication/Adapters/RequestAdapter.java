package com.home.bloodbankapplication.Adapters;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.home.bloodbankapplication.DataModels.RequestDataModel;
import com.home.bloodbankapplication.R;

import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private Context context;
    private List<RequestDataModel> requestList;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private static final int SMS_PERMISSION_REQUEST_CODE = 1001;

    public RequestAdapter(Context context, List<RequestDataModel> requestList) {
        this.context = context;
        this.requestList = requestList;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(context).inflate(R.layout.request_item_layout, parent, false);
            return new RequestViewHolder(view);
        } catch (Exception e) {
            Log.e("RequestAdapter", "Error inflating layout: " + e.getMessage());
            View view = new View(context);
            return new RequestViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        try {
            if (requestList == null || requestList.isEmpty()) {
                return;
            }

            RequestDataModel request = requestList.get(position);

            if (request != null) {
                // Set request data
                String message = request.getMessage() != null ? request.getMessage() : "No message available";
                String bloodType = request.getBloodType() != null ? request.getBloodType() : "Unknown";
                String location = request.getLocation() != null ? request.getLocation() : "Unknown location";
                String time = request.getTime() != null ? request.getTime() : "Recently";
                String contact = request.getContact() != null ? request.getContact() : "N/A";

                String fullText = message +
                        "\n\n🩸 Blood Type: " + bloodType +
                        "\n📍 Location: " + location +
                        "\n⏰ " + time +
                        "\n📞 " + contact;

                holder.messageText.setText(fullText);

                // Show delete button only for user's own requests
                String currentUserId = firebaseAuth.getCurrentUser() != null ?
                        firebaseAuth.getCurrentUser().getUid() : null;

                if (currentUserId != null && currentUserId.equals(request.getUserId())) {
                    holder.deleteButton.setVisibility(View.VISIBLE);
                } else {
                    holder.deleteButton.setVisibility(View.GONE);
                }
            }

            // Call button
            if (holder.callButton != null) {
                holder.callButton.setOnClickListener(v -> {
                    if (request != null && request.getContact() != null && !request.getContact().isEmpty()) {
                        // Create intent to dial the number
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + request.getContact()));
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Contact number not available", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // SMS button
            if (holder.smsButton != null) {
                holder.smsButton.setOnClickListener(v -> {
                    if (request != null && request.getContact() != null && !request.getContact().isEmpty()) {
                        showSMSDialog(request);
                    } else {
                        Toast.makeText(context, "Contact number not available", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Share button
            if (holder.shareButton != null) {
                holder.shareButton.setOnClickListener(v -> {
                    if (request != null) {
                        String shareText = "Blood Request: " + request.getMessage() +
                                "\nBlood Type: " + request.getBloodType() +
                                "\nLocation: " + request.getLocation() +
                                "\nContact: " + request.getContact();

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                        context.startActivity(Intent.createChooser(shareIntent, "Share Blood Request"));
                    }
                });
            }

            // Delete button
            if (holder.deleteButton != null) {
                holder.deleteButton.setOnClickListener(v -> {
                    showDeleteConfirmationDialog(request, position);
                });
            }

        } catch (Exception e) {
            Log.e("RequestAdapter", "Error in onBindViewHolder: " + e.getMessage());
        }
    }

    private void showSMSDialog(RequestDataModel request) {
        // Create a dialog with editable message
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Send Message to Donor");

        // Create layout for dialog
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_sms_message, null);
        builder.setView(dialogView);

        EditText messageEditText = dialogView.findViewById(R.id.sms_message_edittext);
        TextView previewTextView = dialogView.findViewById(R.id.sms_preview_text);

        // Set default message
        String defaultMessage = createDefaultSMSMessage(request);
        messageEditText.setText(defaultMessage);
        previewTextView.setText("To: " + request.getContact() + "\n\n" + defaultMessage);

        // Update preview when user types
        messageEditText.setOnClickListener(v -> {
            previewTextView.setText("To: " + request.getContact() + "\n\n" + messageEditText.getText().toString());
        });

        builder.setPositiveButton("Send SMS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String customMessage = messageEditText.getText().toString().trim();
                if (!customMessage.isEmpty()) {
                    sendDirectSMS(request, customMessage);
                } else {
                    Toast.makeText(context, "Please enter a message", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendDirectSMS(RequestDataModel request, String customMessage) {
        try {
            String phoneNumber = request.getContact();

            // Remove any non-digit characters from phone number (keep + for international)
            phoneNumber = phoneNumber.replaceAll("[^0-9+]", "");

            if (phoneNumber.isEmpty()) {
                Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check for SMS permission
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, request it
                requestSMSPermission(phoneNumber, customMessage);
            } else {
                // Permission already granted, send SMS
                sendSMSDirectly(phoneNumber, customMessage);
            }
        } catch (Exception e) {
            Log.e("RequestAdapter", "Error sending direct SMS: " + e.getMessage());
            Toast.makeText(context, "Error sending SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void requestSMSPermission(String phoneNumber, String message) {
        if (ActivityCompat.shouldShowRequestPermissionRationale((android.app.Activity) context,
                Manifest.permission.SEND_SMS)) {
            // Show explanation why we need permission
            new AlertDialog.Builder(context)
                    .setTitle("SMS Permission Required")
                    .setMessage("This app needs SMS permission to send text messages directly to blood request contacts.")
                    .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((android.app.Activity) context,
                                    new String[]{Manifest.permission.SEND_SMS},
                                    SMS_PERMISSION_REQUEST_CODE);
                            // Store the phone number and message for later
                            storePendingSMS(phoneNumber, message);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            // No explanation needed, request the permission
            ActivityCompat.requestPermissions((android.app.Activity) context,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
            // Store the phone number and message for later
            storePendingSMS(phoneNumber, message);
        }
    }

    // This method should be called from the Activity's onRequestPermissionsResult
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults, String phoneNumber, String message) {
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, send SMS
                sendSMSDirectly(phoneNumber, message);
            } else {
                // Permission denied
                Toast.makeText(context, "SMS permission denied. Cannot send message.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendSMSDirectly(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();

            // Check if message is too long and needs to be split
            if (message.length() > 160) {
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null);
                Toast.makeText(context, "Message sent (multiple parts)", Toast.LENGTH_SHORT).show();
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Toast.makeText(context, "Message sent successfully!", Toast.LENGTH_SHORT).show();
            }

            Log.d("RequestAdapter", "SMS sent to: " + phoneNumber);
            Log.d("RequestAdapter", "Message: " + message);

        } catch (Exception e) {
            Log.e("RequestAdapter", "Failed to send SMS: " + e.getMessage());
            Toast.makeText(context, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            // Fallback to intent method if direct SMS fails
            fallbackToIntentSMS(phoneNumber, message);
        }
    }

    private void fallbackToIntentSMS(String phoneNumber, String message) {
        try {
            // Fallback to intent method
            Uri smsUri = Uri.parse("smsto:" + phoneNumber);
            Intent smsIntent = new Intent(Intent.ACTION_VIEW, smsUri);
            smsIntent.putExtra("sms_body", message);

            if (smsIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(smsIntent);
                Toast.makeText(context, "Opening messaging app...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "No SMS app available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("RequestAdapter", "Fallback SMS also failed: " + e.getMessage());
        }
    }

    private String createDefaultSMSMessage(RequestDataModel request) {
        StringBuilder message = new StringBuilder();

        message.append("Hello, I saw your blood request on Blood Bank App.\n\n");

        if (request.getMessage() != null) {
            message.append("Your Request: ").append(request.getMessage()).append("\n\n");
        }

        if (request.getBloodType() != null) {
            message.append("Blood Type Needed: ").append(request.getBloodType()).append("\n");
        }

        if (request.getLocation() != null) {
            message.append("Location: ").append(request.getLocation()).append("\n");
        }

        message.append("\n");
        message.append("I'm interested to help with blood donation. Please let me know if you still need it.\n\n");
        message.append("Thank you!");

        return message.toString();
    }

    // Simple method to store pending SMS (you might want to use SharedPreferences or a better approach)
    private void storePendingSMS(String phoneNumber, String message) {
        // In a real app, you might want to store this in SharedPreferences
        // For now, we'll just log it
        Log.d("RequestAdapter", "Pending SMS - Phone: " + phoneNumber + ", Message: " + message);
    }

    private void showDeleteConfirmationDialog(RequestDataModel request, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Request");
        builder.setMessage("Are you sure you want to delete this blood request? This action cannot be undone.");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteRequest(request, position);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteRequest(RequestDataModel request, int position) {
        if (request.getRequestId() != null && !request.getRequestId().isEmpty()) {
            firestore.collection("BloodRequests")
                    .document(request.getRequestId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        // Remove from local list and update UI
                        requestList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, requestList.size());
                        Toast.makeText(context, "Request deleted successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to delete request: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(context, "Cannot delete request: Invalid request ID", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        ImageView requestImage;
        TextView messageText;
        ImageView callButton;
        ImageView smsButton;
        ImageView shareButton;
        ImageView deleteButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                requestImage = itemView.findViewById(R.id.request_image);
                messageText = itemView.findViewById(R.id.message_text);
                callButton = itemView.findViewById(R.id.call_button);
                smsButton = itemView.findViewById(R.id.sms_button);
                shareButton = itemView.findViewById(R.id.share_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
            } catch (Exception e) {
                Log.e("RequestViewHolder", "Error finding views: " + e.getMessage());
            }
        }
    }
}