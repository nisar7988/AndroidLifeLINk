package com.example.lifelink;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private CircleImageView ivProfileImage;
    private TextView tvProfileName, tvProfileEmail, tvBio, tvConnectionsCount;
    private Button btnEditProfile, btnLogout;

    // Remove Firebase fields
    // private FirebaseAuth mAuth;
    // private FirebaseFirestore db;
    // private FirebaseUser currentUser;
    // private String userId;
    // private String userProfileImageUrl;
    // private Uri imageUri;
    // private StorageReference storageRef;

    private ProgressDialog progressDialog;

    // Static user data
    private String userName = "Jaagu";
    private String userEmail = "jaag@gmail.com";
    private String userBio = "A passionate blood donor and community helper.";
    private int connectionsCount = 42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Remove Firebase init
        // mAuth = FirebaseAuth.getInstance();
        // db = FirebaseFirestore.getInstance();
        // currentUser = mAuth.getCurrentUser();
        // storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        // Remove Firebase user check
        // if (currentUser == null) { ... }

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvBio = findViewById(R.id.tvBio);
        tvConnectionsCount = findViewById(R.id.tvConnectionsCount);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Set profile image from drawable (profile.jpeg)
        try {
            ivProfileImage.setImageResource(R.drawable.profile);
        } catch (Exception e) {
            // fallback to a default icon if profile.jpeg is missing
            ivProfileImage.setImageResource(android.R.drawable.ic_menu_camera);
        }
        loadUserData();

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
            finish();
        });

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    private void loadUserData() {
        tvProfileName.setText(userName != null && !userName.isEmpty() ? userName : "Default Name");
        tvProfileEmail.setText(userEmail != null && !userEmail.isEmpty() ? userEmail : "default@email.com");
        tvBio.setText(userBio != null && !userBio.isEmpty() ? userBio : "No bio available.");
        tvConnectionsCount.setText(connectionsCount + " Connections");
        android.util.Log.d("ProfileActivity", "Loaded static data: " + userName + ", " + userEmail + ", " + userBio + ", " + connectionsCount);
        Toast.makeText(this, "Static profile loaded", Toast.LENGTH_SHORT).show();
    }

    private void showEditProfileDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        final EditText etEditName = new EditText(this);
        etEditName.setHint("Name");
        etEditName.setText(userName);

        final EditText etEditBio = new EditText(this);
        etEditBio.setHint("Bio");
        etEditBio.setText(userBio);

        layout.addView(etEditName);
        layout.addView(etEditBio);

        new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    userName = etEditName.getText().toString().trim();
                    userBio = etEditBio.getText().toString().trim();
                    loadUserData();
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // imageUri = data.getData(); // This line is removed as per the new_code
            // uploadProfileImage(); // This line is removed as per the new_code
        }
    }

    private void uploadProfileImage() {
        // This method is removed as per the new_code
    }

    private void saveProfileImageLocally() {
        // This method is removed as per the new_code
    }
}
