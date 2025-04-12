package com.example.boozy.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.boozy.R;
import com.example.boozy.ui.auth.AuthChoiceActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFullScreen();
        setContentView(R.layout.activity_splash);

        ImageView gifView = findViewById(R.id.gifImageView);
        Glide.with(this).asGif().load(R.drawable.logo_gif).into(gifView);

        // Attendre 3 secondes puis lancer ClientHomeActivity
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, AuthChoiceActivity.class));
            finish();
        }, 3000);
    }

    private void setupFullScreen() {
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }
}
