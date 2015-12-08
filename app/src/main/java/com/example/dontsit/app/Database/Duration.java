package com.example.dontsit.app.Database;

import com.example.dontsit.app.Common.DateFormatter;

import java.text.ParseException;
import java.util.Date;

public class Duration {
    private Date StartTime;
    private Integer Time;

    public Duration() {

    }

    public Duration(Date startTime, Integer time) {
        StartTime = startTime;
        Time = time;
    }

    public Date getStartTime() {
        return StartTime;
    }

    public void setStartTime(Date startTime) {
        StartTime = startTime;
    }

    public Integer getTime() {
        return Time;
    }

    public void setTime(Integer time) {
        Time = time;
    }

    @Override
    public String toString() {
        try {
            return DateFormatter.format(StartTime) + ", " + Time + " milliseconds";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
