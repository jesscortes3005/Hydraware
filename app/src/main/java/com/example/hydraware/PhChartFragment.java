package com.example.hydraware;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;
import java.util.List;

public class PhChartFragment extends Fragment {
    private PhGaugeView phGaugeView;
    private PhViewModel viewModel;
    private String lastPhState = "Normal";
    private String lastTempState = "Fría/Ambiental";
    private static final String CHANNEL_ID = "ph_notifications";
    private long lastNotificationTime = 0;
    private static final long NOTIFICATION_INTERVAL_MS = 120000; // 2 minutos
    private Handler handler = new Handler();
    private Runnable notificationRunnable;
    private PhEvent lastEvent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ph_chart, container, false);
        phGaugeView = view.findViewById(R.id.phGaugeView);
        createNotificationChannel();
        viewModel = new ViewModelProvider(requireActivity()).get(PhViewModel.class);
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (!events.isEmpty()) {
                lastEvent = events.get(events.size() - 1);
                phGaugeView.setPhValue(lastEvent.phValue);
            }
        });
        notificationRunnable = new Runnable() {
            @Override
            public void run() {
                if (lastEvent != null) {
                    sendPhNotification(lastEvent);
                }
                handler.postDelayed(this, NOTIFICATION_INTERVAL_MS);
            }
        };
        handler.postDelayed(notificationRunnable, NOTIFICATION_INTERVAL_MS);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(notificationRunnable);
    }

    private void sendPhNotification(PhEvent event) {
        try {
            requireActivity().runOnUiThread(() -> {
                try {
                    String message = "pH actual: " + String.format("%.2f", event.phValue);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_ph_normal)
                        .setContentTitle("Estado del pH")
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);
                    NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify((int) System.currentTimeMillis(), builder.build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notificaciones de pH y Temperatura";
            String description = "Alertas automáticas sobre el estado del pH y la temperatura";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
} 