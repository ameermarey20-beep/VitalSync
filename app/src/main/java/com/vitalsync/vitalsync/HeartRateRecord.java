package com.vitalsync.vitalsync;

import com.google.firebase.Timestamp;

public class HeartRateRecord {
    private int bpm;
    private Timestamp timestamp;
    private String userId;

    public HeartRateRecord() {
        // Required for Firestore
    }

    public HeartRateRecord(int bpm, Timestamp timestamp, String userId) {
        this.bpm = bpm;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public int getBpm() {
        return bpm;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }
}
