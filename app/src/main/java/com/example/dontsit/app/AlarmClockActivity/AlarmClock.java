package com.example.dontsit.app.AlarmClockActivity;

public class AlarmClock {

    public static final int EveryTimeAlarm = 0;
    public static final int OneTimeAlarm = 1;
    private int id, type, time;

    public AlarmClock() {
    }

    public AlarmClock(int type, int time) {
        this.type = type;
        this.time = time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return id + " - " + type + " - " + time;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AlarmClock && this.getId() == ((AlarmClock) o).getId();
    }
}
