package com.example.hydraware;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class SplashActivity extends Activity {
    
    private TextView[] letters;
    private TextView subtitle;
    private int currentLetterIndex = 0;
    private Handler animationHandler;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        initializeViews();
        startAnimation();
    }
    
    private void initializeViews() {
        // Initialize all letter TextViews
        letters = new TextView[9];
        letters[0] = findViewById(R.id.letterH);
        letters[1] = findViewById(R.id.letterY);
        letters[2] = findViewById(R.id.letterD);
        letters[3] = findViewById(R.id.letterR1);
        letters[4] = findViewById(R.id.letterA1);
        letters[5] = findViewById(R.id.letterW);
        letters[6] = findViewById(R.id.letterA2);
        letters[7] = findViewById(R.id.letterR2);
        letters[8] = findViewById(R.id.letterE);
        
        subtitle = findViewById(R.id.subtitle);
        animationHandler = new Handler();
    }
    
    private void startAnimation() {
        // Start the letter animation sequence after a short delay
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateNextLetter();
            }
        }, 500);
    }
    
    private void animateNextLetter() {
        if (currentLetterIndex < letters.length) {
            TextView currentLetter = letters[currentLetterIndex];
            
            // Create illumination animation
            illuminateLetter(currentLetter, new Runnable() {
                @Override
                public void run() {
                    currentLetterIndex++;
                    // Continue with next letter after a delay
                    animationHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animateNextLetter();
                        }
                    }, 200); // 200ms delay between letters
                }
            });
        } else {
            // All letters animated, show subtitle and then proceed to main activity
            showSubtitleAndProceed();
        }
    }
    
    private void illuminateLetter(TextView letter, Runnable onComplete) {
        // Color animation from dim to bright
        ValueAnimator colorAnimator = ValueAnimator.ofArgb(
            ContextCompat.getColor(this, R.color.letter_dim),
            ContextCompat.getColor(this, R.color.letter_bright)
        );
        
        colorAnimator.setDuration(300);
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                letter.setTextColor((Integer) animation.getAnimatedValue());
            }
        });
        
        // Scale animation for emphasis
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(letter, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(letter, "scaleY", 1f, 1.2f, 1f);
        scaleXAnimator.setDuration(300);
        scaleYAnimator.setDuration(300);
        
        // Glow effect (shadow radius animation)
        ValueAnimator glowAnimator = ValueAnimator.ofFloat(0f, 15f, 8f);
        glowAnimator.setDuration(300);
        glowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float shadowRadius = (Float) animation.getAnimatedValue();
                letter.setShadowLayer(shadowRadius, 0, 0, 
                    ContextCompat.getColor(SplashActivity.this, R.color.letter_glow));
            }
        });
        
        // Add animation completion listener
        colorAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        
        // Start all animations together
        colorAnimator.start();
        scaleXAnimator.start();
        scaleYAnimator.start();
        glowAnimator.start();
    }
    
    private void showSubtitleAndProceed() {
        // Animate subtitle fade in
        ObjectAnimator subtitleAnimator = ObjectAnimator.ofFloat(subtitle, "alpha", 0f, 1f);
        subtitleAnimator.setDuration(500);
        subtitleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Wait a bit more and then proceed to main activity
                animationHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        finish();
                        // Add a smooth transition
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                }, 1000);
            }
        });
        subtitleAnimator.start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
    }
} 