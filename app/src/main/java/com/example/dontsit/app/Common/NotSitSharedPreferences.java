package com.example.dontsit.app.Common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.example.dontsit.app.Database.BLEStateChangedReceiver;
import com.example.dontsit.app.Database.CushionDatabaseChangedReceiver;
import com.example.dontsit.app.R;

public class NotSitSharedPreferences {

    private Context context;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private static final String Name = "NotSitPreference";
    public static final String BLEState = "IsConnected";
    public static final String ScanMode = "ScanMode";
    public static final String MAC = "CushionMAC";
    public static final String LastTimeDuration = "LastTimeDuration";
    public static final String LastNotifyTime = "LastNotifyTime";
    public static final String LastConnectTime = "LastConnectTime";
    public static final String IsSeated = "IsSeated";
    public static final String ClockSoundPath = "ClockSoundPath";
    public static final String IsChanged = "IsChanged";

    public static final String ClockSoundDefaultPath
            = "android.resource://com.example.dontsit.app/" + R.raw.oldalarmclock;

    public NotSitSharedPreferences(Context context) {
        this.context = context;
        settings = context.getSharedPreferences(Name, 0);
        editor = settings.edit();
    }

    public boolean set(String key, String value) {
        if (get(key).equals(value))
            return false;
        editor.putString(key, value);
        editor.apply();
        if (key.equals(IsSeated)) {
            Intent intent = new Intent(CushionDatabaseChangedReceiver.ACTION_DATABASE_CHANGED);
            intent.putExtra(context.getString(R.string.NotifyCushionStateDataKey), value.equals("1"));
            context.sendBroadcast(intent, CushionDatabaseChangedReceiver.PERMISSION_DATABASE_CHANGED);
        } else if (key.equals(BLEState)) {
            Intent intent = new Intent(BLEStateChangedReceiver.ACTION_STATE_CHANGED);
            context.sendBroadcast(intent, BLEStateChangedReceiver.PERMISSION_STATE_CHANGED);
        }
        return true;
    }

    public String get(String key) {
        if (key.equals(BLEState) ||
                key.equals(ScanMode) ||
                key.equals(IsChanged) ||
                key.equals(IsSeated))
            return settings.getString(key, "0");
        if (key.equals(ClockSoundPath))
            return settings.getString(key, ClockSoundDefaultPath);
        return settings.getString(key, "");
    }

    public void clear(String key) {
        editor.remove(key);
        editor.commit();
    }

    public void clear() {
        editor.clear();
        editor.commit();
    }

}
