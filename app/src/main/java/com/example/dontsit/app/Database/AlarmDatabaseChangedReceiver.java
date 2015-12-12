package com.example.dontsit.app.Database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmDatabaseChangedReceiver extends BroadcastReceiver{

    public static String ACTION_DATABASE_CHANGED = "com.example.dontsit.app.Database.ALARM_CHANGED";
    public static String PERMISSION_DATABASE_CHANGED = "com.example.dontsit.SEND_AlarmClock";
    public static int ACTION_INSERT = 0;
    public static int ACTION_UPDATE = 1;
    public static int ACTION_DELETE = 2;

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
