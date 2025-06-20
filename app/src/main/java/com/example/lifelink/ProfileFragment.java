package com.example.lifelink;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.OnProgressListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String PROFILE_IMAGE_FILENAME = "profile_image.jpg";
    
    private CircleImageView ivProfileImage;
    private TextView tvProfileName, tvProfileEmail, tvBio, tvBloodType, tvPhone, tvAddress, tvAvailabilityStatus;
    private Button btnEditProfile, btnLogout, btnViewOnMap;
    private SwitchMaterial switchAvailability;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String userId;
    private String userAddress;
    private String userProfileImagePath; // Local path to stored image
    private Uri imageUri;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        if (currentUser == null) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
            return view;
        }
        
        userId = currentUser.getUid();
        
        // Initialize views
        ivProfileImage = view.findViewById(R.id.ivProfileImage);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvBio = view.findViewById(R.id.tvBio);
        tvBloodType = view.findViewById(R.id.tvBloodType);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvAvailabilityStatus = view.findViewById(R.id.tvAvailabilityStatus);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnViewOnMap = view.findViewById(R.id.btnViewOnMap);
        switchAvailability = view.findViewById(R.id.switchAvailability);
        
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
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });
        
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });
        
        btnViewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userAddress != null && !userAddress.isEmpty()) {
                    // Launch the MapsActivity with the address
                    Intent mapIntent = new Intent(getActivity(), MapsActivity.class);
                    mapIntent.putExtra("address", userAddress);
                    startActivity(mapIntent);
                } else {
                    Toast.makeText(getActivity(), "Address not available", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        switchAvailability.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateAvailabilityStatus(isChecked);
            }
        });
        
        return view;
    }
    
    private void loadUserData() {
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && isAdded()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = document.getString("name");
                                String email = document.getString("email");
                                String bio = document.getString("bio");
                                String bloodType = document.getString("bloodType");
                                String phone = document.getString("phone");
                                userAddress = document.getString("address");
                                Boolean isAvailable = document.getBoolean("isAvailable");
                                userProfileImagePath = document.getString("profileImagePath");
                                
                                tvProfileName.setText(name);
                                tvProfileEmail.setText(email);
                                
                                if (bio != null && !bio.isEmpty()) {
                                    tvBio.setText(bio);
                                } else {
                                    tvBio.setText("No bio added yet.");
                                }
                                
                                if (bloodType != null) {
                                    tvBloodType.setText(bloodType);
                                }
                                
                                if (phone != null) {
                                    tvPhone.setText(phone);
                                }
                                
                                if (userAddress != null) {
                                    tvAddress.setText(userAddress);
                                }
                                
                                if (isAvailable != null) {
                                    switchAvailability.setChecked(isAvailable);
                                    tvAvailabilityStatus.setText(isAvailable ? 
                                            R.string.available : R.string.unavailable);
                                    tvAvailabilityStatus.setTextColor(getResources().getColor(
                                            isAvailable ? R.color.colorPrimary : R.color.grey));
                                }
                                
                                // Load profile image if available
                                loadProfileImageFromStorage();
                            } else if (isAdded()) {
                                Toast.makeText(getActivity(), "User profile not found", Toast.LENGTH_SHORT).show();
                            }
                        } else if (isAdded()) {
                            Toast.makeText(getActivity(), "Failed to load profile data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    private void loadProfileImageFromStorage() {
        try {
            // Try to load image from internal storage
            File storageDir = getActivity().getFilesDir();
            File imageFile = new File(storageDir, userId + "_" + PROFILE_IMAGE_FILENAME);
            
            if (imageFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                if (bitmap != null) {
                    ivProfileImage.setImageBitmap(bitmap);
                    return;
                }
            }
            
            // If not in internal storage but path is in database, try to find it
            if (userProfileImagePath != null && !userProfileImagePath.isEmpty()) {
                File filePath = new File(userProfileImagePath);
                if (filePath.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath());
                    if (bitmap != null) {
                        ivProfileImage.setImageBitmap(bitmap);
                        return;
                    }
                }
            }
            
            // If no image found, use default
            ivProfileImage.setImageResource(android.R.drawable.ic_menu_camera);
        } catch (Exception e) {
            Log.e("ProfileFragment", "Error loading profile image: " + e.getMessage(), e);
            ivProfileImage.setImageResource(android.R.drawable.ic_menu_camera);
        }
    }
    
    private void updateAvailabilityStatus(final boolean isAvailable) {
        if (userId != null) {
            db.collection("users").document(userId)
                    .update("isAvailable", isAvailable)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (isAdded()) {
                                tvAvailabilityStatus.setText(isAvailable ? 
                                        R.string.available : R.string.unavailable);
                                tvAvailabilityStatus.setTextColor(getResources().getColor(
                                        isAvailable ? R.color.colorPrimary : R.color.grey));
                                Toast.makeText(getActivity(), 
                                        "Availability status updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    
    private void showEditProfileDialog() {
        // Create a LinearLayout container for the EditText fields
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        
        // Create EditText fields
        final EditText etEditName = new EditText(getContext());
        etEditName.setHint("Name");
        etEditName.setText(tvProfileName.getText().toString());
        
        final EditText etEditBio = new EditText(getContext());
        etEditBio.setHint("Bio");
        etEditBio.setText(tvBio.getText().toString());
        
        final EditText etEditPhone = new EditText(getContext());
        etEditPhone.setHint("Phone");
        etEditPhone.setText(tvPhone.getText().toString());
        
        final EditText etEditAddress = new EditText(getContext());
        etEditAddress.setHint("Address");
        etEditAddress.setText(tvAddress.getText().toString());
        
        // Add EditText fields to the layout
        layout.addView(etEditName);
        layout.addView(etEditBio);
        layout.addView(etEditPhone);
        layout.addView(etEditAddress);
        
        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Profile")
                .setView(layout)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get updated values
                        String newName = etEditName.getText().toString().trim();
                        String newBio = etEditBio.getText().toString().trim();
                        String newPhone = etEditPhone.getText().toString().trim();
                        String newAddress = etEditAddress.getText().toString().trim();
                        
                        // Update user profile
                        updateUserProfile(newName, newBio, newPhone, newAddress);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    
    private void updateUserProfile(String name, String bio, String phone, String address) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("bio", bio);
        updates.put("phone", phone);
        updates.put("address", address);
        
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (isAdded()) {
                            // Refresh profile data
                            loadUserData();
                            Snackbar.make(getView(), "Profile updated successfully", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (isAdded()) {
                            Toast.makeText(getActivity(), "Failed to update profile: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    
    private void openFileChooser() {
        try {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } catch (Exception e) {
            Log.e("ProfileFragment", "Error opening file chooser: " + e.getMessage(), e);
            Toast.makeText(getActivity(), "Error opening image picker: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        try {
            if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK
                    && data != null && data.getData() != null) {
                imageUri = data.getData();
                
                // Validate the URI
                if (getActivity().getContentResolver().getType(imageUri) == null) {
                    Toast.makeText(getActivity(), "Invalid image format", Toast.LENGTH_LONG).show();
                    return;
                }
                
                saveProfileImageToStorage();
            }
        } catch (Exception e) {
            Log.e("ProfileFragment", "Error in activity result: " + e.getMessage(), e);
            Toast.makeText(getActivity(), "Error processing selected image: " + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
        }
    }
    
    private void saveProfileImageToStorage() {
        if (imageUri == null) {
            Toast.makeText(getActivity(), "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (getActivity() == null) {
            Log.e("ProfileFragment", "Activity is null, cannot save image");
            return;
        }
        
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(getActivity(), "User ID not found. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }
        
        try {
            // Show progress dialog
            final AlertDialog progressDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Saving")
                .setMessage("Please wait while we save your profile photo...")
                .setCancelable(false)
                .create();
            progressDialog.show();
            
            // Execute image processing in background thread
            new SaveImageTask(getActivity(), progressDialog, imageUri, userId).execute();
            
        } catch (Exception e) {
            Log.e("ProfileFragment", "Unexpected error starting image save: " + e.getMessage(), e);
            Toast.makeText(getActivity(), "Unexpected error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // AsyncTask to handle image processing in background
    private class SaveImageTask extends AsyncTask<Void, Integer, Bitmap> {
        private Context context;
        private AlertDialog progressDialog;
        private Uri imageUri;
        private String userId;
        private String imagePath;
        private Exception error;
        
        public SaveImageTask(Context context, AlertDialog progressDialog, Uri imageUri, String userId) {
            this.context = context;
            this.progressDialog = progressDialog;
            this.imageUri = imageUri;
            this.userId = userId;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        
        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                // Create a unique filename for this user's profile image
                final String filename = userId + "_" + PROFILE_IMAGE_FILENAME;
                
                // Get a reference to the app's internal storage directory
                File storageDir = context.getFilesDir();
                final File imageFile = new File(storageDir, filename);
                
                // If file already exists, delete it
                if (imageFile.exists()) {
                    imageFile.delete();
                }
                
                // Get bitmap from URI and compress it
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
                
                // Resize the image if it's too large
                int maxSize = 800; // max width/height in pixels
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                
                Bitmap resizedBitmap;
                if (width > maxSize || height > maxSize) {
                    float aspectRatio = (float) width / (float) height;
                    
                    if (width > height) {
                        width = maxSize;
                        height = Math.round(width / aspectRatio);
                    } else {
                        height = maxSize;
                        width = Math.round(height * aspectRatio);
                    }
                    
                    resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
                    bitmap.recycle(); // Free up the original bitmap memory
                } else {
                    resizedBitmap = bitmap;
                }
                
                // Save the bitmap to internal storage
                FileOutputStream fos = new FileOutputStream(imageFile);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.flush();
                fos.close();
                
                // Get the absolute path to the saved image
                imagePath = imageFile.getAbsolutePath();
                
                return resizedBitmap;
                
            } catch (Exception e) {
                error = e;
                Log.e("ProfileFragment", "Error in background processing: " + e.getMessage(), e);
                return null;
            }
        }
        
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // Update progress dialog if needed
            progressDialog.setMessage("Processing image: " + values[0] + "%");
        }
        
        @Override
        protected void onPostExecute(final Bitmap result) {
            super.onPostExecute(result);
            
            if (error != null || result == null) {
                progressDialog.dismiss();
                String errorMsg = error != null ? error.getMessage() : "Failed to process image";
                Toast.makeText(context, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                return;
            }
            
            // Update database with the local path
            db.collection("users").document(userId)
                    .update("profileImagePath", imagePath)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Display the new image
                            ivProfileImage.setImageBitmap(result);
                            
                            // Save path for future reference
                            userProfileImagePath = imagePath;
                            
                            progressDialog.dismiss();
                            Toast.makeText(context, "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Log.e("ProfileFragment", "Failed to update database: " + e.getMessage(), e);
                            Toast.makeText(context, "Saved image locally but failed to update database", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
} 