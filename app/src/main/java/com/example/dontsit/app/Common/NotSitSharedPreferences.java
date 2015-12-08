package com.example.dontsit.app.Common;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.example.dontsit.app.Database.CushionDatabaseChangedReceiver;
import com.example.dontsit.app.R;

public class NotSitSharedPreferences {

    private Context context;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private static final String Name = "NotSitPreference";
    public static final String BLEState = "IsConnected";
    public static final String MAC = "CushionMAC";
    public static final String LastTimeDuration = "LastTimeDuration";
    public static final String LastNotifyTime = "LastNotifyTime";
    public static final String LastConnectTime = "LastConnectTime";
    public static final String IsSeated = "IsSeated";

    public NotSitSharedPreferences(Context context) {
        this.context = context;
        settings = context.getSharedPreferences(Name, 0);
        editor = settings.edit();
    }

    public void set(String key, String value) {
        editor.putString(key, value);
        editor.apply();
        if (key.equals(IsSeated)) {
            Intent intent = new Intent(
                    CushionDatabaseChangedReceiver.ACTION_DATABASE_CHANGED);
            intent.putExtra(context.getString(R.string.NotifyCushionStateDataKey), value.equals("1"));
            context.sendBroadcast(intent
                    , CushionDatabaseChangedReceiver.PERMISSION_DATABASE_CHANGED);
        }
    }

    public String get(String key) {
        return settings.getString(key, key.equals(BLEState) ? "0" : "");
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
