package com.example.hydraware;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhHistoryFragment extends Fragment {
    private PhHistoryAdapter adapter;
    private List<PhEvent> allEvents = new ArrayList<>();
    private EditText searchEditText;
    private Spinner spinnerPhState, spinnerTempState;
    private Button buttonDate;
    private Date selectedDate = null;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ph_history, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PhHistoryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
        searchEditText = view.findViewById(R.id.searchEditText);
        spinnerPhState = view.findViewById(R.id.spinnerPhState);
        spinnerTempState = view.findViewById(R.id.spinnerTempState);
        buttonDate = view.findViewById(R.id.buttonDate);
        PhViewModel viewModel = new ViewModelProvider(requireActivity()).get(PhViewModel.class);
        viewModel.getEvents().observe(getViewLifecycleOwner(), events -> {
            allEvents = new ArrayList<>(events);
            applyFilters();
        });
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilters(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        spinnerPhState.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { applyFilters(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        spinnerTempState.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) { applyFilters(); }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        buttonDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new android.app.DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                selectedDate = calendar.getTime();
                buttonDate.setText(dateFormat.format(selectedDate));
                applyFilters();
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
        return view;
    }
    private void applyFilters() {
        String search = searchEditText.getText().toString().toLowerCase(Locale.getDefault());
        String phState = spinnerPhState.getSelectedItem().toString();
        String tempState = spinnerTempState.getSelectedItem().toString();
        List<PhEvent> filtered = new ArrayList<>();
        for (PhEvent event : allEvents) {
            boolean matches = true;
            if (!phState.equals("Todos") && !event.phState.equals(phState)) matches = false;
            if (!tempState.equals("Todos") && !event.tempState.equals(tempState)) matches = false;
            if (selectedDate != null) {
                String eventDate = dateFormat.format(event.timestamp);
                String selected = dateFormat.format(selectedDate);
                if (!eventDate.equals(selected)) matches = false;
            }
            if (!search.isEmpty()) {
                String values = String.format(Locale.getDefault(), "%.2f %.1f %s %s", event.phValue, event.tempValue, event.phState, event.tempState).toLowerCase(Locale.getDefault());
                if (!values.contains(search)) matches = false;
            }
            if (matches) filtered.add(event);
        }
        adapter.updateData(filtered);
    }
} 