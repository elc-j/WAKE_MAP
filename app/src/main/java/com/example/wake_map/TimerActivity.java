package com.example.wake_map;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TimerActivity extends AppCompatActivity {

    private EditText inputHours, inputMinutes, inputSeconds;
    private Button btnStartTimer, btnStopTimer;
    private TextView timerStatus;
    private CountDownTimer countDownTimer;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer); // Assurez-vous que "timer.xml" existe bien dans res/layout

        // Initialisation des vues
        inputHours = findViewById(R.id.inputHours);
        inputMinutes = findViewById(R.id.inputMinutes);
        inputSeconds = findViewById(R.id.inputSeconds);
        btnStartTimer = findViewById(R.id.btnStartTimer);
        btnStopTimer = findViewById(R.id.btnStopTimer); // Bouton pour arrêter le minuteur
        timerStatus = findViewById(R.id.timerStatus);

        // Configurer la Toolbar pour un retour arrière
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish()); // Retour arrière

        // Initialiser le MediaPlayer avec un son d'alarme
        mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound); // Vérifiez que "alarm_sound.mp3" est dans res/raw

        // Action pour lancer le minuteur
        btnStartTimer.setOnClickListener(v -> startTimer());

        // Action pour arrêter le minuteur
        btnStopTimer.setOnClickListener(v -> stopTimer());
    }

    private void startTimer() {
        // Récupérer les entrées utilisateur
        int hours = parseInput(inputHours);
        int minutes = parseInput(inputMinutes);
        int seconds = parseInput(inputSeconds);

        // Calculer le total du temps en millisecondes
        int totalTimeInMillis = (hours * 3600 + minutes * 60 + seconds) * 1000;

        if (totalTimeInMillis <= 0) {
            Toast.makeText(this, "Veuillez entrer un temps valide !", Toast.LENGTH_SHORT).show();
            return;
        }

        // Définir le message initial
        timerStatus.setText("Minuteur en cours...");

        // Lancer le CountDownTimer
        countDownTimer = new CountDownTimer(totalTimeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Mettre à jour l'affichage du temps restant
                int remainingSeconds = (int) (millisUntilFinished / 1000);
                int hours = remainingSeconds / 3600;
                int minutes = (remainingSeconds % 3600) / 60;
                int seconds = remainingSeconds % 60;

                String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                timerStatus.setText("Temps restant : " + timeFormatted);
            }

            @Override
            public void onFinish() {
                // Minuteur terminé
                timerStatus.setText("Temps écoulé !");
                mediaPlayer.start(); // Jouer l'alarme
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel(); // Arrêter le minuteur
            timerStatus.setText("Minuteur arrêté."); // Mettre à jour le statut
        }
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop(); // Arrêter le son si l'alarme joue
            mediaPlayer.prepareAsync(); // Préparer à nouveau le MediaPlayer
        }
    }

    private int parseInput(EditText editText) {
        String input = editText.getText().toString();
        return input.isEmpty() ? 0 : Integer.parseInt(input);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Libérer les ressources du MediaPlayer
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
