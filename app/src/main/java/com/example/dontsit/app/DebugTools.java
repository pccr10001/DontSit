package com.example.dontsit.app;

import android.content.res.Resources;
import android.util.Log;

public class DebugTools {

    private static Boolean isDebug;
    private static String TAG;

    public static void initTools(Resources resources) {
        isDebug = resources.getBoolean(R.bool.debug);
        if (isDebug) {
            TAG = resources.getString(R.string.tag);
        }
    }

    public static void Log(String message) {
        if (isDebug) {
            Log.i(TAG, message);
        }
    }

    public static boolean isDebugged() {
        return isDebug;
    }
}
