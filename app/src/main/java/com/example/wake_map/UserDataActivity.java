package com.example.wake_map;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class UserDataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);

        // Configurer la Toolbar pour un retour arrière
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish()); // Retour à SettingsActivity
    }
}
