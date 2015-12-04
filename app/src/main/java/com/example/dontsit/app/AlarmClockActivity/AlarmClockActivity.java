package com.example.dontsit.app.AlarmClockActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import java.util.Calendar;
import java.util.TimeZone;

public class AlarmClockActivity extends AppCompatActivity {

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private Calendar mCalendar;
    private FormatChangeObserver mFormatChangeObserver;
    private int mMinutes;
    private int mHour;
    private int mSecond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver);
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = Calendar.getInstance();
                mCalendar.setTimeZone(TimeZone.getTimeZone(tz));
            }
            updateTime();
        }
    };

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange) {
            updateTime();
        }
    }

    private void updateTime() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());

        int hour = mCalendar.get(Calendar.HOUR);
        int minute = mCalendar.get(Calendar.MINUTE);
        int second = mCalendar.get(Calendar.SECOND);

        mSecond = second;
        mMinutes = Float.valueOf(minute + second / 60.0f).intValue();
        mHour = Float.valueOf(hour + mMinutes / 60.0f).intValue();
    }

}
