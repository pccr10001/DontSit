package com.example.dontsit.app.Database;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BLEStateChangedReceiver extends BroadcastReceiver {

    public static String ACTION_STATE_CHANGED = "com.example.dontsit.app.Database.BLUETOOTH_CHANGED";
    public static String PERMISSION_STATE_CHANGED = "com.example.dontsit.SEND_BLEState";

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
