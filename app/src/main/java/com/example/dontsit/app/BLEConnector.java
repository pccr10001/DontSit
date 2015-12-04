package com.example.dontsit.app;

import android.annotation.TargetApi;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class BLEConnector {

    private Context mCallback;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private boolean mScanning = false;
    private BluetoothLeScanner scanner;
    private List<ScanFilter> filters;
    private ScanSettings settings;
    private int ScanMode = ScanSettings.SCAN_MODE_LOW_POWER;
    private BluetoothDevice Target_Device;
    private String Target_MAC;

    private ScanCallback mLeScanCallback;
    private BluetoothGattCallback mBluetoothGattCallback;

    public BLEConnector(BluetoothAdapter adapter, Context connectible) {
        mBluetoothAdapter = adapter;
        mCallback = connectible;
        initScanCallBack();
        initGattCallBack();
        initBLEScanner();
        initFilters();
        initSettings(ScanMode);
    }

    private void initBLEScanner() {
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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

    private void initScanCallBack() {
        mLeScanCallback = new ScanCallback() {

            @Override
            public void onBatchScanResults(List<ScanResult> results) {

            }

            @Override
            public void onScanFailed(int errorCode) {
                DebugTools.Log("ScanFailed : " + errorCode);
            }

            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                if (mScanning) {
                    ((BLEConnectible) mCallback).ScanResultThenDoWith(result.getDevice());
                }
            }
        };
    }

    private void initGattCallBack() {
        mBluetoothGattCallback = new BluetoothGattCallback() {

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                ((BLEConnectible) mCallback).ReceiveNotificationThenDoWith(characteristic.getValue());
            }

            @Override
            public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
                switch (newState) {
                    case BluetoothGatt.STATE_CONNECTED:
                        ((BLEConnectible) mCallback).ConnectThenDoWith();
                        DebugTools.Log("Connected");
                        break;
                    case BluetoothGatt.STATE_CONNECTING:
                        DebugTools.Log("Connecting");
                        break;
                    case BluetoothGatt.STATE_DISCONNECTING:
                        DebugTools.Log("DisConnecting");
                        break;
                    case BluetoothGatt.STATE_DISCONNECTED:
                        ((BLEConnectible) mCallback).DisConnectThenDoWith();
                        DebugTools.Log("DisConnected");
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                ((BLEConnectible) mCallback).DiscoveredServicesThenDo(gatt);
            }


        };
    }

    public void setTarget_MAC(String mac) {
        Target_MAC = mac;
        filters.add(new ScanFilter.Builder().setDeviceAddress(mac).build());
    }

    public void ScanWith(boolean bool) {
        if (scanner == null) {
            initBLEScanner();
            return;
        }

        if (mLeScanCallback == null) {
            initScanCallBack();
            return;
        }

        if (bool && !mScanning) {
            mScanning = true;
            scanner.startScan(filters, settings, mLeScanCallback);
            return;
        }
        if (!bool && mScanning) {
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
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt = null;
        }
    }

    public void DiscoverServices() {
        mBluetoothGatt.discoverServices();
    }

    private void initBluetoothGatt() {
        if (mBluetoothGatt == null)
            mBluetoothGatt = Target_Device.connectGatt(mCallback, true, mBluetoothGattCallback);
    }

}
