package com.example.wake_map;

import android.Manifest;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ResultsActivity extends AppCompatActivity {

    private WebView mapWebView;
    private LatLng oneStopAway = null;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private MediaPlayer mediaPlayer;

    // Nouvelle variable pour stocker le temps de trajet
    private int temps_trajet = 0; // Temps en secondes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.results);

        // Initialize WebView
        mapWebView = findViewById(R.id.mapWebView);

        // Configure WebView settings
        WebSettings webSettings = mapWebView.getSettings();
        webSettings.setJavaScriptEnabled(true); // Enable JavaScript
        webSettings.setGeolocationEnabled(true); // Enable Geolocation

        // Set WebViewClient to handle URLs
        mapWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("intent://")) {
                    try {
                        Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                            return true;
                        }
                    } catch (Exception e) {
                        Toast.makeText(ResultsActivity.this, "Unable to handle intent.", Toast.LENGTH_SHORT).show();
                    }
                    return true; // Prevent WebView from trying to load intent
                } else if (url.startsWith("https://")) {
                    view.loadUrl(url);
                    return true;
                }
                return false; // Let WebView handle other URLs
            }
        });

        // Initialize location provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Get destination and origin from Intent
        String origin = getIntent().getStringExtra("origin");
        String destination = getIntent().getStringExtra("destination");

        if (destination != null && origin != null) {
            String googleMapsUrl = "https://www.google.com/maps/dir/?api=1" +
                    "&origin=" + origin +
                    "&destination=" + destination +
                    "&travelmode=transit";
            mapWebView.loadUrl(googleMapsUrl);

            // Fetch directions to track proximity
            fetchDirections(origin, destination);
        } else {
            Toast.makeText(this, "Origin or destination not provided.", Toast.LENGTH_SHORT).show();
        }

        mapWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Une fois que la page est chargée, ouvrir TimerActivity
                if (url.contains("google.com/maps")) { // Vérifie si c'est bien la page de Google Maps
                    navigateToTimerActivity();
                }
            }
        });

    }

    private void fetchDirections(String origin, String destination) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=" + origin +
                        "&destination=" + destination +
                        "&mode=transit" +
                        "&alternatives=true" +
                        "&key=AIzaSyDXoUy9HwwGL_T6ucGgyLNEVg_V-6v45ok"; // Replace with your API key

                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                Log.d("FetchDirections", "Temps de trajet avant requete (en secondes) : " + temps_trajet);

                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseBody);

                    JSONArray routes = jsonObject.getJSONArray("routes");
                    if (routes.length() > 0) {
                        JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");

                        if (legs.length() > 0) {
                            JSONObject firstLeg = legs.getJSONObject(0);

                            // Récupérer le temps de trajet en secondes et le stocker dans la variable temps_trajet
                            temps_trajet = firstLeg.getJSONObject("duration").getInt("value");
                            String durationText = firstLeg.getJSONObject("duration").getString("text"); // Format texte
                            Log.d("FetchDirections", "Temps de trajet (en secondes) : " + temps_trajet);
                            Log.d("FetchDirections", "Réponse brute de l'API : " + responseBody);
                            String responseBody12 = response.body().string();
                            Log.d("FetchDirections", "Réponse brute de l'API : " + responseBody12);
                            Log.d("FetchDirections", "Origin : " + origin + ", Destination : " + destination);




                            JSONArray steps = firstLeg.getJSONArray("steps");
                            if (steps.length() > 1) {
                                JSONObject lastStep = steps.getJSONObject(steps.length() - 2); // N-1 step
                                double lat = lastStep.getJSONObject("end_location").getDouble("lat");
                                double lng = lastStep.getJSONObject("end_location").getDouble("lng");
                                String stopName = lastStep.getString("html_instructions").replaceAll("<[^>]*>", ""); // Nettoyer les balises HTML
                                oneStopAway = new LatLng(lat, lng);

                                // Ajouter ici le code optionnel pour une Toast, une alerte et la navigation vers TimerActivity
                                runOnUiThread(() -> {
                                    checkLocationPermission();
                                    showAlertToUser(stopName);

                                    // Afficher une Toast pour confirmer les données
                                    Toast.makeText(ResultsActivity.this,
                                            "Temps de trajet : " + durationText,
                                            Toast.LENGTH_LONG).show();

                                    // Naviguer vers TimerActivity après récupération des données
                                    navigateToTimerActivity();
                                });
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void showAlertToUser(String stopName) {
        String message = "Vous serez réveillé à l'arrêt \"" + stopName + "\"";
        TextView stopAlertMessage = findViewById(R.id.stopAlertMessage);
        stopAlertMessage.setText(message);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startMonitoringProximity();
        }
    }

    private void startMonitoringProximity() {
        if (oneStopAway == null) return;

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        float[] distance = new float[1];
                        Location.distanceBetween(
                                location.getLatitude(),
                                location.getLongitude(),
                                oneStopAway.latitude,
                                oneStopAway.longitude,
                                distance
                        );

                        if (distance[0] < 100) { // Within 100 meters
                            playAlarm();
                            fusedLocationProviderClient.removeLocationUpdates(this);
                        }
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void playAlarm() {
        Toast.makeText(this, "You are one stop away from your destination!", Toast.LENGTH_LONG).show();
        try {
            Uri alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmTone == null) {
                alarmTone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            mediaPlayer = MediaPlayer.create(this, alarmTone);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } catch (Exception e) {
            Toast.makeText(this, "Error playing alarm sound.", Toast.LENGTH_SHORT).show();
        }
    }
    private void navigateToTimerActivity() {
        Log.d("FetchDirections", "Temps de trajet dans la fonction navigateToTimerActivity (en secondes) : " + temps_trajet);
        temps_trajet -= 300; // Soustraire 5 minutes à temps_trajet
        Log.d("FetchDirections", "Temps de trajet dans la fonction navigateToTimerActivity (en secondes) moins 5 mins: " + temps_trajet);
        Intent intent = new Intent(ResultsActivity.this, TimerActivity.class);
        intent.putExtra("temps_trajet", temps_trajet); // Passer la valeur de temps_trajet
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
