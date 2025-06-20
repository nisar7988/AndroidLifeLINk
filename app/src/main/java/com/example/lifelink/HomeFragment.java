package com.example.lifelink;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment implements BloodRequestAdapter.OnBloodRequestClickListener, 
                                                     NotificationAdapter.OnNotificationClickListener {

    private TextView tvWelcome, tvNoNotifications, tvNoBloodRequests;
    private TextView tvUserBloodType, tvDonationsCount, tvLivesSaved, tvLastDonation;
    private TextView tvViewAllRequests, tvViewAllNotifications;
    private RecyclerView rvNotifications, rvBloodRequests;
    private CircleImageView ivUserProfile;
    private LinearLayout btnDonateBlood, btnFindCenters, btnRequestBlood, btnEmergency;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String userId;
    private String userName;
    private String userBloodType;
    
    private BloodRequestAdapter bloodRequestAdapter;
    private NotificationAdapter notificationAdapter;
    private List<BloodRequestModel> bloodRequestsList;
    private List<NotificationModel> notificationsList;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
        
        // Initialize all views
        initializeViews(view);
        
        // Set up RecyclerViews
        setupRecyclerViews();
        
        // Set click listeners for quick action buttons
        setupClickListeners();
        
        // Load user data
        loadUserData();
        
        // Get notifications and blood requests
        loadNotifications();
        loadBloodRequests();
        
        return view;
    }
    
    private void initializeViews(View view) {
        // Welcome card views
        tvWelcome = view.findViewById(R.id.tvWelcome);
        ivUserProfile = view.findViewById(R.id.ivUserProfile);
        tvUserBloodType = view.findViewById(R.id.tvUserBloodType);
        tvDonationsCount = view.findViewById(R.id.tvDonationsCount);
        tvLivesSaved = view.findViewById(R.id.tvLivesSaved);
        tvLastDonation = view.findViewById(R.id.tvLastDonation);
        
        // Quick action buttons
        btnDonateBlood = view.findViewById(R.id.btnDonateBlood);
        btnFindCenters = view.findViewById(R.id.btnFindCenters);
        btnRequestBlood = view.findViewById(R.id.btnRequestBlood);
        btnEmergency = view.findViewById(R.id.btnEmergency);
        
        // Blood requests views
        tvNoBloodRequests = view.findViewById(R.id.tvNoBloodRequests);
        rvBloodRequests = view.findViewById(R.id.rvBloodRequests);
        tvViewAllRequests = view.findViewById(R.id.tvViewAllRequests);
        
        // Notifications views
        tvNoNotifications = view.findViewById(R.id.tvNoNotifications);
        rvNotifications = view.findViewById(R.id.rvNotifications);
        tvViewAllNotifications = view.findViewById(R.id.tvViewAllNotifications);
    }
    
    private void setupRecyclerViews() {
        // Blood requests
        bloodRequestsList = new ArrayList<>();
        rvBloodRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        bloodRequestAdapter = new BloodRequestAdapter(bloodRequestsList, this);
        rvBloodRequests.setAdapter(bloodRequestAdapter);
        
        // Notifications
        notificationsList = new ArrayList<>();
        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationAdapter = new NotificationAdapter(getContext(), notificationsList, this);
        rvNotifications.setAdapter(notificationAdapter);
    }
    
    private void setupClickListeners() {
        // Quick action buttons
        btnDonateBlood.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Donate Blood feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        btnFindCenters.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent mapIntent = new Intent(getActivity(), MapsActivity.class);
                startActivity(mapIntent);
            }
        });
        
        btnRequestBlood.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Request Blood feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        btnEmergency.setOnClickListener(v -> {
            // Show a test notification
            if (getContext() != null) {
                NotificationHelper.createNotificationChannel(getContext());
                NotificationHelper.sendEmergencyNotification(getContext(), 
                        "Blood Donation Emergency", 
                        "Emergency blood donation needed at City Hospital!");
                Toast.makeText(getContext(), "Emergency notification sent!", Toast.LENGTH_SHORT).show();
            }
        });
        
        // View all buttons
        tvViewAllRequests.setOnClickListener(v -> {
            Toast.makeText(getContext(), "View all blood requests coming soon", Toast.LENGTH_SHORT).show();
        });
        
        tvViewAllNotifications.setOnClickListener(v -> {
            Toast.makeText(getContext(), "View all notifications coming soon", Toast.LENGTH_SHORT).show();
        });
        
        // User profile picture
        ivUserProfile.setOnClickListener(v -> {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).navigateToProfile();
            }
        });
    }
    
    private void loadUserData() {
        if (userId != null) {
            db.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && isAdded()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    userName = document.getString("name");
                                    userBloodType = document.getString("bloodType");
                                    
                                    // Update UI with user data
                                    tvWelcome.setText("Welcome, " + userName + "!");
                                    tvUserBloodType.setText(userBloodType != null ? userBloodType : "Unknown");
                                    
                                    // Load profile image if available
                                    String profileImagePath = document.getString("profileImagePath");
                                    if (profileImagePath != null && !profileImagePath.isEmpty()) {
                                        try {
                                            File imageFile = new File(profileImagePath);
                                            if (imageFile.exists()) {
                                                Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                                                if (bitmap != null) {
                                                    ivUserProfile.setImageBitmap(bitmap);
                                                }
                                            }
                                        } catch (Exception e) {
                                            // If there's an error loading the profile image, just use the default
                                            Log.e("HomeFragment", "Error loading profile image: " + e.getMessage());
                                        }
                                    }
                                    
                                    // Load donation statistics
                                    Long donationsCount = document.getLong("donationsCount");
                                    Long livesSaved = document.getLong("livesSaved");
                                    Long lastDonationDays = document.getLong("lastDonationDays");
                                    
                                    // Update UI with donation statistics
                                    tvDonationsCount.setText(donationsCount != null ? donationsCount.toString() : "0");
                                    tvLivesSaved.setText(livesSaved != null ? livesSaved.toString() : "0");
                                    tvLastDonation.setText(lastDonationDays != null ? lastDonationDays.toString() : "--");
                                }
                            }
                        }
                    });
        }
    }
    
    private void loadNotifications() {
        // For demonstration, we'll just use test data
        // In a real app, you would fetch from Firestore
        notificationsList.clear();
        
        notificationsList.add(new NotificationModel("1", "Blood Drive", 
                "Campus blood drive next Monday at Student Center. All blood types welcome.", "2h ago", 0));
        
        notificationsList.add(new NotificationModel("2", "Blood Type in Demand", 
                "Your blood type " + userBloodType + " is in high demand! Please consider donating soon.", "1d ago", 2));
        
        notificationsList.add(new NotificationModel("3", "Thank You", 
                "Thank you for being a blood donor. Your last donation has helped save lives!", "2d ago", 1));
        
        notificationsList.add(new NotificationModel("4", "New Blood Request", 
                "There's a new blood request matching your blood type nearby. Check it out!", "3d ago", 3));
        
        // Update UI
        if (notificationsList.isEmpty()) {
            tvNoNotifications.setVisibility(View.VISIBLE);
            rvNotifications.setVisibility(View.GONE);
        } else {
            tvNoNotifications.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
            notificationAdapter.notifyDataSetChanged();
        }
    }
    
    private void loadBloodRequests() {
        // For demonstration, we'll just use test data
        // In a real app, you would fetch from Firestore
        bloodRequestsList.clear();
        
        bloodRequestsList.add(new BloodRequestModel("1", "A+", "City Hospital", true, "2h ago", "2.5 miles"));
        bloodRequestsList.add(new BloodRequestModel("2", "O-", "Memorial Medical Center", false, "5h ago", "5.1 miles"));
        bloodRequestsList.add(new BloodRequestModel("3", "B+", "University Hospital", true, "1d ago", "8.3 miles"));
        bloodRequestsList.add(new BloodRequestModel("4", "AB-", "Children's Hospital", false, "1d ago", "10.7 miles"));
        
        // Update UI
        if (bloodRequestsList.isEmpty()) {
            tvNoBloodRequests.setVisibility(View.VISIBLE);
            rvBloodRequests.setVisibility(View.GONE);
        } else {
            tvNoBloodRequests.setVisibility(View.GONE);
            rvBloodRequests.setVisibility(View.VISIBLE);
            bloodRequestAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBloodRequestClick(BloodRequestModel request) {
        // Handle blood request click
        Toast.makeText(getContext(), "Blood request details coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRespondClick(BloodRequestModel request) {
        // Handle respond button click
        Toast.makeText(getContext(), "Responding to " + request.getHospitalName() + " for " + 
                request.getBloodType() + " blood request", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotificationClick(NotificationModel notification) {
        // Handle notification click
        Toast.makeText(getContext(), notification.getTitle() + ": " + notification.getContent(), 
                Toast.LENGTH_SHORT).show();
    }
} 