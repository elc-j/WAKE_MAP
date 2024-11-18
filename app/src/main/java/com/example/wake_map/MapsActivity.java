package com.example.wake_map;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.atomic.AtomicReference;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mMap;
    private EditText originInput, destinationInput;
    private Button getDirectionsButton;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng currentLocation;

    private String selectedMode = "transit"; // Default mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize views
        originInput = findViewById(R.id.origin_input);
        destinationInput = findViewById(R.id.destination_input);
        getDirectionsButton = findViewById(R.id.get_directions_button);

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Check and request location permission
        checkLocationPermission();

        // Initialize the Spinner
        Spinner transportModeSpinner = findViewById(R.id.transport_mode_spinner);

        // Default transport modes
        String[] transportModes = {"driving", "transit", "walking", "bicycling"};
        transportModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMode = transportModes[position]; // Update mode based on user selection
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMode = "transit"; // Default to transit
            }
        });

        // Set up "Get Directions" button
        getDirectionsButton.setOnClickListener(v -> {
            String origin = originInput.getText().toString();
            String destination = destinationInput.getText().toString();

            if (origin.isEmpty()) {
                if (currentLocation != null) {
                    origin = currentLocation.latitude + "," + currentLocation.longitude; // Use current location
                } else {
                    Toast.makeText(MapsActivity.this, "Localisation actuelle introuvable.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (destination.isEmpty()) {
                Toast.makeText(MapsActivity.this, "Veuillez remplir le champ destination.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pass origin and destination to ResultsActivity
            Intent intent = new Intent(MapsActivity.this, ResultsActivity.class);
            intent.putExtra("origin", origin);
            intent.putExtra("destination", destination);
            intent.putExtra("mode", selectedMode); // Pass selected mode (optional for dynamic WebView loading)
            startActivity(intent);
        });

        // Settings button to open SettingsActivity
        ImageButton settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        // SetMusicButton to open SetMusicActivity
        Button setMusicButton = findViewById(R.id.setMusicButton);
        setMusicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, SetMusicActivity.class);
                startActivity(intent);
            }
        });
        Button startNavigationButton = findViewById(R.id.startNavigationButton);


        // TimerButton to open TimerActivity
        Button timerButton = findViewById(R.id.timerButton);
        timerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, TimerActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set initial location to Paris by default
        LatLng paris = new LatLng(48.85, 2.35);
        mMap.addMarker(new MarkerOptions().position(paris).title("Marker in Paris"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(paris, 10));

        // Enable "My Location" button if permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Permission de localisation refusée.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                mMap.addMarker(new MarkerOptions().position(currentLocation).title("Votre position actuelle"));
            } else {
                Toast.makeText(this, "Impossible de récupérer la localisation.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
