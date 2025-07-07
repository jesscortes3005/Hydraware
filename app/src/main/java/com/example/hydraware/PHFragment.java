package com.example.hydraware;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.highlight.Highlight;
import android.content.Context;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.media.RingtoneManager;
import android.net.Uri;
import com.example.hydraware.PhGaugeView;
import android.widget.LinearLayout;

public class PHFragment extends Fragment {
    private String lastPhState = "";
    private String lastTempState = "";
    private static final String CHANNEL_ID = "ph_notifications";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ph, container, false);
        // Tarjeta principal
        TextView mainPhValue = view.findViewById(R.id.mainPhValue);
        TextView mainTempValue = view.findViewById(R.id.mainTempValue);
        // Historial (contenedor)
        LinearLayout histContainer = (LinearLayout) ((ViewGroup) view).findViewById(R.id.histContainer);
        // Gráfica
        LineChart lineChart = view.findViewById(R.id.lineChart);
        // Gauge de pH
        PhGaugeView phGaugeView = view.findViewById(R.id.phGaugeView);
        // Configuración moderna del LineChart
        lineChart.setBackgroundColor(0xFF1A1026);
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);
        lineChart.setExtraOffsets(10, 10, 10, 10);
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(0xFFFFFFFF);
        legend.setTextSize(16f);
        legend.setFormSize(18f);
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(0xFFFFFFFF);
        xAxis.setGridColor(0x22FFFFFF);
        xAxis.setTextSize(14f);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(true);
        YAxis leftAxis = lineChart.getAxisLeft();
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
        leftAxis.setTextColor(0xFFFFFFFF);
        leftAxis.setGridColor(0x22FFFFFF);
        leftAxis.setTextSize(14f);
        leftAxis.setDrawAxisLine(true);
        leftAxis.setDrawGridLines(true);
        // ViewModel y datos simulados
        PhViewModel viewModel = new ViewModelProvider(requireActivity()).get(PhViewModel.class);
        createNotificationChannel();
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (!events.isEmpty()) {
                PhEvent last = events.get(events.size() - 1);
                // Tarjeta principal
                mainPhValue.setText(String.format("pH: %.2f", last.phValue));
                mainTempValue.setText(String.format("Temp: %.0f°C", last.tempValue));
                // Historial (últimos 3 valores)
                histContainer.removeAllViews();
                int count = Math.min(3, events.size());
                for (int i = 0; i < count; i++) {
                    PhEvent e = events.get(events.size() - 1 - i);
                    View histItem = inflater.inflate(R.layout.item_historial, histContainer, false);
                    TextView phValue = histItem.findViewById(R.id.histPhValue);
                    TextView phTime = histItem.findViewById(R.id.histPhTime);
                    TextView tempValue = histItem.findViewById(R.id.histTempValue);
                    TextView tempTime = histItem.findViewById(R.id.histTempTime);
                    ImageView histIcon = histItem.findViewById(R.id.histIcon);
                    ImageView histTempIcon = histItem.findViewById(R.id.histTempIcon);
                    phValue.setText(String.format("pH: %.2f", e.phValue));
                    tempValue.setText(String.format("Temp: %.0f°C", e.tempValue));
                    phTime.setText("Hace " + i + " min");
                    tempTime.setText("Hace " + i + " min");
                    // Icono según estado pH
                    if ("Bajo".equals(e.phState)) histIcon.setImageResource(R.drawable.ic_ph_acid);
                    else if ("Alto".equals(e.phState)) histIcon.setImageResource(R.drawable.ic_ph_basic);
                    else histIcon.setImageResource(R.drawable.ic_ph_normal);
                    // Icono según estado temperatura
                    if ("Baja".equals(e.tempState)) histTempIcon.setImageResource(R.drawable.ic_temp_cold);
                    else if ("Alta".equals(e.tempState)) histTempIcon.setImageResource(R.drawable.ic_temp_hot);
                    else histTempIcon.setImageResource(R.drawable.ic_temp_normal);
                    histContainer.addView(histItem);
                }
                // Gráfica
                List<Entry> phEntries = new ArrayList<>();
                List<Entry> tempEntries = new ArrayList<>();
                for (int i = 0; i < events.size(); i++) {
                    phEntries.add(new Entry(i, events.get(i).phValue));
                    tempEntries.add(new Entry(i, events.get(i).tempValue));
                }
                // Línea de pH: degradado de rosa a morado
                LineDataSet phDataSet = new LineDataSet(phEntries, "pH");
                phDataSet.setDrawCircles(false);
                phDataSet.setLineWidth(5f);
                phDataSet.setDrawValues(false);
                phDataSet.setMode(LineDataSet.Mode.LINEAR);
                int phStart = 0xFFFF4081; // Rosa
                int phEnd = 0xFF7C4DFF;   // Morado
                phDataSet.setGradientColor(phStart, phEnd);
                // Línea de temperatura: degradado de amarillo a verde
                LineDataSet tempDataSet = new LineDataSet(tempEntries, "Temperatura");
                tempDataSet.setDrawCircles(false);
                tempDataSet.setLineWidth(5f);
                tempDataSet.setDrawValues(false);
                tempDataSet.setMode(LineDataSet.Mode.LINEAR);
                int tempStart = 0xFFFFD600;
                int tempEnd = 0xFF00C853;
                tempDataSet.setGradientColor(tempStart, tempEnd);
                LineData data = new LineData(phDataSet, tempDataSet);
                lineChart.setData(data);
                lineChart.moveViewToX(events.size());
                lineChart.animateX(1000);
                lineChart.invalidate();
                // Notificación si cambia el estado de pH o temperatura
                if (!last.phState.equals(lastPhState) || !last.tempState.equals(lastTempState)) {
                    sendPhTempNotification(last);
                    lastPhState = last.phState;
                    lastTempState = last.tempState;
                }
            }
        });
        // Marcador flotante para mostrar valores actuales
        lineChart.setMarker(new CustomMarkerView(requireContext(), R.layout.marker_view));
        lineChart.setHighlightPerTapEnabled(true);
        lineChart.setHighlightPerDragEnabled(true);
        lineChart.setDrawMarkers(true);
        return view;
    }

    private static class PhTabsAdapter extends FragmentStateAdapter {
        public PhTabsAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }
        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) return new PhChartFragment();
            else return new PhHistoryFragment();
        }
        @Override
        public int getItemCount() {
            return 2;
        }
    }

    // Clase para el marcador flotante
    public static class CustomMarkerView extends MarkerView {
        private final TextView tvContent;
        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvContent = findViewById(R.id.tvContent);
        }
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            tvContent.setText(String.format("%.2f", e.getY()));
            super.refreshContent(e, highlight);
        }
        public int getXOffset(float xpos) { return -(getWidth() / 2); }
        public int getYOffset(float ypos) { return -getHeight(); }
    }

    // Métodos auxiliares para iconos y textos
    private int getPhIcon(String phState) {
        if ("Bajo".equals(phState)) return R.drawable.ic_ph_acid;
        if ("Alto".equals(phState)) return R.drawable.ic_ph_basic;
        return R.drawable.ic_ph_normal;
    }
    private String getPhStatusText(String phState) {
        if ("Bajo".equals(phState)) return "Ácido";
        if ("Alto".equals(phState)) return "Alcalino";
        return "Neutro";
    }
    private int getTempIcon(String tempState) {
        if ("Baja".equals(tempState)) return R.drawable.ic_temp_cold;
        if ("Alta".equals(tempState)) return R.drawable.ic_temp_hot;
        return R.drawable.ic_temp_normal;
    }
    private String getTempStatusText(String tempState) {
        if ("Baja".equals(tempState)) return "Fría";
        if ("Alta".equals(tempState)) return "Caliente";
        return "Ambiental";
    }

    private void sendPhTempNotification(PhEvent event) {
        String phMsg = "pH: " + String.format("%.2f", event.phValue) + " (" + getPhStatusText(event.phState) + ")";
        String tempMsg = "Temp: " + String.format("%.0f°C", event.tempValue) + " (" + getTempStatusText(event.tempState) + ")";
        String message = phMsg + "\n" + tempMsg;
        int iconRes = getPhIcon(event.phState);
        // Sonido personalizado
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle("Cambio de estado pH/Temperatura")
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri);
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
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