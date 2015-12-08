package com.example.dontsit.app.Common;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

public interface BLEConnectible {
    void ScanResultThenDoWith(BluetoothDevice device);
    void ReceiveNotificationThenDoWith(byte[] bytes);
    void DiscoveredServicesThenDo(BluetoothGatt gatt);
    void ConnectionStateChangedThenDo(int state);
}
