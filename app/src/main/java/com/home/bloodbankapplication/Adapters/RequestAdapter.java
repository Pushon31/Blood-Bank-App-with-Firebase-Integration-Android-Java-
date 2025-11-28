package com.home.bloodbankapplication.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.home.bloodbankapplication.DataModels.RequestDataModel;
import com.home.bloodbankapplication.R;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private Context context;
    private List<RequestDataModel> requestList;

    public RequestAdapter(Context context, List<RequestDataModel> requestList) {
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(context).inflate(R.layout.request_item_layout, parent, false);
            return new RequestViewHolder(view);
        } catch (Exception e) {
            Log.e("RequestAdapter", "Error inflating layout: " + e.getMessage());
            // Return a simple ViewHolder as fallback
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

            if (request != null && holder.messageText != null) {
                holder.messageText.setText(request.getMessage() != null ? request.getMessage() : "No message");
            }

            // Call button
            if (holder.callButton != null) {
                holder.callButton.setOnClickListener(v -> {
                    Toast.makeText(context, "Call feature coming soon!", Toast.LENGTH_SHORT).show();
                });
            }

            // Share button
            if (holder.shareButton != null) {
                holder.shareButton.setOnClickListener(v -> {
                    Toast.makeText(context, "Share feature coming soon!", Toast.LENGTH_SHORT).show();
                });
            }
        } catch (Exception e) {
            Log.e("RequestAdapter", "Error in onBindViewHolder: " + e.getMessage());
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
        ImageView shareButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                requestImage = itemView.findViewById(R.id.request_image);
                messageText = itemView.findViewById(R.id.message_text);
                callButton = itemView.findViewById(R.id.call_button);
                shareButton = itemView.findViewById(R.id.share_button);
            } catch (Exception e) {
                Log.e("RequestViewHolder", "Error finding views: " + e.getMessage());
            }
        }
    }
}