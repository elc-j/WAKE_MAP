package com.example.wake_map;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class TimerActivity extends AppCompatActivity {

    private EditText inputHours, inputMinutes, inputSeconds;
    private Button btnStartTimer, btnStopTimer, btnHome;
    private TextView timerStatus;
    private MediaPlayer mediaPlayer;

    // Variables pour le TimerService
    private TimerService timerService;
    private boolean isServiceBound = false;

    // Connexion au service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) service;
            timerService = binder.getService();
            isServiceBound = true;

            // Récupérer l'état actuel du minuteur
            long remainingTime = timerService.getRemainingTime();
            if (remainingTime > 0) {
                resumeTimer(remainingTime);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer);

        // Initialisation des vues
        inputHours = findViewById(R.id.inputHours);
        inputMinutes = findViewById(R.id.inputMinutes);
        inputSeconds = findViewById(R.id.inputSeconds);
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnStopTimer = findViewById(R.id.btnStopTimer);

        timerStatus = findViewById(R.id.timerStatus);

        // Configurer la Toolbar pour un retour arrière
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialiser le MediaPlayer avec un son d'alarme
        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound);

        // Vérifier si un temps de trajet est passé depuis ResultsActivity
        int tempsTrajet = getIntent().getIntExtra("temps_trajet", 0);
        if (tempsTrajet > 0) {
            // Convertir le temps en heures, minutes et secondes
            int hours = tempsTrajet / 3600;
            int minutes = (tempsTrajet % 3600) / 60;
            int seconds = tempsTrajet % 60;

            // Pré-remplir les champs d'entrée
            inputHours.setText(String.valueOf(hours));
            inputMinutes.setText(String.valueOf(minutes));
            inputSeconds.setText(String.valueOf(seconds));

            // Démarrer automatiquement le timer via le service
            startTimerInService(hours, minutes, seconds);
        }

        // Action pour lancer le minuteur manuellement
        btnStartTimer.setOnClickListener(v -> {
            int hours = parseInput(inputHours);
            int minutes = parseInput(inputMinutes);
            int seconds = parseInput(inputSeconds);
            startTimerInService(hours, minutes, seconds);
        });

        // Action pour arrêter le minuteur
        btnStopTimer.setOnClickListener(v -> stopTimerInService());



    }

    private void startTimerInService(int hours, int minutes, int seconds) {
        long durationInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000;

        if (durationInMillis <= 0) {
            Toast.makeText(this, "Veuillez entrer un temps valide !", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, TimerService.class);
        startService(intent);

        if (isServiceBound) {
            timerService.startTimer(durationInMillis, new TimerService.TimerListener() {
                @Override
                public void onTick(long millisUntilFinished) {
                    updateTimerUI(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    timerStatus.setText("Temps écoulé !");
                    mediaPlayer.start();
                }
            });
        }
    }

    private void stopTimerInService() {
        if (isServiceBound) {
            timerService.stopTimer();
            timerService.stopMusic();
        }
        stopService(new Intent(this, TimerService.class));
        timerStatus.setText("Minuteur arrêté.");
    }



    private void updateTimerUI(long millisUntilFinished) {
        int remainingSeconds = (int) (millisUntilFinished / 1000);
        int hours = remainingSeconds / 3600;
        int minutes = (remainingSeconds % 3600) / 60;
        int seconds = remainingSeconds % 60;

        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timerStatus.setText("Temps restant : " + timeFormatted);
    }

    private void resumeTimer(long remainingTime) {
        updateTimerUI(remainingTime);
    }

    private int parseInput(EditText editText) {
        String input = editText.getText().toString();
        return input.isEmpty() ? 0 : Integer.parseInt(input);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TimerService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}

