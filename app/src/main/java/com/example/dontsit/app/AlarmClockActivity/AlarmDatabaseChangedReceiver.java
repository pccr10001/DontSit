package com.example.dontsit.app.AlarmClockActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmDatabaseChangedReceiver extends BroadcastReceiver{

    public static String ACTION_DATABASE_CHANGED = "com.example.dontsit.app.AlarmClockActivity.DATABASE_CHANGED";

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
