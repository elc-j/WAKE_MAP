package com.example.wake_map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private ImageView imgProfile;
    private ImageView avatarMale, avatarFemale;
    private ImageView iconEditProfile;
    private Button btnPrivacyPolicy, btnUserData, btnGiveFeedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Configurer la Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Lier les vues
        imgProfile = findViewById(R.id.imgProfile);
        avatarMale = findViewById(R.id.avatarMale);
        avatarFemale = findViewById(R.id.avatarFemale);
        iconEditProfile = findViewById(R.id.iconEditProfile);

        btnPrivacyPolicy = findViewById(R.id.btnPrivacyPolicy);
        btnUserData = findViewById(R.id.btnUserData);
        btnGiveFeedback = findViewById(R.id.btnGiveFeedback);

        // Vérifier les liaisons
        if (imgProfile == null || avatarMale == null || avatarFemale == null || iconEditProfile == null ||
                btnPrivacyPolicy == null || btnUserData == null || btnGiveFeedback == null) {
            Log.e("SettingsActivity", "Erreur de liaison des vues");
            Toast.makeText(this, "Erreur d'initialisation des composants", Toast.LENGTH_SHORT).show();
            return;
        }

        // Masquer les avatars au départ
        avatarMale.setVisibility(View.GONE);
        avatarFemale.setVisibility(View.GONE);

        // Charger l'avatar sauvegardé
        loadSavedAvatar();

        // Gérer le clic sur l'icône de modification
        iconEditProfile.setOnClickListener(v -> {
            Log.d("SettingsActivity", "Icône Modifier cliquée");
            avatarMale.setVisibility(View.VISIBLE);
            avatarFemale.setVisibility(View.VISIBLE);
        });

        // Gérer le clic sur l'avatar Homme
        avatarMale.setOnClickListener(v -> {
            imgProfile.setImageResource(R.drawable.ic_avatar_male);
            saveAvatarChoice("male");
            hideAvatarOptions();
            Toast.makeText(this, "Avatar Homme sélectionné", Toast.LENGTH_SHORT).show();
        });

        // Gérer le clic sur l'avatar Femme
        avatarFemale.setOnClickListener(v -> {
            imgProfile.setImageResource(R.drawable.ic_avatar_female);
            saveAvatarChoice("female");
            hideAvatarOptions();
            Toast.makeText(this, "Avatar Femme sélectionné", Toast.LENGTH_SHORT).show();
        });

        // Configurer les clics sur les boutons principaux
        btnPrivacyPolicy.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });

        btnUserData.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, UserDataActivity.class);
            startActivity(intent);
        });

        btnGiveFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Masquer les options des avatars.
     */
    private void hideAvatarOptions() {
        avatarMale.setVisibility(View.GONE);
        avatarFemale.setVisibility(View.GONE);
    }

    /**
     * Sauvegarder le choix de l'avatar dans SharedPreferences.
     */
    private void saveAvatarChoice(String avatar) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedAvatar", avatar);
        editor.apply();
    }

    /**
     * Charger l'avatar sauvegardé.
     */
    private void loadSavedAvatar() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE);
        String selectedAvatar = sharedPreferences.getString("selectedAvatar", "default");

        if ("male".equals(selectedAvatar)) {
            imgProfile.setImageResource(R.drawable.ic_avatar_male);
        } else if ("female".equals(selectedAvatar)) {
            imgProfile.setImageResource(R.drawable.ic_avatar_female);
        } else {
            imgProfile.setImageResource(R.drawable.ic_profile_default);
        }
    }
}
