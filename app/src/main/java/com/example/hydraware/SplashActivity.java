package com.example.hydraware;

import android.app.Activity;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LinearLayout letters = findViewById(R.id.lettersContainer);
        if (letters != null) {
            int letterCount = letters.getChildCount();
            int offColor = ContextCompat.getColor(this, R.color.colorTextSecondary);
            int onColor = ContextCompat.getColor(this, R.color.colorForeground);

            long perLetterDelayMs = 120;
            long perLetterDurationMs = 260;

            long totalAnimMs = 0;
            for (int i = 0; i < letterCount; i++) {
                View child = letters.getChildAt(i);
                if (!(child instanceof TextView)) continue;
                TextView tv = (TextView) child;

                tv.setAlpha(0.25f);
                tv.setTextColor(offColor);

                ObjectAnimator alphaIn = ObjectAnimator.ofFloat(tv, View.ALPHA, 0.25f, 1f);
                alphaIn.setDuration(perLetterDurationMs);
                alphaIn.setStartDelay(i * perLetterDelayMs);

                ValueAnimator colorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), offColor, onColor);
                colorAnim.addUpdateListener(animation -> tv.setTextColor((int) animation.getAnimatedValue()));
                colorAnim.setDuration(perLetterDurationMs);
                colorAnim.setStartDelay(i * perLetterDelayMs);

                AnimatorSet set = new AnimatorSet();
                set.playTogether(alphaIn, colorAnim);
                set.start();

                totalAnimMs = i * perLetterDelayMs + perLetterDurationMs;
            }

            long goDelay = totalAnimMs + 400; // pequeña pausa final
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }, goDelay);
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }, 1500);
        }
    }
} 