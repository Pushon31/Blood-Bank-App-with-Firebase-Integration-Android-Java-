package com.home.bloodbankapplication.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.home.bloodbankapplication.DataModels.DonorDataModel;
import com.home.bloodbankapplication.R;

import java.util.List;

public class DonorAdapter extends RecyclerView.Adapter<DonorAdapter.DonorViewHolder> {

    private Context context;
    private List<DonorDataModel> donorList;

    public DonorAdapter(Context context, List<DonorDataModel> donorList) {
        this.context = context;
        this.donorList = donorList;
    }

    @NonNull
    @Override
    public DonorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_donor, parent, false);
        return new DonorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DonorViewHolder holder, int position) {
        DonorDataModel donor = donorList.get(position);

        if (donor != null) {
            // Set donor data
            holder.donorName.setText(donor.getName() != null ? donor.getName() : "Unknown Donor");
            holder.donorBloodType.setText(donor.getBloodGroup() != null ? donor.getBloodGroup() : "Unknown");
            holder.donorLocation.setText(donor.getCity() != null ? donor.getCity() : "Location not specified");
            holder.donorMobile.setText(donor.getMobile() != null ? donor.getMobile() : "N/A");

            // Call button
            holder.callButton.setOnClickListener(v -> {
                if (donor.getMobile() != null && !donor.getMobile().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + donor.getMobile()));
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show();
                }
            });

            // Message button
            holder.messageButton.setOnClickListener(v -> {
                if (donor.getMobile() != null && !donor.getMobile().isEmpty()) {
                    sendSMS(donor);
                } else {
                    Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void sendSMS(DonorDataModel donor) {
        try {
            String phoneNumber = donor.getMobile().replaceAll("[^0-9+]", "");
            String message = createSMSMessage(donor);

            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
            smsIntent.putExtra("sms_body", message);

            if (smsIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(smsIntent);
            } else {
                Toast.makeText(context, "No messaging app found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("DonorAdapter", "Error sending SMS: " + e.getMessage());
            Toast.makeText(context, "Error opening messaging app", Toast.LENGTH_SHORT).show();
        }
    }

    private String createSMSMessage(DonorDataModel donor) {
        return "Hello " + (donor.getName() != null ? donor.getName() : "") +
                ",\n\nI found your contact through Blood Bank App. " +
                "I need blood donation. Could you please help me?\n\n" +
                "Thank you!";
    }

    @Override
    public int getItemCount() {
        return donorList != null ? donorList.size() : 0;
    }

    public static class DonorViewHolder extends RecyclerView.ViewHolder {
        TextView donorName, donorBloodType, donorLocation, donorMobile;
        MaterialButton callButton, messageButton;

        public DonorViewHolder(@NonNull View itemView) {
            super(itemView);
            donorName = itemView.findViewById(R.id.donor_name);
            donorBloodType = itemView.findViewById(R.id.donor_blood_type);
            donorLocation = itemView.findViewById(R.id.donor_location);
            donorMobile = itemView.findViewById(R.id.donor_mobile);
            callButton = itemView.findViewById(R.id.call_donor_button);
            messageButton = itemView.findViewById(R.id.message_donor_button);
        }
    }
}