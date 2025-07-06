package com.example.hydraware;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;
import java.util.List;

public class PhChartFragment extends Fragment {
    private LineChart lineChart;
    private PhViewModel viewModel;
    private String lastPhState = "Normal";
    private String lastTempState = "Fría/Ambiental";
    private static final String CHANNEL_ID = "ph_notifications";
    private long lastNotificationTime = 0;
    private static final long NOTIFICATION_INTERVAL_MS = 5000; // 5 segundos

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ph_chart, container, false);
        lineChart = view.findViewById(R.id.lineChart);
        setupChart();
        createNotificationChannel();
        viewModel = new ViewModelProvider(requireActivity()).get(PhViewModel.class);
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            updateChart(events);
            if (!events.isEmpty()) {
                PhEvent last = events.get(events.size() - 1);
                boolean phAlert = !"Normal".equals(last.phState) && "Normal".equals(lastPhState);
                boolean tempAlert = !"Fría/Ambiental".equals(last.tempState) && "Fría/Ambiental".equals(lastTempState);
                if (phAlert || tempAlert) {
                    sendAlertNotification(last);
                }
                lastPhState = last.phState;
                lastTempState = last.tempState;
            }
        });
        return view;
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawGridBackground(false);
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
        leftAxis.setDrawGridLines(true);
        xAxis.setDrawGridLines(false);
    }

    private void updateChart(List<PhEvent> events) {
        List<Entry> phEntries = new ArrayList<>();
        List<Entry> tempEntries = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            phEntries.add(new Entry(i, events.get(i).phValue));
            tempEntries.add(new Entry(i, events.get(i).tempValue));
        }
        LineDataSet phDataSet = new LineDataSet(phEntries, "pH");
        phDataSet.setColor(0xFF43A047);
        phDataSet.setCircleColor(0xFF43A047);
        phDataSet.setLineWidth(2f);
        phDataSet.setCircleRadius(4f);
        phDataSet.setDrawValues(false);
        LineDataSet tempDataSet = new LineDataSet(tempEntries, "Temperatura");
        tempDataSet.setColor(0xFF448AFF);
        tempDataSet.setCircleColor(0xFF448AFF);
        tempDataSet.setLineWidth(2f);
        tempDataSet.setCircleRadius(4f);
        tempDataSet.setDrawValues(false);
        LineData data = new LineData(phDataSet, tempDataSet);
        lineChart.setData(data);
        lineChart.invalidate();
    }

    private void sendAlertNotification(PhEvent event) {
        long now = System.currentTimeMillis();
        if (now - lastNotificationTime < NOTIFICATION_INTERVAL_MS) return;
        lastNotificationTime = now;
        try {
            requireActivity().runOnUiThread(() -> {
                try {
                    StringBuilder message = new StringBuilder();
                    boolean vibrate = false;
                    int iconRes = R.drawable.ic_ph_normal;
                    if (!"Normal".equals(event.phState) || !"Fría/Ambiental".equals(event.tempState)) {
                        vibrate = true;
                    }
                    // Icono personalizado según estado
                    if ("Bajo".equals(event.phState) || "Baja".equals(event.tempState)) {
                        iconRes = R.drawable.ic_notification_exclamation;
                    } else if ("Alto".equals(event.phState) || "Alta".equals(event.tempState)) {
                        iconRes = R.drawable.ic_notification_warning;
                    }
                    message.append("pH: ").append(String.format("%.2f", event.phValue)).append(" (" + event.phState + ")\n");
                    message.append("Temp: ").append(String.format("%.1f", event.tempValue)).append("°C (" + event.tempState + ")");
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                        .setSmallIcon(iconRes)
                        .setContentTitle("¡Alerta de Agua!")
                        .setContentText(message.toString())
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message.toString()))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);
                    NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify((int) System.currentTimeMillis(), builder.build());
                    if (vibrate) {
                        Vibrator vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null && vibrator.hasVibrator()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                vibrator.vibrate(500);
                            }
                        }
                    }
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