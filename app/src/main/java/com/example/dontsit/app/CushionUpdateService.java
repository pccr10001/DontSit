package com.example.dontsit.app;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import java.text.ParseException;
import java.util.*;

public class CushionUpdateService extends Service implements BLEConnectible {

    // Binder given to clients
    private final IBinder binder = new LocalBinder();
    // Registered callbacks
    private DataAlwaysChanged serviceCallbacks;

    private BLEConnector connector;
    private String target_mac;
    private String target_service = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private String target_characteristic = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private CushionStateDAO StateDAO;
    private DurationLogDAO LogDAO;
    private CushionState state = new CushionState();
    private Duration duration = new Duration();
    //private final String Idle = "30";
    private final String Hold = "31";
    private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    @Override
    public void ScanResultThenDoWith(BluetoothDevice device) {
//        DebugTools.Log(device.getAddress());
//        DebugTools.Log(target_mac);
//        DebugTools.Log(Boolean.valueOf(device.getAddress().equals(target_mac)).toString());
        if (target_mac == null && !devices.contains(device)) {
            serviceCallbacks.notifyScanResult();
            devices.add(device);
        } else if (device.getAddress().equals(target_mac)) {
            state.setMAC(target_mac);
            connector.Connect(device);
            connector.ScanWith(false);
        }
    }

    public void connect(String mac) {
        for (BluetoothDevice device : devices) {
            if (device.getAddress().equals(mac)) {
                target_mac = mac;
                connector.setTarget_MAC(mac);
                ScanResultThenDoWith(device);
            }
        }
    }

    public void disconnect() {
        connector.DisConnect();
    }

    public void stopScan() {
        connector.ScanWith(false);
    }

    public List<BluetoothDevice> getResults() {
        return devices;
    }

    @Override
    public void ConnectThenDoWith() {
        state.setLastConnectTime(Calendar.getInstance().getTime());
        connector.DiscoverServices();
        serviceCallbacks.notifyConnect();
    }

    @Override
    public void DisConnectThenDoWith() {
        SaveDataWithSeatedIs(false);
        serviceCallbacks.notifyDisconnect();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void DiscoveredServicesThenDo(BluetoothGatt gatt) {
//        DebugTools.Log(target_service);
//        DebugTools.Log(target_characteristic);
//        for (BluetoothGattService service : gatt.getServices()) {
//            DebugTools.Log(service.getUuid().toString());
//            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics())
//                DebugTools.Log(characteristic.getUuid().toString());
//        }
        BluetoothGattService service = null;
        BluetoothGattCharacteristic characteristic = null;
        if (gatt != null)
            service = gatt.getService(UUID.fromString(target_service));
        if (service != null)
            characteristic = service.getCharacteristic(UUID.fromString(target_characteristic));
        if (characteristic != null)
            gatt.setCharacteristicNotification(characteristic, true);

    }

    @Override
    public void ReceiveNotificationThenDoWith(byte[] bytes) {
        SaveDataWithSeatedIs(BytesToHex(bytes).equals(Hold));
        serviceCallbacks.notifyDataChanged();
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
        StateDAO = new CushionStateDAO(context);
        LogDAO = new DurationLogDAO(context);
        connector = new BLEConnector(BluetoothAdapter.getDefaultAdapter(), this);
        try {
            if (StateDAO.getAll().size() != 0) {
                state = StateDAO.getAll().get(0);
                target_mac = state.getMAC();
                connector.setTarget_MAC(target_mac);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        connector.ScanWith(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void SaveDataWithSeatedIs(Boolean IsSeated) {
        //DebugTools.Log(IsSeated);
        Date now = Calendar.getInstance().getTime();

        if (state.isSeated() && !IsSeated) {
            Long time = now.getTime() - state.getLastNotifyTime().getTime();
            duration.setStartTime(state.getLastNotifyTime());
            duration.setTime(time.intValue());

            state.setLastTimeDuration(time.intValue());

            DebugTools.Log(state);
            DebugTools.Log(duration);

            try {
                StateDAO.update(state);
                LogDAO.insert(duration);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        state.setLastNotifyTime(now);
        state.setSeated(IsSeated);
    }

    @Override
    public void onDestroy() {
        disconnect();
        super.onDestroy();
    }

    private static String BytesToHex(byte[] bytes) {
        StringBuilder hexChars = new StringBuilder();
        for (byte aByte : bytes)
            hexChars.append(Integer.toHexString(aByte));
        return hexChars.toString();
    }

    // Class used for the client Binder.
    public class LocalBinder extends Binder implements IBinder {
        CushionUpdateService getService() {
            // Return this instance of MyService so clients can call public methods
            return CushionUpdateService.this;
        }
    }

    public void setCallbacks(DataAlwaysChanged callbacks) {
        serviceCallbacks = callbacks;
    }
}
