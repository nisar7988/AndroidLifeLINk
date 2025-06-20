package com.example.lifelink;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<MessageModel> messagesList;
    private OnMessageClickListener listener;

    public MessageAdapter(Context context) {
        this.context = context;
        this.messagesList = new ArrayList<>();
    }

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.listener = listener;
    }

    public void setMessagesList(List<MessageModel> messagesList) {
        this.messagesList = messagesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messagesList.get(position);
        
        holder.tvMessageUserName.setText(message.getUserName());
        holder.tvMessagePreview.setText(message.getLastMessage());
        holder.tvMessageTime.setText(message.getTimestamp());
        
        // Show unread count if there are unread messages
        if (message.getUnreadCount() > 0) {
            holder.tvUnreadCount.setVisibility(View.VISIBLE);
            holder.tvUnreadCount.setText(String.valueOf(message.getUnreadCount()));
        } else {
            holder.tvUnreadCount.setVisibility(View.GONE);
        }
        
        // Set click listener for the whole item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMessageClick(message);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivMessageUserImage;
        TextView tvMessageUserName, tvMessagePreview, tvMessageTime, tvUnreadCount;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivMessageUserImage = itemView.findViewById(R.id.ivMessageUserImage);
            tvMessageUserName = itemView.findViewById(R.id.tvMessageUserName);
            tvMessagePreview = itemView.findViewById(R.id.tvMessagePreview);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);
        }
    }
    
    public interface OnMessageClickListener {
        void onMessageClick(MessageModel message);
    }
} 