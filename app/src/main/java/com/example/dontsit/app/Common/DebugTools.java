package com.example.dontsit.app.Common;

import android.content.res.Resources;
import android.util.Log;
import com.example.dontsit.app.R;

public class DebugTools {

    private static Boolean isDebug = false;
    public static String TAG;

    public static void initTools(Resources resources) {
        isDebug = resources.getBoolean(R.bool.debug);
        if (isDebug) {
            TAG = resources.getString(R.string.tag);
        }
    }

    public static void Log(Object message) {
        if (isDebug) {
            Log.i(TAG, message.toString());
        }
    }

    public static boolean isDebugged() {
        return isDebug;
    }
}
