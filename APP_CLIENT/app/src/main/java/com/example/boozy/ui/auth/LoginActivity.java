package com.example.boozy.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.ui.client.ClientHomeActivity;
import com.example.boozy.ui.delivery.LivreurHomeActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_login);
        initializeViews();
    }

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

    private void initializeViews() {
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        buttonLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Veuillez remplir tous les champs");
            return;
        }

        // APPEL À L'API ICI
        // Faire un appel à l'API pour vérifier l'utilisateur (client ou livreur).
        // En fonction de la réponse, rediriger l'utilisateur vers ClientHomeActivity ou LivreurHomeActivity.
    }

    private void openActivity(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
        finish();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}
