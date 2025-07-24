package com.example.lifelink;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private Toolbar toolbar;
    private CircleImageView profileImage;
    private TextView tvUsername;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvEmptyChat;

    private String currentUserId = "jaagu";
    private String chatUserId;
    private String chatUserName;

    private List<ChatMessageModel> messagesList;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get chat user information from intent
        Intent intent = getIntent();
        if (intent != null) {
            chatUserId = intent.getStringExtra("user_id");
            chatUserName = intent.getStringExtra("user_name");
        }

        // Verify we have the required data
        if (TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(chatUserId)) {
            Toast.makeText(this, "Error: User information not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();

        // Set up RecyclerView
        setupRecyclerView();

        // Load chat messages
        try {
            loadMessages();
        } catch (Exception e) {
            Log.e(TAG, "Error loading messages: " + e.getMessage());
            Toast.makeText(this, "Could not load messages. Please try again.", Toast.LENGTH_SHORT).show();
        }

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("");
            }
            
            profileImage = findViewById(R.id.ivUserProfile);
            tvUsername = findViewById(R.id.tvChatUserName);
            rvMessages = findViewById(R.id.recyclerViewChat);
            etMessage = findViewById(R.id.etMessage);
            btnSend = findViewById(R.id.btnSend);
            
            // Add a null check for tvEmptyChat as it may not exist in the layout
            TextView emptyView = findViewById(R.id.tvEmptyChat);
            if (emptyView != null) {
                tvEmptyChat = emptyView;
            }
            
            // Set chat user name if available
            if (!TextUtils.isEmpty(chatUserName)) {
                tvUsername.setText(chatUserName);
            } else {
                tvUsername.setText("Chat");
            }
            
            // Load user profile image
            loadUserProfileImage();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage());
        }
    }

    private void loadUserProfileImage() {
        // Set a default image
        profileImage.setImageResource(android.R.drawable.ic_menu_camera);
    }

    private void setupRecyclerView() {
        try {
            messagesList = new ArrayList<>();
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setStackFromEnd(true); // Show latest messages at the bottom
            rvMessages.setLayoutManager(layoutManager);
            
            chatAdapter = new ChatAdapter(this, messagesList, currentUserId);
            rvMessages.setAdapter(chatAdapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        try {
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up click listeners: " + e.getMessage());
        }
    }

    private void sendMessage() {
        try {
            String messageText = etMessage.getText().toString().trim();
            
            if (TextUtils.isEmpty(messageText)) {
                return;
            }
            
            // Clear the input field
            etMessage.setText("");
            
            // Add message to static list
            ChatMessageModel newMessage = new ChatMessageModel(
                    String.valueOf(messagesList.size() + 1),
                    currentUserId,
                    chatUserId,
                    messageText,
                    "Now",
                    true
            );
            
            messagesList.add(newMessage);
            chatAdapter.notifyItemInserted(messagesList.size() - 1);
            
            // Scroll to bottom
            rvMessages.smoothScrollToPosition(messagesList.size() - 1);
            
            updateUIAfterMessagesLoaded();
        } catch (Exception e) {
            Log.e(TAG, "Error in sendMessage: " + e.getMessage());
        }
    }
    
    private void updateConversationMetadata(String conversationId, String lastMessage) {
        // This method is no longer needed as conversation logic is removed
    }

    private void loadMessages() {
        // Use static data
        messagesList.clear();
        messagesList.addAll(Arrays.asList(
            new ChatMessageModel("1", chatUserId, currentUserId, "Hi, are you available for donation?", "Today, 10:00 AM", false),
            new ChatMessageModel("2", currentUserId, chatUserId, "Yes, I am available.", "Today, 10:01 AM", true),
            new ChatMessageModel("3", chatUserId, currentUserId, "Great! Can you come to City Hospital?", "Today, 10:02 AM", false),
            new ChatMessageModel("4", currentUserId, chatUserId, "Sure, I will be there at 11 AM.", "Today, 10:03 AM", true)
        ));
        updateUIAfterMessagesLoaded();
    }
    
    private void updateUIAfterMessagesLoaded() {
        try {
            // Update UI
            if (messagesList.isEmpty() && tvEmptyChat != null) {
                tvEmptyChat.setVisibility(View.VISIBLE);
                rvMessages.setVisibility(View.GONE);
            } else {
                if (tvEmptyChat != null) {
                    tvEmptyChat.setVisibility(View.GONE);
                }
                rvMessages.setVisibility(View.VISIBLE);
                chatAdapter.notifyDataSetChanged();
                
                // Scroll to the bottom
                if (messagesList.size() > 0) {
                    rvMessages.smoothScrollToPosition(messagesList.size() - 1);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI: " + e.getMessage());
        }
    }
    
    private void markMessageAsRead(String messageId, String conversationId) {
        // This method is no longer needed as conversation logic is removed
    }
    
    private void markAllMessagesAsRead() {
        // This method is no longer needed as conversation logic is removed
    }
    
    // Generate a consistent conversation ID for two users, regardless of order
    private String getConversationId(String user1, String user2) {
        if (TextUtils.isEmpty(user1) || TextUtils.isEmpty(user2)) {
            return "conversation_default";
        }
        return user1.compareTo(user2) < 0 ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 