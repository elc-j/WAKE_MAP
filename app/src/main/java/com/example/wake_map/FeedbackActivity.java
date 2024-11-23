package com.example.wake_map;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class FeedbackActivity extends AppCompatActivity {

    // Déclaration des vues
    private RatingBar ratingBar;
    private Button btnSubmitFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Liaison des vues
        ratingBar = findViewById(R.id.ratingBar); // Vérifie l'ID exact
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback); // Vérifie l'ID exact

        // Configurer la Toolbar pour un retour arrière
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish()); // Retour à SettingsActivity

        // Gérer l'action du bouton de soumission
        btnSubmitFeedback.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            if (rating > 0) {
                Toast.makeText(this, "Merci pour votre avis : " + rating + " étoiles !", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Veuillez donner une note avant de soumettre.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
