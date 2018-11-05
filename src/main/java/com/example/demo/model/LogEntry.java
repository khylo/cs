package com.example.demo.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

/*
Class for storing LogEntries
 */
@Data
@Entity
@Builder
public class LogEntry {

    public final static long AlertDuration = 4;

    @Id
    String id;

    public enum State {
        STARTED,
        FINISHED;
    }
    @NonNull
    State state;
    String type;
    String host;
    @Transient
    long timestamp;
    boolean alert;
    long duration;

    public void setDuration(long d){
        if (d > AlertDuration) {
            this.alert = true;
        }
        this.duration = d;
    }
}
