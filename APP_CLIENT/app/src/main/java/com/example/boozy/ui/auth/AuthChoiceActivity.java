package com.example.boozy.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;

public class AuthChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_auth_choice);
        setupClickListeners();
    }

    /**
     * Configure l'écran en plein écran avec barre de navigation transparente
     */
    private void setFullScreen() {
        Window window = getWindow();
        window.setNavigationBarColor(Color.TRANSPARENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    /**
     * Initialise les clics sur les boutons
     */
    private void setupClickListeners() {
        findViewById(R.id.buttonInscrire).setOnClickListener(v -> openActivity(SelectionProfilActivity.class));
        findViewById(R.id.buttonConnexion).setOnClickListener(v -> openActivity(LoginActivity.class));
    }

    /**
     * Ouvre une nouvelle activité
     */
    private void openActivity(Class<?> activityClass) {
        startActivity(new Intent(AuthChoiceActivity.this, activityClass));
    }
}
