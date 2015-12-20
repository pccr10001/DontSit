package com.example.dontsit.app.AlarmClockActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.NumberPicker;
import com.example.dontsit.app.Database.AlarmClockDAO;
import com.example.dontsit.app.Database.AlarmDatabaseChangedReceiver;
import com.example.dontsit.app.R;
import com.gc.materialdesign.views.ButtonFloat;
import com.rey.material.app.Dialog;

import java.util.List;

public class AlarmClockActivity extends AppCompatActivity
        implements NumberPicker.OnScrollListener, NumberPicker.OnValueChangeListener,
        NumberPicker.Formatter, View.OnClickListener, AbsListView.OnScrollListener {

    private AlarmClockDAO alarmClockDAO;
    private ButtonFloat SetButton;
    private CheckBox alarmRepeat;
    private CheckBox alarmReset;
    private NumberPicker HourPicker;
    private NumberPicker MinutePicker;
    private NumberPicker SecondPicker;
    private ListView alarmListView;
    private AlarmListAdapter adapter;
    private List<AlarmClock> clocks;
    private int ButtonTextSize = 18;
    private View dialogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarmclock);
        initToolbar();

        alarmClockDAO = new AlarmClockDAO(this);
        alarmListView = (ListView) findViewById(R.id.ClockListView);
        if (alarmClockDAO.getCount() == 0)
            alarmClockDAO.generate();
        clocks = alarmClockDAO.getAll();
        alarmClockDAO.close();
        adapter = new AlarmListAdapter(this, clocks);
        alarmListView.setAdapter(adapter);
        alarmListView.setOnScrollListener(this);
        SetButton = (ButtonFloat) findViewById(R.id.AddButtonFloat);
        SetButton.setOnClickListener(this);
        registerReceiver(mAlarmDatabaseReceiver, mAlarmFilter);
    }

    private void initToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.alarm_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            SetButton.animate().start();
            SetButton.hide();
        } else {
            SetButton.animate().cancel();
            SetButton.show();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        dialogView = getLayoutInflater().inflate(R.layout.dialog_alarmsetting, null);
        alarmRepeat = (CheckBox) dialogView.findViewById(R.id.ClockRepeat);
        alarmReset = (CheckBox) dialogView.findViewById(R.id.ClockReset);
        HourPicker = (NumberPicker) dialogView.findViewById(R.id.HourNumberPicker);
        HourPicker.setMaxValue(24);
        HourPicker.setMinValue(0);
        HourPicker.setFormatter(this);
        HourPicker.setOnScrollListener(this);
        HourPicker.setOnValueChangedListener(this);
        MinutePicker = (NumberPicker) dialogView.findViewById(R.id.MinuteNumberPicker);
        MinutePicker.setMaxValue(60);
        MinutePicker.setMinValue(0);
        MinutePicker.setFormatter(this);
        MinutePicker.setOnScrollListener(this);
        MinutePicker.setOnValueChangedListener(this);
        SecondPicker = (NumberPicker) dialogView.findViewById(R.id.SecondNumberPicker);
        SecondPicker.setMaxValue(60);
        SecondPicker.setMinValue(0);
        SecondPicker.setFormatter(this);
        SecondPicker.setOnScrollListener(this);
        SecondPicker.setOnValueChangedListener(this);

        builder.setView(dialogView)
                .setTitle("新增鬧鐘")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("新增", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alarmClockDAO = new AlarmClockDAO(AlarmClockActivity.this);
                        boolean IsRepeated = alarmRepeat.isChecked();
                        boolean IsResettable = alarmReset.isChecked();
                        int time = HourPicker.getValue() * 3600
                                + MinutePicker.getValue() * 60 + SecondPicker.getValue();
                        AlarmClock clock = new AlarmClock();
                        clock.setIsRepeated(IsRepeated);
                        clock.setIsResettable(IsResettable);
                        clock.setEnabled(true);
                        clock.setTime(time * 1000);
                        alarmClockDAO.insert(clock);
                        clocks.add(clock);
                        adapter.notifyDataSetChanged();
                        dialog.cancel();
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                ((AlertDialog) dialog).getButton(Dialog.BUTTON_POSITIVE).setTextSize(ButtonTextSize);
                ((AlertDialog) dialog).getButton(Dialog.BUTTON_NEGATIVE).setTextSize(ButtonTextSize);
            }
        });
        dialog.show();
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

            if (extras != null) {
                id = extras.getInt("ID");
            }
//            DebugTools.Log(id);

            if (id == -1)
                return;

            if (adapter == null)
                return;

            AlarmClock newClock = alarmClockDAO.get((long) id);
            //alarm_delete
            if (newClock == null) {
                AlarmClock clock = new AlarmClock();
                clock.setId(id);
                adapter.remove(clock);
            } else { //update
                ((AlarmClock) adapter.getItemById(newClock.getId())).setEnabled(newClock.isEnabled());
                adapter.notifyDataSetChanged();
            }
        }
    };

    public void BackParent(View view) {
        onBackPressed();
    }
}
