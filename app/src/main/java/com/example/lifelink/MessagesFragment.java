package com.example.lifelink;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MessagesFragment extends Fragment implements MessageAdapter.OnMessageClickListener {

    private RecyclerView rvMessages;
    private TextView tvNoMessages;
    private ProgressBar progressBar;
    private FloatingActionButton fabNewMessage;
    
    private MessageAdapter messageAdapter;
    private List<MessageModel> messagesList;

    public MessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_messages, container, false);
        
        // Initialize views
        rvMessages = view.findViewById(R.id.rvMessages);
        tvNoMessages = view.findViewById(R.id.tvNoMessages);
        progressBar = view.findViewById(R.id.progressBar);
        fabNewMessage = view.findViewById(R.id.fabNewMessage);
        
        // Setup RecyclerView
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        messageAdapter = new MessageAdapter(getContext());
        messageAdapter.setOnMessageClickListener(this);
        rvMessages.setAdapter(messageAdapter);
        
        // Initialize data
        messagesList = new ArrayList<>();
        
        // Add sample data
        loadSampleData();
        
        // Set up new message button
        fabNewMessage.setOnClickListener(v -> {
            Toast.makeText(getContext(), "New message feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        return view;
    }
    
    private void loadSampleData() {
        // Show progress while loading
        progressBar.setVisibility(View.VISIBLE);
        rvMessages.setVisibility(View.GONE);
        tvNoMessages.setVisibility(View.GONE);
        
        // Use static data
                    messagesList.clear();
        messagesList.addAll(Arrays.asList(
            new MessageModel("1", "user1", "John Smith", "Hey, are you available for donation tomorrow?", "Today, 10:30 AM", 2),
            new MessageModel("2", "user2", "Sarah Johnson", "Thank you for your help!", "Yesterday", 0),
            new MessageModel("3", "user3", "Michael Brown", "I'll be at the hospital at 3 PM", "Yesterday", 0),
            new MessageModel("4", "user4", "Emily Davis", "Do you know where the donation center is located?", "25/3/2023", 1),
            new MessageModel("5", "user5", "David Wilson", "Is AB+ compatible with my blood type?", "20/3/2023", 0)
        ));
        messageAdapter.setMessagesList(messagesList);
        progressBar.setVisibility(View.GONE);
        updateMessagesUI();
    }
    
    private void updateMessagesUI() {
        progressBar.setVisibility(View.GONE);
        
        if (messagesList.isEmpty()) {
            rvMessages.setVisibility(View.GONE);
            tvNoMessages.setVisibility(View.VISIBLE);
        } else {
            rvMessages.setVisibility(View.VISIBLE);
            tvNoMessages.setVisibility(View.GONE);
        }
    }
    
    private String getTimeAgo(Date date) {
        if (date == null) return "Unknown";
        
        long timeInMillis = date.getTime();
        long now = System.currentTimeMillis();
        long diff = now - timeInMillis;
        
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d ago";
        } else if (hours > 0) {
            return hours + "h ago";
        } else if (minutes > 0) {
            return minutes + "m ago";
        } else {
            return "Just now";
        }
    }

    @Override
    public void onMessageClick(MessageModel message) {
        // Mark messages as read by updating the UI
        int position = messagesList.indexOf(message);
        if (position != -1) {
            messagesList.get(position).setUnreadCount(0);
            messageAdapter.notifyItemChanged(position);
        }
        
        // Open chat activity with the selected user
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("user_id", message.getUserId());
        intent.putExtra("user_name", message.getUserName());
        startActivity(intent);
    }
} 