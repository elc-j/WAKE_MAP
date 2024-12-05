
package com.example.wake_map;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;

public class TimerService extends Service {

    private final IBinder binder = new TimerBinder();
    private CountDownTimer countDownTimer;
    private long remainingTimeInMillis;
    private TimerListener listener;
    private MediaPlayer mediaPlayer;

    public class TimerBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void startTimer(long durationInMillis, TimerListener listener) {
        this.listener = listener;
        remainingTimeInMillis = durationInMillis;

        countDownTimer = new CountDownTimer(durationInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTimeInMillis = millisUntilFinished;
                if (listener != null) {
                    listener.onTick(millisUntilFinished);
                }
            }

            @Override
            public void onFinish() {
                remainingTimeInMillis = 0;
                if (listener != null) {
                    listener.onFinish();
                }
                stopSelf(); // Arrêter le service lorsque le minuteur se termine

                // Démarrer la musique lorsqu'on atteint la fin du timer
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        }.start();
    }

    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopSelf();
    }

    public void startMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.alert_sound); // Assurez-vous que le fichier "alert_sound.mp3" est présent dans res/raw
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop(); // Arrêter la musique si elle est en cours de lecture
            mediaPlayer.release(); // Libérer les ressources pour éviter les fuites mémoire
            mediaPlayer = null; // Réinitialiser l'objet MediaPlayer
        }
    }

    public long getRemainingTime() {
        return remainingTimeInMillis;
    }

    public interface TimerListener {
        void onTick(long millisUntilFinished);
        void onFinish();
    }
}