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
    private static final int PH_CYCLE_LENGTH = 60; // 60 ciclos de 1s = 1 minuto
    private static final int TEMP_CYCLE_LENGTH = 60; // 60 ciclos de 1s = 1 minuto
    private int phCycle = 0;
    private int tempCycle = 0;

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
                // Ciclo de pH: Neutro -> Ácido -> Alcalino -> Neutro ...
                int phPhase = phCycle % 3;
                float ph;
                String phState;
                if (phPhase == 0) { // Neutro
                    ph = 7.0f;
                    phState = "Normal";
                } else if (phPhase == 1) { // Ácido
                    ph = 4.0f;
                    phState = "Bajo";
                } else { // Alcalino
                    ph = 10.0f;
                    phState = "Alto";
                }
                phCycle++;
                // Ciclo de temperatura: Ambiental -> Fría -> Caliente -> Fuera de rango -> Ambiental ...
                int tempPhase = tempCycle % 4;
                float temp;
                String tempState;
                if (tempPhase == 0) { // Ambiental
                    temp = 25.0f;
                    tempState = "Ambiental";
                } else if (tempPhase == 1) { // Fría
                    temp = 7.0f;
                    tempState = "Baja";
                } else if (tempPhase == 2) { // Caliente
                    temp = 55.0f;
                    tempState = "Alta";
                } else { // Fuera de rango
                    temp = 80.0f;
                    tempState = "Fuera de rango";
                }
                tempCycle++;
                PhEvent event = new PhEvent(ph, phState, temp, tempState, new Date());
                events.add(event);
                if (events.size() > 100) events.remove(0); // Limitar historial
                eventsLiveData.postValue(new ArrayList<>(events));
            }
        }, 0, 60000); // 1 minuto real
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) timer.cancel();
    }
} 