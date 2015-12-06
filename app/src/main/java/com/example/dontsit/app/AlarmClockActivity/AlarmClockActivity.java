package com.example.dontsit.app.AlarmClockActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.*;
import android.widget.Button;
import android.widget.ListView;
import com.example.dontsit.app.Common.DebugTools;
import com.example.dontsit.app.R;

import java.util.List;

public class AlarmClockActivity extends AppCompatActivity
        implements NumberPicker.OnScrollListener, NumberPicker.OnValueChangeListener, NumberPicker.Formatter {

    private AlarmClockDAO alarmClockDAO;
    private com.rey.material.widget.CheckBox alarmType;
    private NumberPicker HourPicker;
    private NumberPicker MinutePicker;
    private NumberPicker SecondPicker;
    private Button AddButton;
    private ListView alarmListView;
    private AlarmListAdapter adapter;
    private List<AlarmClock> clocks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarmclock);
        alarmClockDAO = new AlarmClockDAO(this);
        alarmListView = (ListView) findViewById(R.id.ClockListView);
        clocks = alarmClockDAO.getAll();
        adapter = new AlarmListAdapter(this, clocks);
        alarmListView.setAdapter(adapter);
        alarmType = (com.rey.material.widget.CheckBox) findViewById(R.id.ClockType);
        HourPicker = (NumberPicker) findViewById(R.id.HourNumberPicker);
        HourPicker.setMaxValue(24);
        HourPicker.setMinValue(0);
        HourPicker.setFormatter(this);
        HourPicker.setOnScrollListener(this);
        HourPicker.setOnValueChangedListener(this);
        MinutePicker = (NumberPicker) findViewById(R.id.MinuteNumberPicker);
        MinutePicker.setMaxValue(60);
        MinutePicker.setMinValue(0);
        MinutePicker.setFormatter(this);
        MinutePicker.setOnScrollListener(this);
        MinutePicker.setOnValueChangedListener(this);
        SecondPicker = (NumberPicker) findViewById(R.id.SecondNumberPicker);
        SecondPicker.setMaxValue(60);
        SecondPicker.setMinValue(0);
        SecondPicker.setFormatter(this);
        SecondPicker.setOnScrollListener(this);
        SecondPicker.setOnValueChangedListener(this);
        AddButton = (Button) findViewById(R.id.ClockAddButton);
        AddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int type = alarmType.isChecked() ? AlarmClock.EveryTimeAlarm : AlarmClock.OneTimeAlarm;
                int time = HourPicker.getValue() * 3600
                        + MinutePicker.getValue() * 60 + SecondPicker.getValue();
                AlarmClock clock = new AlarmClock(type, time * 1000);
                alarmClockDAO.insert(clock);
                clocks.add(clock);
                adapter.notifyDataSetChanged();
            }
        });
        registerReceiver(mAlarmDatabaseReceiver, mAlarmFilter);
    }

    @Override
    public String format(int value) {
        String tmpStr = String.valueOf(value);
        if (value < 10) {
            tmpStr = "0" + tmpStr;
        }
        return tmpStr;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mAlarmDatabaseReceiver);
        super.onDestroy();
    }

    @Override
    public void onScrollStateChange(NumberPicker view, int scrollState) {
//        switch (scrollState) {
//            case NumberPicker.OnScrollListener.SCROLL_STATE_FLING:
//                break;
//            case NumberPicker.OnScrollListener.SCROLL_STATE_IDLE:
//                break;
//            case NumberPicker.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
//                break;
//        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {

    }

    private IntentFilter mAlarmFilter = new IntentFilter(AlarmDatabaseChangedReceiver.ACTION_DATABASE_CHANGED);

    private AlarmDatabaseChangedReceiver mAlarmDatabaseReceiver = new AlarmDatabaseChangedReceiver() {
        public void onReceive(Context context, Intent intent) {
            alarmClockDAO = new AlarmClockDAO(AlarmClockActivity.this);
            Bundle extras = intent.getExtras();
            int id = -1;

            if (extras != null)
                id = extras.getInt("ID");
//            DebugTools.Log(id);

            if (id == -1)
                return;

            AlarmClock newClocks = alarmClockDAO.get((long) id);

            if (newClocks == null && adapter != null) {
                AlarmClock clock = new AlarmClock();
                clock.setId(id);
                adapter.remove(clock);
            }
        }
    };
}
