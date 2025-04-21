package com.example.boozy.ui.home;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boozy.R;
import com.example.boozy.ui.auth.AuthChoiceActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TEXT_TO_TYPE = "BOOZY";
    private static final int LETTER_DELAY_MS = 150;
    private static final int TOTAL_SPLASH_TIME_MS = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFullScreen();
        setContentView(R.layout.activity_splash);

        TextView textSplash = findViewById(R.id.splashText);
        Typeface customFont = Typeface.createFromAsset(getAssets(), "fonts/SpecialGothicExpandedOne-Regular.ttf");
        textSplash.setTypeface(customFont);
        animateTyping(textSplash, TEXT_TO_TYPE, LETTER_DELAY_MS);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, AuthChoiceActivity.class));
            finish();
        }, TOTAL_SPLASH_TIME_MS);
    }

    private void animateTyping(TextView textView, String text, long delay) {
        final int[] index = {0};
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] <= text.length()) {
                    textView.setText(text.substring(0, index[0]));
                    index[0]++;
                    handler.postDelayed(this, delay);
                }
            }
        };
        handler.post(runnable);
    }

    private void setupFullScreen() {
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }
}
