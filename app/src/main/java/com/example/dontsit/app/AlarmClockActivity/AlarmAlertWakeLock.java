package com.example.dontsit.app.AlarmClockActivity;

import android.content.Context;
import android.os.PowerManager;
import com.example.dontsit.app.Common.DebugTools;

/**
 * Hold a wakelock that can be acquired in the AlarmReceiver and
 * released in the AlarmAlert activity
 */

public class AlarmAlertWakeLock {
    private static PowerManager.WakeLock sScreenWakeLock;
    private static PowerManager.WakeLock sCpuWakeLock;
    static void acquireCpuWakeLock(Context context) {
        DebugTools.Log("Acquiring cpu wake lock");
        if (sCpuWakeLock != null) {
            return;
        }
        PowerManager pm =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        sCpuWakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, DebugTools.TAG);
        sCpuWakeLock.acquire();
    }
    static void acquireScreenWakeLock(Context context) {
        DebugTools.Log("Acquiring screen wake lock");
        if (sScreenWakeLock != null) {
            return;
        }
        PowerManager pm =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        sScreenWakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.ON_AFTER_RELEASE, DebugTools.TAG);
        sScreenWakeLock.acquire();
    }
    static void release() {
        DebugTools.Log("Releasing wake lock");
        if (sCpuWakeLock != null) {
            sCpuWakeLock.release();
            sCpuWakeLock = null;
        }
        if (sScreenWakeLock != null) {
            sScreenWakeLock.release();
            sScreenWakeLock = null;
        }
    }
}