package com.example.dontsit.app;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

public interface BLEConnectible {
    void ScanResultThenDoWith(BluetoothDevice device);
    void ConnectThenDoWith();
    void ReceiveNotificationThenDoWith(byte[] bytes);
    void DiscoveredServicesThenDo(BluetoothGatt gatt);
}
