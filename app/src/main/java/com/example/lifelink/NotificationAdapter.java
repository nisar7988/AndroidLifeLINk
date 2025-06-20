package com.example.lifelink;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<NotificationModel> notificationList;
    private OnNotificationClickListener listener;
    private Context context;

    public NotificationAdapter(Context context, List<NotificationModel> notificationList, OnNotificationClickListener listener) {
        this.context = context;
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);
        
        holder.tvNotificationTitle.setText(notification.getTitle());
        holder.tvNotificationContent.setText(notification.getContent());
        holder.tvNotificationTime.setText(notification.getTimeAgo());
        
        // Set appropriate icon and color based on notification type
        switch (notification.getNotificationType()) {
            case 0: // info
                holder.ivNotificationType.setImageResource(android.R.drawable.ic_dialog_info);
                holder.ivNotificationType.setColorFilter(ContextCompat.getColor(context, R.color.info_blue));
                holder.cardIcon.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_bg_2));
                break;
            case 1: // success
                holder.ivNotificationType.setImageResource(android.R.drawable.ic_dialog_info);
                holder.ivNotificationType.setColorFilter(ContextCompat.getColor(context, R.color.success_green));
                holder.cardIcon.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_bg_3));
                break;
            case 2: // warning
                holder.ivNotificationType.setImageResource(android.R.drawable.ic_dialog_alert);
                holder.ivNotificationType.setColorFilter(ContextCompat.getColor(context, R.color.warning_amber));
                holder.cardIcon.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_bg_3));
                break;
            case 3: // emergency
                holder.ivNotificationType.setImageResource(android.R.drawable.ic_dialog_alert);
                holder.ivNotificationType.setColorFilter(ContextCompat.getColor(context, R.color.emergency_red));
                holder.cardIcon.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_bg_1));
                break;
        }
        
        // Set click listener for the item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    public void updateList(List<NotificationModel> newList) {
        this.notificationList = newList;
        notifyDataSetChanged();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        CardView cardIcon;
        ImageView ivNotificationType;
        TextView tvNotificationTitle, tvNotificationContent, tvNotificationTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardIcon = itemView.findViewById(R.id.cardIcon);
            ivNotificationType = itemView.findViewById(R.id.ivNotificationType);
            tvNotificationTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvNotificationContent = itemView.findViewById(R.id.tvNotificationContent);
            tvNotificationTime = itemView.findViewById(R.id.tvNotificationTime);
        }
    }

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationModel notification);
    }
} 