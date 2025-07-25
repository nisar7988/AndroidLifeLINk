package com.example.lifelink;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionsFragment extends Fragment implements ConnectionAdapter.OnConnectionClickListener {

    private RecyclerView rvConnections;
    private TextView tvNoConnections;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddConnection;
    private TextInputEditText etSearch;
    
    private ConnectionAdapter connectionAdapter;
    private List<ConnectionModel> connectionsList;
    private List<ConnectionModel> filteredList;

    public ConnectionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connections, container, false);
        
        // Initialize views
        rvConnections = view.findViewById(R.id.rvConnections);
        tvNoConnections = view.findViewById(R.id.tvNoConnections);
        progressBar = view.findViewById(R.id.progressBar);
        fabAddConnection = view.findViewById(R.id.fabAddConnection);
        etSearch = view.findViewById(R.id.etSearch);
        
        // Setup RecyclerView
        rvConnections.setLayoutManager(new LinearLayoutManager(getContext()));
        connectionAdapter = new ConnectionAdapter(getContext());
        connectionAdapter.setOnConnectionClickListener(this);
        rvConnections.setAdapter(connectionAdapter);
        
        // Initialize data
        connectionsList = new ArrayList<>();
        filteredList = new ArrayList<>();
        
        // Add sample data
        loadSampleData();
        
        // Set up search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterConnections(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Set up add connection button
        fabAddConnection.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Add connection feature coming soon", Toast.LENGTH_SHORT).show();
        });
        
        return view;
    }
    
    private void loadSampleData() {
        // Show progress while loading
        progressBar.setVisibility(View.VISIBLE);
        rvConnections.setVisibility(View.GONE);
        tvNoConnections.setVisibility(View.GONE);
        // Use static data
                    connectionsList.clear();
        connectionsList.addAll(Arrays.asList(
            new ConnectionModel("1", "John Smith", "A+", "2.3", false),
            new ConnectionModel("2", "Sarah Johnson", "O-", "4.5", true),
            new ConnectionModel("3", "Michael Brown", "B+", "1.8", false),
            new ConnectionModel("4", "Emily Davis", "AB+", "5.2", false),
            new ConnectionModel("5", "David Wilson", "A-", "3.7", true)
        ));
        filteredList.clear();
        filteredList.addAll(connectionsList);
        connectionAdapter.setConnectionsList(filteredList);
        progressBar.setVisibility(View.GONE);
        if (filteredList.isEmpty()) {
            rvConnections.setVisibility(View.GONE);
            tvNoConnections.setVisibility(View.VISIBLE);
        } else {
            rvConnections.setVisibility(View.VISIBLE);
            tvNoConnections.setVisibility(View.GONE);
        }
    }
    
    private void filterConnections(String query) {
        filteredList.clear();
        
        if (query.isEmpty()) {
            filteredList.addAll(connectionsList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            
            for (ConnectionModel connection : connectionsList) {
                if (connection.getName().toLowerCase().contains(lowerCaseQuery) ||
                        connection.getBloodType().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(connection);
                }
            }
        }
        
        connectionAdapter.setConnectionsList(filteredList);
        
        // Show "no connections" message if filter results are empty
        if (filteredList.isEmpty()) {
            rvConnections.setVisibility(View.GONE);
            tvNoConnections.setVisibility(View.VISIBLE);
        } else {
            rvConnections.setVisibility(View.VISIBLE);
            tvNoConnections.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConnectionClick(ConnectionModel connection) {
        Toast.makeText(getContext(), "Clicked on " + connection.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectClick(ConnectionModel connection) {
        // Show confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Connect with " + connection.getName());
        builder.setMessage("Would you like to connect with " + connection.getName() + 
                " (" + connection.getBloodType() + ")? You'll be able to message each other.");
        
        builder.setPositiveButton("Connect", (dialog, which) -> {
            // sendConnectionRequest(connection); // Removed Firebase logic
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void sendConnectionRequest(ConnectionModel connection) {
        // Removed FirebaseAuth and Firestore usages
        Toast.makeText(getContext(), "Connection request feature coming soon", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessageClick(ConnectionModel connection) {
        // Open chat activity with the selected connection
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("user_id", connection.getId());
        intent.putExtra("user_name", connection.getName());
        startActivity(intent);
    }
} 