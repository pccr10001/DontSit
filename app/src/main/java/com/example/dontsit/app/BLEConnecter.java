package com.example.dontsit.app;

import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by IDIC on 2015/12/1.
 */
public class BLEConnecter {
    private AppCompatActivity parent;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private final static int REQUEST_ENABLE_BT = 1;
    private boolean mScanning = false;
    private boolean isEnabled = false;
    private BluetoothLeScanner scanner;
    private List<ScanFilter> filters;
    private ScanSettings settings;
    private int ScanMode = ScanSettings.SCAN_MODE_LOW_POWER;
    private BluetoothDevice Target_Device;
    private String Target_MAC;
    private HashSet<BluetoothDevice> devices = new HashSet<BluetoothDevice>();

    public BLEConnecter(AppCompatActivity parent) {
        this.parent = parent;
        checkBLEEnabled();
        initBLEScanner();
        initFilters();
        initSettings(ScanMode);
    }

    public void checkBLEEnabled() {
        if (!parent.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            isEnabled = false;
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            parent.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else isEnabled = true;
    }

    private void initBLEScanner() {
        mBluetoothManager = (BluetoothManager) parent.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
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

    private void setTarget(String mac) {
        Target_MAC = mac;
        filters.add(new ScanFilter.Builder().setDeviceAddress(mac).build());
    }

    private void ScanWith(boolean IsStarted) {
        if (scanner == null) {
            initBLEScanner();
        }

        if (IsStarted) {
            mScanning = true;
            scanner.startScan(filters, settings, mLeScanCallback);
        } else {
            mScanning = false;
            scanner.stopScan(mLeScanCallback);
        }
    }

    private void initBluetoothGatt() {
        if (mBluetoothGatt == null) mBluetoothGatt = Target_Device.connectGatt(parent, false, mBluetoothGattCallback);
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
                devices.add(result.getDevice());
                ((BLEConnectable) parent).ScanResultThenDo();
            }
        }
    };

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                ((BLEConnectable) parent).ConnectThenDo();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
        }
    };
}
