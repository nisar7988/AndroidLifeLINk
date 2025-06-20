package com.example.lifelink;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    
    private GoogleMap mMap;
    private String userAddress;
    private LatLng userLocation;
    private ExecutorService executorService;
    private LinearLayout errorLayout;
    private TextView tvErrorMessage, tvAddressDetails;
    private Button btnRetry, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        
        // Initialize views
        errorLayout = findViewById(R.id.errorLayout);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvAddressDetails = findViewById(R.id.tvAddressDetails);
        btnRetry = findViewById(R.id.btnRetry);
        btnBack = findViewById(R.id.btnBack);
        
        // Initialize executor service for background tasks
        executorService = Executors.newSingleThreadExecutor();
        
        // Get the address from intent
        if (getIntent().hasExtra("address")) {
            userAddress = getIntent().getStringExtra("address");
            Toast.makeText(this, "Location: " + userAddress, Toast.LENGTH_SHORT).show();
            
            // Show address in the text view
            if (tvAddressDetails != null) {
                tvAddressDetails.setText(userAddress);
            }
        } else {
            showError("Address not provided");
            return;
        }
        
        if (errorLayout != null) {
            btnRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupMap();
                }
            });
            
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        
        // Check if Google Play Services are available
        if (checkPlayServices()) {
            setupMap();
        } else {
            showSimpleAddressView();
        }
    }
    
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google Play services not available");
            return false;
        }
        return true;
    }
    
    private void setupMap() {
        // Check if the Google Maps API key is the default placeholder
        String apiKey = getString(R.string.google_maps_key);
        if ("YOUR_API_KEY".equals(apiKey)) {
            showSimpleAddressView();
            return;
        }
        
        // Get the SupportMapFragment and request notification when the map is ready
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            // Hide error layout if it was shown
            if (errorLayout != null) {
                errorLayout.setVisibility(View.GONE);
            }
            mapFragment.getMapAsync(this);
        } else {
            showSimpleAddressView();
        }
    }
    
    private void showSimpleAddressView() {
        if (errorLayout != null) {
            errorLayout.setVisibility(View.VISIBLE);
            tvErrorMessage.setText("Map is not available. Below is the address information:");
            if (tvAddressDetails != null) {
                tvAddressDetails.setText(userAddress);
                tvAddressDetails.setVisibility(View.VISIBLE);
            }
            btnRetry.setVisibility(View.GONE);
            btnBack.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Map not available. Address: " + userAddress, Toast.LENGTH_LONG).show();
            // Finish after delay to show the toast
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 3000);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        if (userAddress != null && !userAddress.isEmpty()) {
            // Geocode the address in a background thread
            geocodeAddress();
        } else {
            showError("Address not available");
        }
    }
    
    private void geocodeAddress() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocationName(userAddress, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        final LatLng location = new LatLng(address.getLatitude(), address.getLongitude());
                        
                        // Update UI on main thread
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                updateMapWithLocation(location);
                            }
                        });
                    } else {
                        // Fallback to showing simple address view
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                showToast("Could not find the address on map");
                                showSimpleAddressView();
                            }
                        });
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Geocoding error: " + e.getMessage());
                    // Fallback to showing simple address view
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            showToast("Error finding location: " + e.getMessage());
                            showSimpleAddressView();
                        }
                    });
                }
            }
        });
    }
    
    private void updateMapWithLocation(LatLng location) {
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
    }
    
    private void showError(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (errorLayout != null) {
                    errorLayout.setVisibility(View.VISIBLE);
                    tvErrorMessage.setText(message);
                }
                Log.e(TAG, "Map error: " + message);
                Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void showToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
} 