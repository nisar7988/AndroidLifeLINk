package com.example.lifelink;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "ChatAdapter";
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    
    private Context context;
    private List<ChatMessageModel> messageList;
    private String currentUserId;
    
    public ChatAdapter(Context context, List<ChatMessageModel> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId != null ? currentUserId : "";
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            if (viewType == VIEW_TYPE_SENT) {
                View view = LayoutInflater.from(context).inflate(R.layout.item_chat_sent, parent, false);
                return new SentMessageViewHolder(view);
            } else {
                View view = LayoutInflater.from(context).inflate(R.layout.item_chat_received, parent, false);
                return new ReceivedMessageViewHolder(view);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateViewHolder: " + e.getMessage());
            // Fallback to sent message layout
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_sent, parent, false);
            return new SentMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            if (position < 0 || position >= messageList.size()) {
                return;
            }
            
            ChatMessageModel message = messageList.get(position);
            if (message == null) {
                return;
            }
            
            if (holder instanceof SentMessageViewHolder) {
                SentMessageViewHolder viewHolder = (SentMessageViewHolder) holder;
                viewHolder.tvMessage.setText(message.getText());
                viewHolder.tvTime.setText(formatTime(message.getTimestamp()));
                
                // Show read status
                if (message.isRead()) {
                    viewHolder.tvStatus.setText("Read");
                } else {
                    viewHolder.tvStatus.setText("Sent");
                }
                
            } else if (holder instanceof ReceivedMessageViewHolder) {
                ReceivedMessageViewHolder viewHolder = (ReceivedMessageViewHolder) holder;
                viewHolder.tvMessage.setText(message.getText());
                viewHolder.tvTime.setText(formatTime(message.getTimestamp()));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onBindViewHolder: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }
    
    @Override
    public int getItemViewType(int position) {
        try {
            if (messageList != null && position >= 0 && position < messageList.size()) {
                ChatMessageModel message = messageList.get(position);
                
                if (message != null && !TextUtils.isEmpty(message.getSender()) 
                        && message.getSender().equals(currentUserId)) {
                    return VIEW_TYPE_SENT;
                } else {
                    return VIEW_TYPE_RECEIVED;
                }
            }
            return VIEW_TYPE_RECEIVED; // Default fallback
        } catch (Exception e) {
            Log.e(TAG, "Error in getItemViewType: " + e.getMessage());
            return VIEW_TYPE_RECEIVED; // Default fallback
        }
    }
    
    private String formatTime(String timestampStr) {
        try {
            if (timestampStr == null || timestampStr.isEmpty()) {
                return "";
            }
            
            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
            inputFormat.setTimeZone(TimeZone.getDefault());
            
            Date date = inputFormat.parse(timestampStr);
            if (date == null) return "";
            
            SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.US);
            return outputFormat.format(date);
            
        } catch (ParseException e) {
            // If there's an error parsing the date, just return a safe value
            Log.e(TAG, "Error parsing date: " + e.getMessage());
            return "Just now";
        } catch (Exception e) {
            Log.e(TAG, "Error formatting time: " + e.getMessage());
            return "Just now";
        }
    }
    
    // ViewHolder for sent messages
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvStatus;
        
        SentMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
    
    // ViewHolder for received messages
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        
        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
} 