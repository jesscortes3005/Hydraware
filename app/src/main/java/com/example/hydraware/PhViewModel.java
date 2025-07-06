package com.example.hydraware;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PhViewModel extends ViewModel {
    private final MutableLiveData<List<PhEvent>> eventsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final List<PhEvent> events = new ArrayList<>();
    private Timer timer;
    private int cycleCounter = 0;
    private static final int CYCLE_LENGTH = 60; // 60 ciclos de 3s ≈ 3 minutos

    public PhViewModel() {
        startSimulation();
    }

    public LiveData<List<PhEvent>> getEvents() {
        return eventsLiveData;
    }

    private void startSimulation() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Cambiar de estado cada 5 minutos (100 ciclos de 3s)
                int phase = (cycleCounter / CYCLE_LENGTH) % 3;
                float ph;
                String phState;
                float temp;
                String tempState;
                if (phase == 0) { // Normal
                    ph = 7.0f;
                    phState = "Normal";
                    temp = 22.0f;
                    tempState = "Fría/Ambiental";
                } else if (phase == 1) { // Bajo
                    ph = 5.5f;
                    phState = "Bajo";
                    temp = 7.0f;
                    tempState = "Baja";
                } else { // Alto
                    ph = 8.5f;
                    phState = "Alto";
                    temp = 55.0f;
                    tempState = "Alta";
                }
                cycleCounter++;
                PhEvent event = new PhEvent(ph, phState, temp, tempState, new Date());
                events.add(event);
                if (events.size() > 100) events.remove(0); // Limitar historial
                eventsLiveData.postValue(new ArrayList<>(events));
            }
        }, 0, 3000);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) timer.cancel();
    }
} 