package com.example.dontsit.app.Database;

import java.util.Date;

public class DayDuration {

    private int Id;
    private Date Date;
    private int SitTime;
    private int ChangeTime;

    public DayDuration() {
    }

    public Date getDate() {
        return Date;
    }

    public void setDate(Date date) {
        Date = date;
    }

    public int getSitTime() {
        return SitTime;
    }

    public void setSitTime(int sitTime) {
        SitTime = sitTime;
    }

    public int getChangeTime() {
        return ChangeTime;
    }

    public void setChangeTime(int changeTime) {
        ChangeTime = changeTime;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    @Override
    public String toString() {
        return Id + " - " + Date + " - " + SitTime + " - " + ChangeTime;
    }
}
