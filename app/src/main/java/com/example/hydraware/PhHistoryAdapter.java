package com.example.hydraware;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhHistoryAdapter extends RecyclerView.Adapter<PhHistoryAdapter.ViewHolder> {
    private List<PhEvent> events;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

    public PhHistoryAdapter(List<PhEvent> events) {
        this.events = events;
    }

    public void updateData(List<PhEvent> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ph_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PhEvent event = events.get(position);
        holder.textPhValue.setText(String.format(Locale.getDefault(), "pH: %.2f (%s)", event.phValue, event.phState));
        holder.textTempValue.setText(String.format(Locale.getDefault(), "Temp: %.1f°C (%s)", event.tempValue, event.tempState));
        holder.textDate.setText(dateFormat.format(event.timestamp));
        // Iconos de estado
        if ("Bajo".equals(event.phState)) holder.phIcon.setImageResource(R.drawable.ic_ph_acid);
        else if ("Alto".equals(event.phState)) holder.phIcon.setImageResource(R.drawable.ic_ph_basic);
        else holder.phIcon.setImageResource(R.drawable.ic_ph_normal);
        if ("Fría/Ambiental".equals(event.tempState)) holder.tempIcon.setImageResource(R.drawable.ic_temp_cold);
        else if ("Caliente".equals(event.tempState)) holder.tempIcon.setImageResource(R.drawable.ic_temp_hot);
        else holder.tempIcon.setImageResource(R.drawable.ic_temp_normal);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView textPhValue, textTempValue, textDate;
        ImageView phIcon, tempIcon;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            card = (MaterialCardView) itemView;
            textPhValue = itemView.findViewById(R.id.textPhValue);
            textTempValue = itemView.findViewById(R.id.textTempValue);
            textDate = itemView.findViewById(R.id.textDate);
            phIcon = itemView.findViewById(R.id.phIcon);
            tempIcon = itemView.findViewById(R.id.tempIcon);
        }
    }
} 