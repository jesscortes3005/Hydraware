package com.example.hydraware;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class PhGaugeView extends View {
    private static final int[] PH_COLORS = {
            Color.parseColor("#F44336"), // 0-1 rojo
            Color.parseColor("#FF5722"), // 2-3 naranja
            Color.parseColor("#FFEB3B"), // 4-5 amarillo
            Color.parseColor("#8BC34A"), // 6-7 verde
            Color.parseColor("#00BCD4"), // 8-9 cian
            Color.parseColor("#2196F3"), // 10-11 azul
            Color.parseColor("#673AB7")  // 12-14 violeta
    };
    private static final float[] PH_RANGES = {0, 2, 4, 6, 8, 10, 12, 14};
    private static final String[] PH_LABELS = {"ÁCIDO", "NEUTRO", "ALCALINO"};
    private float phValue = 7f;
    private float animatedPhValue = 7f;
    private Paint arcPaint, textPaint, indicatorPaint;
    private RectF arcRect;
    private Handler handler = new Handler();
    private Runnable phChanger;
    private int activeSegment = 3; // Por defecto NEUTRO
    private float indicatorScale = 1f;
    private int highlightColor = Color.parseColor("#FFFFFF");
    private float arcHighlight = 0f;
    private AnimatorSet stateAnimator;
    private boolean isFromTimer = false;

    public PhGaugeView(Context context) { super(context); init(); }
    public PhGaugeView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(); }
    public PhGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(); }

    private void init() {
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(50f);
        arcPaint.setStrokeCap(Paint.Cap.BUTT);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setColor(Color.BLACK);
        indicatorPaint.setStrokeWidth(10f);
        indicatorPaint.setStyle(Paint.Style.STROKE);
        arcRect = new RectF();
        // Cambia el pH automáticamente cada 2 minutos
        phChanger = new Runnable() {
            @Override
            public void run() {
                float next = phValue + 2f;
                if (next > 14f) next = 0f;
                isFromTimer = true;
                setPhValue(next);
                handler.postDelayed(this, 2 * 60 * 1000); // 2 minutos
            }
        };
        handler.postDelayed(phChanger, 2 * 60 * 1000);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(phChanger);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth();
        float h = getHeight();
        float size = Math.min(w, h) * 0.8f;
        float cx = w / 2f;
        float cy = h * 0.95f;
        float radius = size / 2f;
        arcRect.set(cx - radius, cy - radius, cx + radius, cy + radius);
        // Dibuja los arcos de colores
        for (int i = 0; i < PH_COLORS.length; i++) {
            int color = PH_COLORS[i];
            float startAngle = 180f * (PH_RANGES[i] / 14f);
            float sweepAngle = 180f * ((PH_RANGES[i+1] - PH_RANGES[i]) / 14f);
            float stroke = 50f;
            if (i == activeSegment) {
                // Resalta el segmento activo siempre
                color = getHighlightColor(PH_COLORS[i], 0.5f + 0.5f * arcHighlight);
                stroke = 60f + 40f * arcHighlight;
                if (arcHighlight > 0f) {
                    // Efecto de resplandor animado
                    color = blendColors(color, highlightColor, arcHighlight * 0.8f);
                }
            }
            arcPaint.setColor(color);
            arcPaint.setStrokeWidth(stroke);
            canvas.drawArc(arcRect, 180 + startAngle, sweepAngle, false, arcPaint);
        }
        // Dibuja los números
        textPaint.setTextSize(size * 0.07f);
        for (int i = 0; i <= 14; i++) {
            double angle = Math.PI * (1 + (i / 14.0));
            float tx = (float)(cx + (radius + 40) * Math.cos(angle));
            float ty = (float)(cy + (radius + 40) * Math.sin(angle) + 15);
            canvas.drawText(String.valueOf(i), tx, ty, textPaint);
        }
        // Dibuja etiquetas ÁCIDO, NEUTRO, ALCALINO
        textPaint.setTextSize(size * 0.09f);
        canvas.drawText(PH_LABELS[0], cx - radius * 0.7f, cy - radius * 0.2f, textPaint);
        canvas.drawText(PH_LABELS[1], cx, cy - radius * 1.05f, textPaint);
        canvas.drawText(PH_LABELS[2], cx + radius * 0.7f, cy - radius * 0.2f, textPaint);
        // Dibuja el indicador con rebote
        float angle = (float) Math.PI * (1 + (animatedPhValue / 14f));
        float ix = (float)(cx + radius * indicatorScale * Math.cos(angle));
        float iy = (float)(cy + radius * indicatorScale * Math.sin(angle));
        canvas.drawLine(cx, cy, ix, iy, indicatorPaint);
        // Dibuja el valor de pH grande en el centro
        textPaint.setTextSize(size * 0.25f);
        canvas.drawText("pH", cx, cy - radius * 0.3f, textPaint);
    }

    public void setPhValue(float value) {
        int newSegment = getSegmentForPh(value);
        boolean stateChanged = (newSegment != activeSegment);
        ValueAnimator indicatorAnim = ValueAnimator.ofFloat(animatedPhValue, value);
        indicatorAnim.setDuration(1000);
        indicatorAnim.addUpdateListener(animation -> {
            animatedPhValue = (float) animation.getAnimatedValue();
            invalidate();
        });
        indicatorAnim.start();
        this.phValue = value;
        // Solo animar si el cambio es automático (por temporizador)
        if (stateChanged && isFromTimer) {
            animateStateChange(newSegment);
        }
        activeSegment = newSegment;
        isFromTimer = false;
    }

    private int getSegmentForPh(float ph) {
        for (int i = 0; i < PH_RANGES.length - 1; i++) {
            if (ph >= PH_RANGES[i] && ph < PH_RANGES[i + 1]) return i;
        }
        return PH_RANGES.length - 2;
    }

    private void animateStateChange(int newSegment) {
        // Animación de brillo en el segmento activo
        ValueAnimator arcAnim = ValueAnimator.ofFloat(0f, 1f, 0f);
        arcAnim.setDuration(400);
        arcAnim.addUpdateListener(a -> {
            arcHighlight = (float) a.getAnimatedValue();
            invalidate();
        });
        // Animación de rebote en el indicador
        ValueAnimator scaleAnim = ValueAnimator.ofFloat(1f, 1.4f, 1f);
        scaleAnim.setDuration(400);
        scaleAnim.addUpdateListener(a -> {
            indicatorScale = (float) a.getAnimatedValue();
            invalidate();
        });
        stateAnimator = new AnimatorSet();
        stateAnimator.playTogether(arcAnim, scaleAnim);
        stateAnimator.start();
    }

    private int blendColors(int from, int to, float ratio) {
        return (Integer) new ArgbEvaluator().evaluate(ratio, from, to);
    }

    private int getHighlightColor(int baseColor, float intensity) {
        // Aumenta saturación y brillo del color base de forma más notoria
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[1] = 1f; // Máxima saturación
        hsv[2] = Math.min(1f, hsv[2] + 0.7f * intensity); // Mucho más brillo
        return Color.HSVToColor(hsv);
    }
} 