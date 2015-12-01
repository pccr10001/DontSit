package com.example.dontsit.app;

import java.util.Date;

public class CushionState {
    private String MAC;
    private Integer LastTimeDuration ;
    private Date LastNotifyTime;
    private Date LastConnectTime;
    private boolean IsSeated;

    public CushionState() {

    }

    public CushionState(String mac) {
        MAC = mac;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public Integer getLastTimeDuration() {
        return LastTimeDuration;
    }

    public void setLastTimeDuration(Integer lastTimeDuration) {
        LastTimeDuration = lastTimeDuration;
    }

    public Date getLastNotifyTime() {
        return LastNotifyTime;
    }

    public void setLastNotifyTime(Date lastNotifyTime) {
        LastNotifyTime = lastNotifyTime;
    }

    public Date getLastConnectTime() {
        return LastConnectTime;
    }

    public void setLastConnectTime(Date lastConnectTime) {
        LastConnectTime = lastConnectTime;
    }

    public boolean isSeated() {
        return IsSeated;
    }

    public void setSeated(boolean seated) {
        IsSeated = seated;
    }

    @Override
    public String toString() {
        return MAC + ", " + LastTimeDuration + ", "
                + LastNotifyTime + ", " + LastConnectTime + ", " + isSeated();
    }
}
