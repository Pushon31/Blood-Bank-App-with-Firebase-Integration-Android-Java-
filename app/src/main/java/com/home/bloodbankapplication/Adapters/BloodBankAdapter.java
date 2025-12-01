package com.home.bloodbankapplication.Adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.home.bloodbankapplication.DataModels.BloodBank;
import com.home.bloodbankapplication.R;
import java.util.List;

public class BloodBankAdapter extends RecyclerView.Adapter<BloodBankAdapter.BloodBankViewHolder> {

    private List<BloodBank> bloodBankList;

    public BloodBankAdapter(List<BloodBank> bloodBankList) {
        this.bloodBankList = bloodBankList;
    }

    @NonNull
    @Override
    public BloodBankViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_bank, parent, false);
        return new BloodBankViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BloodBankViewHolder holder, int position) {
        BloodBank bloodBank = bloodBankList.get(position);

        holder.bloodBankName.setText(bloodBank.getName());
        holder.bloodBankAddress.setText(bloodBank.getAddress());
        holder.bloodBankContact.setText(bloodBank.getContact());

        if (bloodBank.isAvailable()) {
            holder.bloodBankAvailability.setText("Available");
            holder.bloodBankAvailability.setBackgroundColor(holder.itemView.getContext().getColor(android.R.color.holo_green_dark));
        } else {
            holder.bloodBankAvailability.setText("Closed");
            holder.bloodBankAvailability.setBackgroundColor(holder.itemView.getContext().getColor(android.R.color.holo_red_dark));
        }

        holder.callButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + bloodBank.getContact()));
            holder.itemView.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bloodBankList.size();
    }

    public void updateList(List<BloodBank> newList) {
        bloodBankList = newList;
        notifyDataSetChanged();
    }

    static class BloodBankViewHolder extends RecyclerView.ViewHolder {
        TextView bloodBankName, bloodBankAddress, bloodBankContact, bloodBankDistance, bloodBankAvailability;
        TextView callButton;

        public BloodBankViewHolder(@NonNull View itemView) {
            super(itemView);
            bloodBankName = itemView.findViewById(R.id.bloodBankName);
            bloodBankAddress = itemView.findViewById(R.id.bloodBankAddress);
            bloodBankContact = itemView.findViewById(R.id.bloodBankContact);
            bloodBankDistance = itemView.findViewById(R.id.bloodBankDistance);
            bloodBankAvailability = itemView.findViewById(R.id.bloodBankAvailability);
            callButton = itemView.findViewById(R.id.callButton);
        }
    }
}