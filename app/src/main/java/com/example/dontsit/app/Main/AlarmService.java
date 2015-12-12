package com.example.dontsit.app.Main;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.WindowManager;
import com.example.dontsit.app.AlarmClockActivity.AlarmClock;
import com.example.dontsit.app.AlarmClockActivity.CountDownTimerWithPause;
import com.example.dontsit.app.Common.NotSitSharedPreferences;
import com.example.dontsit.app.Database.AlarmClockDAO;
import com.example.dontsit.app.Database.AlarmDatabaseChangedReceiver;
import com.example.dontsit.app.Database.CushionDatabaseChangedReceiver;
import com.example.dontsit.app.Common.DebugTools;
import com.example.dontsit.app.R;

import java.io.IOException;
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
    private String MediaPath;
    private NotSitSharedPreferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        alarmClockDAO = new AlarmClockDAO(this);
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        preferences = new NotSitSharedPreferences(this);
        String IsSeated = preferences.get(NotSitSharedPreferences.IsSeated);
        if (!IsSeated.equals(""))
            this.IsSeated = IsSeated.equals("1");

        if (alarmClockDAO.getCount() > 0) {
            clocks = alarmClockDAO.getAll();
            for (AlarmClock clock : clocks) {
                DebugTools.Log("*"+clock);
                Timer timer = new Timer(clock);
                timers.add(timer);
                timer.create();
                if (this.IsSeated && clock.isEnabled())
                    timer.resume();
            }
        }
        alarmClockDAO.close();
        registerReceiver(mCushionDatabaseReceiver, mCushionFilter);
        registerReceiver(mAlarmDatabaseReceiver, mAlarmFilter);
        MediaPath = preferences.get(NotSitSharedPreferences.ClockSoundPath);
        DebugTools.Log(MediaPath);
        player = MediaPlayer.create(this, Uri.parse(MediaPath));
        player.setLooping(true);
    }


    public class Timer extends CountDownTimerWithPause {

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
            if (clock.isRepeated() && timers != null) {
                DebugTools.Log("Clock " + clock.getId() + " Restart!");
                timers.remove(this);
                Timer timer = new Timer(clock);
                timers.add(timer);
                timer.create();
                timer.pause();
                if (IsSeated)
                    timer.resume();
            } else {
                clock.setEnabled(false);
                alarmClockDAO.update(clock);
            }
        }

        @Override
        public String toString() {
            return "Timer " + clock;
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
        DebugTools.Log("AlarmService Destroy");
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
            DebugTools.Log("AlarmService receive " + IsSeated);
            for (int i = 0; i < timers.size(); i++) {
                Timer timer = timers.get(i);
                if (!timer.clock.isEnabled())
                    continue;
                if (IsSeated) {
                    timer.resume();
                } else {
                    if (timer.clock.isResettable()) {
                        timers.remove(timer);
                        timer.cancel();
                        Timer newTimer = new Timer(timer.clock);
                        timers.add(newTimer);
                        newTimer.create();
                    }
                    timer.pause();
                }
            }
        }
    };

    private AlarmDatabaseChangedReceiver mAlarmDatabaseReceiver = new AlarmDatabaseChangedReceiver() {
        public void onReceive(Context context, Intent intent) {
            alarmClockDAO = new AlarmClockDAO(AlarmService.this);
            Bundle extras = intent.getExtras();

            int id = -1;
            int operation = -1;
            if (extras != null) {
                id = extras.getInt("ID");
                operation = extras.getInt("Operation");
            }

            if (id == -1)
                return;


            AlarmClock newClock = alarmClockDAO.get((long) id);
            Timer temp = null;

            switch (operation) {
                //insert
                case 0:
                    if (newClock.isEnabled()) {
                        Timer newTimer = new Timer(newClock);
                        timers.add(newTimer);
                        newTimer.create();
                        newTimer.pause();
                        if (IsSeated)
                            newTimer.resume();
                    }
                    break;
                //update
                case 1:
                    for (Timer timer : timers)
                        if (timer.clock.getId() == id)
                            temp = timer;
                    if (newClock.isEnabled()) {
                        timers.remove(temp);
                        Timer newTimer = new Timer(newClock);
                        timers.add(newTimer);
                        newTimer.create();
                        newTimer.pause();
                        if (IsSeated)
                            newTimer.resume();
                    }
                    break;
                //delete
                case 2:
                    for (Timer timer : timers)
                        if (timer.clock.getId() == id)
                            temp = timer;
                    if (temp != null) {
                        temp.cancel();
                        timers.remove(temp);
                    }
                    break;
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
