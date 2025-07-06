package com.example.hydraware;

import java.util.Date;

public class PhEvent {
    public float phValue;
    public String phState;
    public float tempValue;
    public String tempState;
    public Date timestamp;

    public PhEvent(float phValue, String phState, float tempValue, String tempState, Date timestamp) {
        this.phValue = phValue;
        this.phState = phState;
        this.tempValue = tempValue;
        this.tempState = tempState;
        this.timestamp = timestamp;
    }
} 