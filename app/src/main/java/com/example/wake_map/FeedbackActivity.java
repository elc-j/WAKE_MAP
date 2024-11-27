package com.example.wake_map;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FeedbackActivity extends AppCompatActivity {

    // Déclaration des vues
    private RatingBar ratingBar;
    private EditText etFeedback;
    private Button btnSubmitFeedback;

    // Référence à Firestore
    private FirebaseFirestore firestore;
    private CollectionReference feedbackRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Initialisation de Firestore
        firestore = FirebaseFirestore.getInstance();
        feedbackRef = firestore.collection("feedbacks");

        // Liaison des vues
        ratingBar = findViewById(R.id.ratingBar);
        etFeedback = findViewById(R.id.etFeedback);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);

        // Configurer la Toolbar pour un retour arrière
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Gérer l'action du bouton de soumission
        btnSubmitFeedback.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String feedback = etFeedback.getText().toString().trim();

            if (rating == 0 || feedback.isEmpty()) {
                Toast.makeText(this, "Veuillez donner une note et un avis.", Toast.LENGTH_SHORT).show();
            } else {
                sendFeedbackToFirestore(rating, feedback);
            }
        });
    }

    private void sendFeedbackToFirestore(float rating, String feedback) {
        // Créer un objet pour stocker les avis
        Feedback feedbackData = new Feedback(rating, feedback);

        // Envoyer les données dans Firestore
        feedbackRef.add(feedbackData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Merci pour votre avis !", Toast.LENGTH_SHORT).show();
                    etFeedback.setText("");
                    ratingBar.setRating(0);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de l'envoi de l'avis.", Toast.LENGTH_SHORT).show();
                });
    }

    // Classe pour stocker les avis
    public static class Feedback {
        public float rating;
        public String feedback;

        public Feedback() {
            // Nécessaire pour Firestore
        }

        public Feedback(float rating, String feedback) {
            this.rating = rating;
            this.feedback = feedback;
        }
    }
}
