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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment implements BloodRequestAdapter.OnBloodRequestClickListener, 
                                                     NotificationAdapter.OnNotificationClickListener {

    private TextView tvWelcome, tvNoNotifications, tvNoBloodRequests;
    private TextView tvUserBloodType, tvDonationsCount, tvLivesSaved, tvLastDonation;
    private TextView tvViewAllRequests, tvViewAllNotifications;
    private RecyclerView rvNotifications, rvBloodRequests;
    private CircleImageView ivUserProfile;
    private LinearLayout btnDonateBlood, btnFindCenters, btnRequestBlood, btnEmergency;
    
    // Remove Firebase fields
    // private FirebaseAuth mAuth;
    // private FirebaseFirestore db;
    // private FirebaseUser currentUser;
    // private String userId;
    private String userName = "jaagu";
    private String userBloodType = "A+";
    private String userAddress = "123 Main St";
    
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
        
        // Remove Firebase init
        // mAuth = FirebaseAuth.getInstance();
        // db = FirebaseFirestore.getInstance();
        // currentUser = mAuth.getCurrentUser();
        
        // if (currentUser != null) {
        //     userId = currentUser.getUid();
        // }
        
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
            showDonateBloodDialog();
        });
        
        btnFindCenters.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent mapIntent = new Intent(getActivity(), MapsActivity.class);
                startActivity(mapIntent);
            }
        });
        
        btnRequestBlood.setOnClickListener(v -> {
            showBloodRequestDialog();
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
        // Use static data
                                    tvWelcome.setText("Welcome, " + userName + "!");
        tvUserBloodType.setText(userBloodType);
        tvDonationsCount.setText("2");
        tvLivesSaved.setText("6");
        tvLastDonation.setText("3");
        // Optionally set a default profile image
        ivUserProfile.setImageResource(R.drawable.profile);
    }
    
    private void loadNotifications() {
        // For demonstration, we'll just use test data
        // In a real app, you would fetch from Firestore
        notificationsList.clear();
        
        notificationsList.addAll(Arrays.asList(
            new NotificationModel("1", "Blood Drive", "Campus blood drive next Monday at Student Center. All blood types welcome.", "2h ago", 0),
            new NotificationModel("2", "Blood Type in Demand", "Your blood type " + userBloodType + " is in high demand! Please consider donating soon.", "1d ago", 2),
            new NotificationModel("3", "Thank You", "Thank you for being a blood donor. Your last donation has helped save lives!", "2d ago", 1),
            new NotificationModel("4", "New Blood Request", "There's a new blood request matching your blood type nearby. Check it out!", "3d ago", 3)
        ));
        
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
        
        bloodRequestsList.addAll(Arrays.asList(
            new BloodRequestModel("1", "A+", "City Hospital", true, "2h ago", "2.5 miles"),
            new BloodRequestModel("2", "O-", "Memorial Medical Center", false, "5h ago", "5.1 miles"),
            new BloodRequestModel("3", "B+", "University Hospital", true, "1d ago", "8.3 miles"),
            new BloodRequestModel("4", "AB-", "Children's Hospital", false, "1d ago", "10.7 miles")
        ));
        
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
    public void onBloodRequestClick(BloodRequestModel request) {
        // Handle blood request click
        Toast.makeText(getContext(), "Blood request details coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRespondClick(BloodRequestModel request) {
        // Show confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Respond to Blood Request");
        builder.setMessage("Are you sure you want to respond to the " + request.getBloodType() + 
                " blood request at " + request.getHospitalName() + "?");
        
        builder.setPositiveButton("Yes, I can help", (dialog, which) -> {
            respondToBloodRequest(request);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void respondToBloodRequest(BloodRequestModel request) {
        // Remove all Firestore and FirebaseAuth usages
                    Toast.makeText(getContext(), "Response sent successfully! The requester will be notified.", 
                            Toast.LENGTH_LONG).show();
    }
    
    private void sendResponseNotification(BloodRequestModel request) {
        // In a real app, you would send a push notification to the requester
        // For now, we'll just show a local notification
        if (getContext() != null) {
            NotificationHelper.sendBloodRequestNotification(
                    getContext(),
                    request.getBloodType(),
                    request.getHospitalName()
            );
        }
    }

    @Override
    public void onNotificationClick(NotificationModel notification) {
        // Handle notification click
        Toast.makeText(getContext(), notification.getTitle() + ": " + notification.getContent(), 
                Toast.LENGTH_SHORT).show();
    }

    private void showBloodRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create Blood Request");
        
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_blood_request, null);
        builder.setView(dialogView);
        
        Spinner spinnerBloodType = dialogView.findViewById(R.id.spinnerBloodType);
        EditText etHospitalName = dialogView.findViewById(R.id.etHospitalName);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);
        SwitchMaterial switchUrgent = dialogView.findViewById(R.id.switchUrgent);
        
        // Setup blood type spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.blood_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodType.setAdapter(adapter);
        
        builder.setPositiveButton("Create Request", (dialog, which) -> {
            String bloodType = spinnerBloodType.getSelectedItem().toString();
            String hospitalName = etHospitalName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            boolean isUrgent = switchUrgent.isChecked();
            
            if (hospitalName.isEmpty()) {
                Toast.makeText(getContext(), "Please enter hospital name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            createBloodRequest(bloodType, hospitalName, description, isUrgent);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void createBloodRequest(String bloodType, String hospitalName, String description, boolean isUrgent) {
        // Remove all Firestore and FirebaseAuth usages
                    Toast.makeText(getContext(), "Blood request created successfully", Toast.LENGTH_SHORT).show();
                    loadBloodRequests(); // Refresh the list
    }

    private void showDonateBloodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Donate Blood");
        
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_donate_blood, null);
        builder.setView(dialogView);
        
        EditText etDonationCenter = dialogView.findViewById(R.id.etDonationCenter);
        EditText etDonationDate = dialogView.findViewById(R.id.etDonationDate);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);
        
        // Set current date as default
        String currentDate = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new Date());
        etDonationDate.setText(currentDate);
        
        builder.setPositiveButton("Record Donation", (dialog, which) -> {
            String donationCenter = etDonationCenter.getText().toString().trim();
            String donationDate = etDonationDate.getText().toString().trim();
            String notes = etNotes.getText().toString().trim();
            
            if (donationCenter.isEmpty()) {
                Toast.makeText(getContext(), "Please enter donation center", Toast.LENGTH_SHORT).show();
                return;
            }
            
            recordBloodDonation(donationCenter, donationDate, notes);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void recordBloodDonation(String donationCenter, String donationDate, String notes) {
        // Remove all Firestore and FirebaseAuth usages
                    Toast.makeText(getContext(), "Blood donation recorded successfully", Toast.LENGTH_SHORT).show();
    }
    
    private void updateUserDonationStats() {
        // Remove all Firestore and FirebaseAuth usages
                                    // Refresh user data display
                                    loadUserData();
    }
} 