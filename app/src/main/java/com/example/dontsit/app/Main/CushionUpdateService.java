package com.example.dontsit.app.Main;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.*;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import com.example.dontsit.app.Common.*;
import com.example.dontsit.app.Database.CushionState;
import com.example.dontsit.app.Database.Duration;
import com.example.dontsit.app.Database.DurationLogDAO;

import java.text.ParseException;
import java.util.*;

public class CushionUpdateService extends Service implements BLEConnectible {

    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Registered callbacks

    private static final String target_service = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String target_characteristic = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final String Hold = "1";
    //private static final String Idle = "30";

    private BLEConnector connector;
    private String target_mac;
    private DurationLogDAO LogDAO;
    private CushionState state = new CushionState();
    private Duration duration = new Duration();
    private NotSitSharedPreferences preferences;

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    @Override
    public void ScanResultThenDoWith(BluetoothDevice device) {
//        DebugTools.Log(device.getAddress());
//        DebugTools.Log(target_mac);
//        DebugTools.Log(Boolean.valueOf(device.getAddress().equals(target_mac)).toString());if (device.getAddress().equals(target_mac)) {
        if (target_mac.equals(device.getAddress())) {
            connector.Connect(device);
            connector.ScanWith(false);
        }
    }

    @Override
    public void ConnectionStateChangedThenDo(int newState) {
        if (newState == BluetoothGatt.STATE_CONNECTED) {
            connector.DiscoverServices();
            this.state.setLastConnectTime(Calendar.getInstance().getTime());
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            preferences.set(NotSitSharedPreferences.BLEState, String.valueOf(newState));
            preferences.set(NotSitSharedPreferences.IsSeated, "0");
        }
    }

    @Override
    public void DiscoveredServicesThenDo(BluetoothGatt gatt) {
        BluetoothGattService service = null;
        BluetoothGattCharacteristic characteristic = null;
        if (gatt != null)
            service = gatt.getService(UUID.fromString(target_service));
        if (service != null)
            characteristic = service.getCharacteristic(UUID.fromString(target_characteristic));
        if (characteristic != null) {
            gatt.setCharacteristicNotification(characteristic, true);
            preferences.set(NotSitSharedPreferences.BLEState,
                    String.valueOf(BluetoothGatt.STATE_CONNECTED));
            characteristic.setValue(intToByteArray(1));
            gatt.writeCharacteristic(characteristic);
        }
    }

    @Override
    public void ReceiveNotificationThenDoWith(byte[] bytes) {
//        DebugTools.Log("Notify " + BytesToHex(bytes).equals(Hold));
        SaveDataWithSeatedIs(BytesToHex(bytes).equals(Hold));
    }

    private void SaveDataWithSeatedIs(Boolean IsSeated) {
//        DebugTools.Log(IsSeated);
        Date now = Calendar.getInstance().getTime();

        LogDAO = new DurationLogDAO(this);

        //if Seated --> Not Seated Update Data
        if (state.isSeated() && !IsSeated) {
            Long time = now.getTime() - state.getLastNotifyTime().getTime();
            duration.setStartTime(state.getLastNotifyTime());
            duration.setTime(time.intValue());

            state.setLastTimeDuration(time.intValue());

            DebugTools.Log(state);
            DebugTools.Log(duration);

            try {
                LogDAO.insert(duration);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        //if NotSeated --> Seated Still Save Data
        if (state.getLastTimeDuration() == null)
            state.setLastTimeDuration(0);

        state.setLastNotifyTime(now);
        state.setSeated(IsSeated);
        try {
            preferences.set(NotSitSharedPreferences.LastNotifyTime,
                    DateFormatter.format(state.getLastNotifyTime()));
            preferences.set(NotSitSharedPreferences.LastTimeDuration,
                    String.valueOf(state.getLastTimeDuration()));
            preferences.set(NotSitSharedPreferences.IsSeated,
                    state.isSeated() ? "1" : "0");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        LogDAO = new DurationLogDAO(context);
        connector = new BLEConnector(BluetoothAdapter.getDefaultAdapter(), this);
        preferences = new NotSitSharedPreferences(this);
        String mac = preferences.get(NotSitSharedPreferences.MAC);
        state = new CushionState();
        if (!mac.equals("")) {
            state.setMAC(mac);
            state.setLastTimeDuration(
                    Integer.valueOf(preferences.get(NotSitSharedPreferences.LastTimeDuration)));
            try {
                state.setLastConnectTime(
                        DateFormatter.parse((preferences.get(NotSitSharedPreferences.LastConnectTime))));
                state.setLastNotifyTime(
                        DateFormatter.parse((preferences.get(NotSitSharedPreferences.LastNotifyTime))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            state.setSeated(preferences.get(NotSitSharedPreferences.IsSeated).equals("1"));
            target_mac = state.getMAC();
            connector.setTarget_MAC(target_mac);
        }
        String ScanMode = preferences.get(NotSitSharedPreferences.ScanMode);
        if (!ScanMode.equals(""))
            connector.setScanMode(Integer.valueOf(ScanMode));

        connector.ScanWith(true);
    }

    @Override
    public void onDestroy() {
        DebugTools.Log("CushionUpdateService Destroy");
        LogDAO.close();
        preferences.set(NotSitSharedPreferences.BLEState,
                String.valueOf(BluetoothGatt.STATE_DISCONNECTED));
        connector.DisConnect();
        connector = null;
        super.onDestroy();
    }

    private static String BytesToHex(byte[] bytes) {
        StringBuilder hexChars = new StringBuilder();
        for (byte aByte : bytes)
            hexChars.append(Integer.toHexString(aByte));
        return hexChars.toString();
    }

    private static byte[] intToByteArray(int a) {
        byte[] ret = new byte[4];
        ret[3] = (byte) (a & 0xFF);
        ret[2] = (byte) ((a >> 8) & 0xFF);
        ret[1] = (byte) ((a >> 16) & 0xFF);
        ret[0] = (byte) ((a >> 24) & 0xFF);
        return ret;
    }

    // Class used for the client Binder.
    public class LocalBinder extends Binder implements IBinder {
        public CushionUpdateService getService() {
            // Return this instance of MyService so clients can call public methods
            return CushionUpdateService.this;
        }
    }
}
