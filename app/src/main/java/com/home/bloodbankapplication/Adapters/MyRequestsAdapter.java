package com.home.bloodbankapplication.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.home.bloodbankapplication.DataModels.RequestDataModel;
import com.home.bloodbankapplication.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyRequestsAdapter extends RecyclerView.Adapter<MyRequestsAdapter.MyRequestViewHolder> {

    private Context context;
    private List<RequestDataModel> requestsList;
    private RequestActionListener actionListener;

    public interface RequestActionListener {
        void onEditRequest(RequestDataModel request);
        void onDeleteRequest(RequestDataModel request, int position);
        void onRequestUpdated();
    }

    public MyRequestsAdapter(Context context, List<RequestDataModel> requestsList, RequestActionListener actionListener) {
        this.context = context;
        this.requestsList = requestsList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public MyRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_request, parent, false);
        return new MyRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRequestViewHolder holder, int position) {
        RequestDataModel request = requestsList.get(position);

        if (request != null) {
            // Set blood type
            holder.bloodTypeBadge.setText(request.getBloodType() != null ? request.getBloodType() : "Unknown");

            // Set status
            boolean isActive = isRequestActive(request);
            holder.statusBadge.setText(isActive ? "Active" : "Completed");
            holder.statusBadge.setBackgroundResource(isActive ?
                    R.drawable.status_active_badge : R.drawable.status_completed_badge);

            // Set time
            holder.requestTime.setText(formatTimestamp(request.getTimestamp()));

            // Set message
            holder.requestMessage.setText(request.getMessage() != null ?
                    request.getMessage() : "No message available");

            // Set location
            holder.requestLocation.setText(request.getLocation() != null ?
                    request.getLocation() : "Location not specified");

            // Set contact
            holder.requestContact.setText(request.getContact() != null ?
                    request.getContact() : "Contact not available");

            // Edit button
            holder.editButton.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onEditRequest(request);
                }
            });

            // Delete button
            holder.deleteButton.setOnClickListener(v -> {
                showDeleteConfirmationDialog(request, position);
            });
        }
    }

    private boolean isRequestActive(RequestDataModel request) {
        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        return request.getTimestamp() > sevenDaysAgo;
    }

    private String formatTimestamp(long timestamp) {
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return "Recently";
        }
    }

    private void showDeleteConfirmationDialog(RequestDataModel request, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Request");
        builder.setMessage("Are you sure you want to delete this blood request? This action cannot be undone.");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            if (actionListener != null) {
                actionListener.onDeleteRequest(request, position);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public int getItemCount() {
        return requestsList != null ? requestsList.size() : 0;
    }

    public static class MyRequestViewHolder extends RecyclerView.ViewHolder {
        TextView bloodTypeBadge, statusBadge, requestTime, requestMessage, requestLocation, requestContact;
        MaterialButton editButton, deleteButton;

        public MyRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            bloodTypeBadge = itemView.findViewById(R.id.blood_type_badge);
            statusBadge = itemView.findViewById(R.id.status_badge);
            requestTime = itemView.findViewById(R.id.request_time);
            requestMessage = itemView.findViewById(R.id.request_message);
            requestLocation = itemView.findViewById(R.id.request_location);
            requestContact = itemView.findViewById(R.id.request_contact);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}