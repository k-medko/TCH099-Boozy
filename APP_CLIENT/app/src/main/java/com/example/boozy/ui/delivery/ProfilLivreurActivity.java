package com.example.boozy.ui.delivery;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.data.model.UtilisateurManager;
import com.example.boozy.ui.auth.AuthChoiceActivity;

public class ProfilLivreurActivity extends AppCompatActivity {

    TextView nomText, prenomText, telephoneText, emailText;
    TextView btnDeconnexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_livreur);

        setupFullScreen();
        initializeViews();
        loadLivreurData();

        findViewById(R.id.btnDeconnexion).setOnClickListener(v -> logout());

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void initializeViews() {
        nomText = findViewById(R.id.nomText);
        prenomText = findViewById(R.id.prenomText);
        telephoneText = findViewById(R.id.telephoneText);
        emailText = findViewById(R.id.emailText);
        btnDeconnexion = findViewById(R.id.btnDeconnexion);
    }

    private void loadLivreurData() {
        UtilisateurManager userManager = UtilisateurManager.getInstance(this);

        nomText.setText(userManager.getNom());
        prenomText.setText(userManager.getPrenom());
        telephoneText.setText(userManager.getNumTel());
        emailText.setText(userManager.getEmail());
    }

    private void setupFullScreen() {
        Window window = getWindow();
        window.setNavigationBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private void logout() {
        UtilisateurManager.getInstance(getApplicationContext()).logout();
        Intent intent = new Intent(this, AuthChoiceActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
