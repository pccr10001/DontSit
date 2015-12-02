package com.example.dontsit.app;

import android.app.Service;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class CushionUpdateService extends Service implements BLEConnectible{

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

    @Override
    public void ScanResultThenDoWith(BluetoothDevice device) {
//        DebugTools.Log(device.getAddress());
//        DebugTools.Log(target_mac);
//        DebugTools.Log(Boolean.valueOf(device.getAddress().equals(target_mac)).toString());
        if (device.getAddress().equals(target_mac)) {
            state.setMAC(target_mac);
            connector.Connect(device);
        }
    }

    @Override
    public void ConnectThenDoWith() {
        state.setLastConnectTime(Calendar.getInstance().getTime());
        connector.DiscoverServices();
    }


    @Override
    public void DiscoveredServicesThenDo(BluetoothGatt gatt) {
//        DebugTools.Log(target_service);
//        DebugTools.Log(target_characteristic);
//        for (BluetoothGattService service : gatt.getServices()) {
//            DebugTools.Log(service.getUuid().toString());
//            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics())
//                DebugTools.Log(characteristic.getUuid().toString());
//        }
        BluetoothGattCharacteristic characteristic = gatt.getService(UUID.fromString(target_service))
                .getCharacteristic(UUID.fromString(target_characteristic));
        gatt.setCharacteristicNotification(characteristic, true);

    }

    @Override
    public void ReceiveNotificationThenDoWith(byte[] bytes) {
        Date now = Calendar.getInstance().getTime();
        Long time = now.getTime() - state.getLastNotifyTime().getTime();

        duration.setStartTime(state.getLastNotifyTime());
        duration.setTime(time.intValue());

        state.setLastTimeDuration(time.intValue());
        state.setLastNotifyTime(now);
        state.setSeated(BytesToHex(bytes).equals(Hold));

        DebugTools.Log(state.toString());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = getApplicationContext();
        StateDAO = new CushionStateDAO(context);
        LogDAO = new DurationLogDAO(context);
        try {
            target_mac = StateDAO.getAll().get(0).getMAC();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        connector = new BLEConnector(BluetoothAdapter.getDefaultAdapter(), this);
        connector.setTarget_MAC(target_mac);
        connector.ScanWith(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        connector.DisConnect();
        super.onDestroy();
    }

    private static String BytesToHex(byte[] bytes) {
        StringBuilder hexChars = new StringBuilder();
        for (byte aByte : bytes)
            hexChars.append(Integer.toHexString(aByte));
        return hexChars.toString();
    }
}
