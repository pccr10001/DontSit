package com.example.dontsit.app.AlarmClockActivity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.view.WindowManager;
import com.example.dontsit.app.Common.CushionDatabaseChangedReceiver;
import com.example.dontsit.app.Common.CushionStateDAO;
import com.example.dontsit.app.Common.DebugTools;
import com.example.dontsit.app.R;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class AlarmService extends Service {

    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    private AlarmManager manager;
    private AlarmClockDAO alarmClockDAO;
    private List<AlarmClock> clocks;
    private List<Timer> timers = new ArrayList<Timer>();
    private Boolean IsSeated = false;
    private MediaPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        alarmClockDAO = new AlarmClockDAO(this);
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        clocks = alarmClockDAO.getAll();
        for (final AlarmClock clock : clocks)
            timers.add(new Timer(clock));
        CushionStateDAO dao = new CushionStateDAO(this);
        if (dao.getCount() > 0)
            try {
                IsSeated = dao.getAll().get(0).isSeated();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        alarmClockDAO.close();
        dao.close();
        registerReceiver(mCushionDatabaseReceiver, mCushionFilter);
        registerReceiver(mAlarmDatabaseReceiver, mAlarmFilter);
        player = MediaPlayer.create(this, R.raw.oldalarmclock);
    }


    private class Timer extends CountDownTimerWithPause {

        private AlarmClock clock;

        public Timer(AlarmClock clock) {
            super(clock.getTime(), 1000, false);
            this.clock = clock;
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            alarmClockDAO = new AlarmClockDAO(AlarmService.this);
            DebugTools.Log("Clock " + clock.getId() + " Finish!");
            showTimeUpMessage();
            player.start();
            if (clock.getType() == AlarmClock.EveryTimeAlarm && timers != null) {
                DebugTools.Log("Clock " + clock.getId() + " Restart!");
                timers.remove(this);
                Timer timer = new Timer(clock);
                timers.add(timer);
                timer.create();
                if (IsSeated)
                    timer.resume();
            } else {
                alarmClockDAO.delete(clock.getId());
            }
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Timer && clock.getId() == ((Timer) o).clock.getId();
        }
    }

    private void showTimeUpMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Time Up!!!!")
                .setTitle("該起身動一動囉~~~")
                .setNegativeButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        player.stop();
                        try {
                            player.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mCushionDatabaseReceiver);
        unregisterReceiver(mAlarmDatabaseReceiver);
        super.onDestroy();
    }

    private CushionDatabaseChangedReceiver mCushionDatabaseReceiver = new CushionDatabaseChangedReceiver() {
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras == null)
                return;
            IsSeated = extras.getBoolean(getString(R.string.NotifyCushionStateDataKey));
//            DebugTools.Log(IsSeated);
            for (Timer timer : timers) {
                if (IsSeated)
                    timer.resume();
                else
                    timer.pause();
            }
        }
    };

    private AlarmDatabaseChangedReceiver mAlarmDatabaseReceiver = new AlarmDatabaseChangedReceiver() {
        public void onReceive(Context context, Intent intent) {
            alarmClockDAO = new AlarmClockDAO(AlarmService.this);
            Bundle extras = intent.getExtras();
            int id = -1;

            if (extras != null)
                id = extras.getInt("ID");
//            DebugTools.Log(id);

            if (id == -1)
                return;

            AlarmClock newClocks = alarmClockDAO.get((long) id);
            if (newClocks != null) {
                DebugTools.Log(newClocks);
                Timer timer = new Timer(newClocks);
                timers.add(timer);
                timer.create();
                if (IsSeated)
                    timer.resume();
            } else {
                for (Timer timer : timers)
                    if (timer.clock.getId() == id) {
                        timers.remove(timer);
                        timer.cancel();
                    }
            }
        }
    };

    private IntentFilter mCushionFilter = new IntentFilter(CushionDatabaseChangedReceiver.ACTION_DATABASE_CHANGED);
    private IntentFilter mAlarmFilter = new IntentFilter(AlarmDatabaseChangedReceiver.ACTION_DATABASE_CHANGED);

    public class LocalBinder extends Binder implements IBinder {
        public AlarmService getService() {
            // Return this instance of MyService so clients can call public methods
            return AlarmService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
