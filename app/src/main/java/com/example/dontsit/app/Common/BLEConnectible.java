package com.example.dontsit.app.Common;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

public interface BLEConnectible {
    void ScanResultThenDoWith(BluetoothDevice device);
    void ConnectThenDoWith();
    void DisConnectThenDoWith();
    void ReceiveNotificationThenDoWith(byte[] bytes);
    void DiscoveredServicesThenDo(BluetoothGatt gatt);
}
