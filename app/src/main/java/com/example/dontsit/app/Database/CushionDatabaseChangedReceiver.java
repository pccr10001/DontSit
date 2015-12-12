package com.example.dontsit.app.Database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CushionDatabaseChangedReceiver extends BroadcastReceiver {

    public static String ACTION_DATABASE_CHANGED = "com.example.dontsit.app.Database.CUSHION_CHANGED";
    public static String PERMISSION_DATABASE_CHANGED = "com.example.dontsit.SEND_CushionState";

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}