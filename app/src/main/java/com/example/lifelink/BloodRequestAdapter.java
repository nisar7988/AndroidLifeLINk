package com.example.lifelink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BloodRequestAdapter extends RecyclerView.Adapter<BloodRequestAdapter.BloodRequestViewHolder> {

    private List<BloodRequestModel> requestList;
    private OnBloodRequestClickListener listener;

    public BloodRequestAdapter(List<BloodRequestModel> requestList, OnBloodRequestClickListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BloodRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_request, parent, false);
        return new BloodRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BloodRequestViewHolder holder, int position) {
        BloodRequestModel request = requestList.get(position);
        
        holder.tvBloodType.setText(request.getBloodType());
        holder.tvHospitalName.setText(request.getHospitalName());
        holder.tvDistance.setText(request.getDistance());
        holder.tvTime.setText(request.getTimeAgo());
        
        // Show or hide urgency tag
        holder.tvUrgency.setVisibility(request.isUrgent() ? View.VISIBLE : View.GONE);
        
        // Set click listener for respond button
        holder.btnRespond.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRespondClick(request);
            }
        });
        
        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBloodRequestClick(request);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList != null ? requestList.size() : 0;
    }

    public void updateList(List<BloodRequestModel> newList) {
        this.requestList = newList;
        notifyDataSetChanged();
    }

    public static class BloodRequestViewHolder extends RecyclerView.ViewHolder {
        CardView cardBloodType;
        TextView tvBloodType, tvHospitalName, tvUrgency, tvTime, tvDistance;
        Button btnRespond;

        public BloodRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBloodType = itemView.findViewById(R.id.cardBloodType);
            tvBloodType = itemView.findViewById(R.id.tvBloodType);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvUrgency = itemView.findViewById(R.id.tvUrgency);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            btnRespond = itemView.findViewById(R.id.btnRespond);
        }
    }

    public interface OnBloodRequestClickListener {
        void onBloodRequestClick(BloodRequestModel request);
        void onRespondClick(BloodRequestModel request);
    }
} 