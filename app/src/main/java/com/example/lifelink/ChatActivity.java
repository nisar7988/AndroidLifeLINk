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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
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

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentUserId;
    private String chatUserId;
    private String chatUserName;

    private List<ChatMessageModel> messagesList;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

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
        try {
            if (db != null && !TextUtils.isEmpty(chatUserId)) {
                db.collection("users").document(chatUserId)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    String profileImagePath = documentSnapshot.getString("profileImagePath");
                                    if (profileImagePath != null && !profileImagePath.isEmpty()) {
                                        try {
                                            // Load profile image from local storage if available
                                            // For now, we'll just use the default image
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error loading profile image: " + e.getMessage());
                                        }
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Failed to get user profile: " + e.getMessage());
                            }
                        });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadUserProfileImage: " + e.getMessage());
        }
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
            
            // Create message object
            Map<String, Object> message = new HashMap<>();
            message.put("sender", currentUserId);
            message.put("receiver", chatUserId);
            message.put("text", messageText);
            message.put("timestamp", new Date());
            message.put("read", false);
            
            // Clear input field immediately for better UX
            etMessage.setText("");
            
            // Create a unique conversation ID to group messages between these two users
            String conversationId = getConversationId(currentUserId, chatUserId);
            
            // Add message to Firestore
            if (db != null) {
                db.collection("conversations").document(conversationId)
                        .collection("messages")
                        .add(message)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                // Update conversation metadata
                                updateConversationMetadata(conversationId, messageText);
                                
                                // Add the message to our local list for instant display
                                ChatMessageModel newMessage = new ChatMessageModel(
                                        documentReference.getId(),
                                        currentUserId,
                                        chatUserId,
                                        messageText,
                                        new Date().toString(),
                                        false
                                );
                                messagesList.add(newMessage);
                                chatAdapter.notifyItemInserted(messagesList.size() - 1);
                                
                                // Scroll to the latest message
                                if (messagesList.size() > 0) {
                                    rvMessages.smoothScrollToPosition(messagesList.size() - 1);
                                }
                                
                                // Update UI if this was the first message
                                if (messagesList.size() == 1 && tvEmptyChat != null) {
                                    tvEmptyChat.setVisibility(View.GONE);
                                    rvMessages.setVisibility(View.VISIBLE);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ChatActivity.this, "Error sending message: " + e.getMessage(), 
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(this, "Error: Database not initialized", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in sendMessage: " + e.getMessage());
            Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateConversationMetadata(String conversationId, String lastMessage) {
        try {
            if (db == null) return;
            
            // Create or update conversation metadata for both users
            Map<String, Object> conversationData = new HashMap<>();
            conversationData.put("lastMessage", lastMessage);
            conversationData.put("timestamp", new Date());
            conversationData.put("participants", new String[]{currentUserId, chatUserId});
            
            db.collection("conversation_metadata").document(conversationId)
                    .set(conversationData)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error updating conversation metadata: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in updateConversationMetadata: " + e.getMessage());
        }
    }

    private void loadMessages() {
        try {
            if (db == null || TextUtils.isEmpty(currentUserId) || TextUtils.isEmpty(chatUserId)) {
                if (tvEmptyChat != null) {
                    tvEmptyChat.setVisibility(View.VISIBLE);
                    rvMessages.setVisibility(View.GONE);
                }
                return;
            }
            
            String conversationId = getConversationId(currentUserId, chatUserId);
            
            db.collection("conversations").document(conversationId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            messagesList.clear();
                            
                            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    try {
                                        String sender = document.getString("sender");
                                        String receiver = document.getString("receiver");
                                        String text = document.getString("text");
                                        Date timestamp = document.getDate("timestamp");
                                        boolean isRead = Boolean.TRUE.equals(document.getBoolean("read"));
                                        
                                        ChatMessageModel message = new ChatMessageModel(
                                                document.getId(), 
                                                sender != null ? sender : "", 
                                                receiver != null ? receiver : "", 
                                                text != null ? text : "", 
                                                timestamp != null ? timestamp.toString() : "", 
                                                isRead);
                                        
                                        messagesList.add(message);
                                        
                                        // Mark received messages as read
                                        if (sender != null && sender.equals(chatUserId) && !isRead) {
                                            markMessageAsRead(document.getId(), conversationId);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error processing message document: " + e.getMessage());
                                    }
                                }
                            }
                            
                            // Update UI
                            updateUIAfterMessagesLoaded();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error loading messages: " + e.getMessage());
                            Toast.makeText(ChatActivity.this, "Error loading messages: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            
                            // Show empty state if we can't load messages
                            updateUIAfterMessagesLoaded();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadMessages: " + e.getMessage());
            updateUIAfterMessagesLoaded();
        }
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
        try {
            if (db == null || TextUtils.isEmpty(messageId) || TextUtils.isEmpty(conversationId)) {
                return;
            }
            
            db.collection("conversations").document(conversationId)
                    .collection("messages").document(messageId)
                    .update("read", true)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error marking message as read: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in markMessageAsRead: " + e.getMessage());
        }
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