package com.example.dontsit.app.AlarmClockActivity;

public class AlarmClock {

    private boolean IsRepeated = false,
            IsResettable = true,
            Enabled = true;

    private int id, time;

    public AlarmClock() {

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

    public boolean isRepeated() {
        return IsRepeated;
    }

    public void setIsRepeated(boolean isRepeated) {
        IsRepeated = isRepeated;
    }

    public boolean isResettable() {
        return IsResettable;
    }

    public void setIsResettable(boolean isResettable) {
        IsResettable = isResettable;
    }

    public boolean isEnabled() {
        return Enabled;
    }

    public void setEnabled(boolean enabled) {
        Enabled = enabled;
    }

    @Override
    public String toString() {
        return id + " - " + time + " - Repeat " + Boolean.valueOf(IsRepeated).toString() +
                " - Reset " + Boolean.valueOf(IsResettable).toString() +
                " - Enabled " + Boolean.valueOf(Enabled).toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AlarmClock && this.getId() == ((AlarmClock) o).getId();
    }

}
