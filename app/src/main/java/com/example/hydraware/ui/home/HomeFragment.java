package com.example.hydraware.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.hydraware.PhViewModel;
import com.example.hydraware.R;
import com.example.hydraware.PhEvent;

public class HomeFragment extends Fragment {
    private TextView textPhValue, textTempValue;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        textPhValue = view.findViewById(R.id.textPhValue);
        textTempValue = view.findViewById(R.id.textTempValue);
        PhViewModel phViewModel = new ViewModelProvider(requireActivity()).get(PhViewModel.class);
        phViewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            if (!events.isEmpty()) {
                PhEvent last = events.get(events.size() - 1);
                textPhValue.setText(String.format("pH: %.2f (%s)", last.phValue, last.phState));
                textTempValue.setText(String.format("Temp: %.1f°C (%s)", last.tempValue, last.tempState));
            }
        });
        return view;
    }
}