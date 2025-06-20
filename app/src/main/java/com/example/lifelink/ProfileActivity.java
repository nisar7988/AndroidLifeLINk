package com.example.lifelink;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView ivProfileImage;
    private TextView tvProfileName, tvProfileEmail, tvBio, tvConnectionsCount;
    private Button btnEditProfile, btnLogout;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String userId;
    private String userProfileImageUrl;
    private Uri imageUri;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");
        
        if (currentUser == null) {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
            return;
        }
        
        userId = currentUser.getUid();
        
        // Initialize views
        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvBio = findViewById(R.id.tvBio);
        tvConnectionsCount = findViewById(R.id.tvConnectionsCount);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
        
        // Set click listener for profile image
        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        
        // Load user data
        loadUserData();
        
        // Set click listeners
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                finish();
            }
        });
        
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });
    }
    
    private void loadUserData() {
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = document.getString("name");
                                String email = document.getString("email");
                                String bio = document.getString("bio");
                                userProfileImageUrl = document.getString("profileImageUrl");
                                
                                tvProfileName.setText(name != null ? name : "No name available");
                                tvProfileEmail.setText(email != null ? email : "No email available");
                                
                                if (bio != null && !bio.isEmpty()) {
                                    tvBio.setText(bio);
                                } else {
                                    tvBio.setText("No bio added yet.");
                                }
                                
                                // Mock connection count for now
                                tvConnectionsCount.setText("0 Connections");
                                
                                // Load profile image if available
                                if (userProfileImageUrl != null && !userProfileImageUrl.isEmpty()) {
                                    Picasso.get().load(userProfileImageUrl)
                                            .placeholder(android.R.drawable.ic_menu_camera)
                                            .error(android.R.drawable.ic_menu_camera)
                                            .into(ivProfileImage);
                                } else {
                                    ivProfileImage.setImageResource(android.R.drawable.ic_menu_camera);
                                }
                            } else {
                                Toast.makeText(ProfileActivity.this, "User profile not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
                            Exception e = task.getException();
                            Log.e("ProfileActivity", "Error loading profile data", e);
                        }
                    }
                });
    }
    
    private void showEditProfileDialog() {
        // Create a LinearLayout container for the EditText fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        
        // Create EditText fields
        final EditText etEditName = new EditText(this);
        etEditName.setHint("Name");
        etEditName.setText(tvProfileName.getText().toString());
        
        final EditText etEditBio = new EditText(this);
        etEditBio.setHint("Bio");
        etEditBio.setText(tvBio.getText().toString());
        
        // Add EditText fields to the layout
        layout.addView(etEditName);
        layout.addView(etEditBio);
        
        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile")
                .setView(layout)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get updated values
                        String newName = etEditName.getText().toString().trim();
                        String newBio = etEditBio.getText().toString().trim();
                        
                        // Update user profile
                        updateUserProfile(newName, newBio);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    
    private void updateUserProfile(String name, String bio) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("bio", bio);
        
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Refresh profile data
                        loadUserData();
                        Snackbar.make(findViewById(android.R.id.content), "Profile updated successfully", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadProfileImage();
        }
    }
    
    private void uploadProfileImage() {
        if (imageUri != null) {
            // Show upload progress
            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();
            
            // Create a reference to the file location with userId as filename
            final StorageReference fileRef = storageRef.child(userId + ".jpg");
            
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get download URL and update profile
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    userProfileImageUrl = uri.toString();
                                    
                                    // Update the profile image URL in database
                                    db.collection("users").document(userId)
                                            .update("profileImageUrl", userProfileImageUrl)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Load the image into the ImageView
                                                    Picasso.get().load(userProfileImageUrl)
                                                            .placeholder(android.R.drawable.ic_menu_camera)
                                                            .error(android.R.drawable.ic_menu_camera)
                                                            .into(ivProfileImage);
                                                    Toast.makeText(ProfileActivity.this, "Profile image updated", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}