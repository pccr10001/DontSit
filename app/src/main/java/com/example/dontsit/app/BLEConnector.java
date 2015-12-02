package com.example.dontsit.app;

import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class BLEConnector {

    private Context mCallback;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private final static int REQUEST_ENABLE_BT = 1;
    private boolean mScanning = false;
    private BluetoothLeScanner scanner;
    private List<ScanFilter> filters;
    private ScanSettings settings;
    private int ScanMode = ScanSettings.SCAN_MODE_LOW_POWER;
    private BluetoothDevice Target_Device;
    private String Target_MAC;

    public BLEConnector(BluetoothAdapter adapter, Context connectible) {
        mBluetoothAdapter = adapter;
        mCallback = connectible;
        initBLEScanner();
        initFilters();
        initSettings(ScanMode);
    }

    private void initBLEScanner() {
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private void initSettings(int mode) {
        settings = new ScanSettings.Builder()
                .setScanMode(mode)
                .build();
    }

    public void setScanMode(int mode) {
        ScanMode = mode;
    }

    private void initFilters() {
        filters = new ArrayList<ScanFilter>();
    }

    public void setTarget_MAC(String mac) {
        Target_MAC = mac;
        filters.add(new ScanFilter.Builder().setDeviceAddress(mac).build());
    }

    public void ScanWith(boolean bool) {
        if (scanner == null) {
            initBLEScanner();
        }

        if (bool) {
            mScanning = true;
            scanner.startScan(filters, settings, mLeScanCallback);
        } else {
            mScanning = false;
            scanner.stopScan(mLeScanCallback);
        }
    }

    public void Connect(BluetoothDevice device) {
        if (Target_MAC != null) {
            Target_Device = device;
            initBluetoothGatt();
        }
    }

    public void DisConnect() {
        mBluetoothGatt.disconnect();
    }

    public void DiscoverServices() {
        mBluetoothGatt.discoverServices();
    }

    private void initBluetoothGatt() {
        if (mBluetoothGatt == null)
            mBluetoothGatt = Target_Device.connectGatt(mCallback, false, mBluetoothGattCallback);
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

        }

        @Override
        public void onScanFailed(int errorCode) {

        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (mScanning) {
                ((BLEConnectible) mCallback).ScanResultThenDoWith(result.getDevice());
            }
        }
    };

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            ((BLEConnectible) mCallback).ReceiveNotificationThenDoWith(characteristic.getValue());
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                ((BLEConnectible) mCallback).ConnectThenDoWith();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            ((BLEConnectible) mCallback).DiscoveredServicesThenDo(gatt);
        }
    };
}
