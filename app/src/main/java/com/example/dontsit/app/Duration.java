package com.example.dontsit.app;

import java.util.Date;

/**
 * Created by IDIC on 2015/12/1.
 */
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
}