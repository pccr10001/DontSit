package com.example.dontsit.app;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.ScanSettings;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.example.dontsit.app.AchievementActivity.AchievementActivity;
import com.example.dontsit.app.AlarmClockActivity.AlarmClockActivity;
import com.example.dontsit.app.Common.*;
import com.example.dontsit.app.CushionStateActivity.CushionStateActivity;
import com.example.dontsit.app.Database.BLEStateChangedReceiver;
import com.example.dontsit.app.Main.AlarmService;
import com.example.dontsit.app.Main.CushionUpdateService;
import com.example.dontsit.app.Main.ScanListViewAdapter;
import com.example.dontsit.app.SettingActivity.SettingActivity;
import com.example.dontsit.app.SitTimeActivity.SitTimeActivity;
import com.rey.material.app.SimpleDialog;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BLEConnectible {

    private CushionUpdateService mBleService;
    private AlarmService mAlarmService;
    private boolean BLEServiceBound = false;
    private boolean AlarmServiceBound = false;
    private final static int REQUEST_ENABLE_BT = 1;
    private Animation ClickedAnimation;
    private MenuItem MenuOperationItem;
    private String mMac;
    private NotSitSharedPreferences mPreferences;

    private BLEConnector FirstCheckConnector;
    private int ScanMode = ScanSettings.SCAN_MODE_LOW_POWER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPreferences();
        initDebugMode();
        registerReceiver(mReceiver, mFilter);
    }

    private void initPreferences() {
        mPreferences = new NotSitSharedPreferences(this);
        String mode = mPreferences.get(NotSitSharedPreferences.ScanMode);
        ScanMode = mode.equals("") ? 0 : Integer.valueOf(mode);
        mMac = mPreferences.get(NotSitSharedPreferences.MAC);
        ClickedAnimation = AnimationUtils.loadAnimation(this, R.anim.image_click);
    }

    private void checkBLE() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showRequestMessage(getString(R.string.system_error), getString(R.string.notSupportBLE), null);
            finish();
        }

        //Start Setting
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            showRequestMessage(getString(R.string.system_error),
                    getString(R.string.enableBLE), new DialogAction() {
                @Override
                public void Action() {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            });
        } else if (!mMac.equals("")) {
            initBLEService();
            initAlarmService();
        } else {
            FirstCheckConnector = new BLEConnector(adapter, this);
            FirstCheckConnector.setScanMode(ScanMode);
            FirstCheckConnector.ScanWith(true);
            initDialog();
        }
    }

    private interface DialogAction {
        void Action();
    }

    private void showRequestMessage(String title, String message, final DialogAction listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(title)
                .setNegativeButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.Action();
                    }
                })
                .show();
    }

    @Override
    public void ScanResultThenDoWith(BluetoothDevice device) {
        if (!ScanResult.contains(device)) {
            ScanResult.add(device);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void ReceiveNotificationThenDoWith(byte[] bytes) {
        //do nothing
    }

    @Override
    public void DiscoveredServicesThenDo(BluetoothGatt gatt) {
        //do nothing
    }

    @Override
    public void ConnectionStateChangedThenDo(int state) {
        //do nothing
    }

    private IntentFilter mFilter = new IntentFilter(BLEStateChangedReceiver.ACTION_STATE_CHANGED);

    private BLEStateChangedReceiver mReceiver = new BLEStateChangedReceiver() {
        public void onReceive(Context context, Intent intent) {
//            DebugTools.Log(mPreferences.get(NotSitSharedPreferences.BLEState));
            int state = Integer.valueOf(mPreferences.get(NotSitSharedPreferences.BLEState));
            switch (state) {
                case BluetoothGatt.STATE_CONNECTED:
                    MenuOperationItem.setTitle(getString(R.string.disconnect));
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    MenuOperationItem.setTitle(getString(R.string.connect));
                    break;
            }
        }
    };

    private void initBLEService() {
        if (mBleService == null) {
            DebugTools.Log("initBLEService");
            Intent intent = new Intent(this, CushionUpdateService.class);
            bindService(intent, BluetoothServiceConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
        }
    }

    private void initAlarmService() {
        if (mAlarmService == null) {
            DebugTools.Log("initAlarmService");
            Intent intent = new Intent(this, AlarmService.class);
            bindService(intent, AlarmServiceConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
        }
    }

    private void initDebugMode() {
        DebugTools.initTools(getResources());
    }

    private BaseAdapter adapter;
    private AlertDialog dialog;
    private List<BluetoothDevice> ScanResult = new ArrayList<BluetoothDevice>();

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.ScanDialog));
        View view = getLayoutInflater().inflate(R.layout.dialog_scan, null);
        ListView devices = (ListView) view.findViewById(R.id.ScanListView);
        adapter = new ScanListViewAdapter(this, ScanResult);
        devices.setAdapter(adapter);
        builder.setView(view);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void chooseMac(String mac) {
        Date now = Calendar.getInstance().getTime();
        mMac = mac;
        try {
            mPreferences.set(NotSitSharedPreferences.MAC, mac);
            mPreferences.set(NotSitSharedPreferences.LastConnectTime, DateFormatter.format(now));
            mPreferences.set(NotSitSharedPreferences.LastNotifyTime, DateFormatter.format(now));
            mPreferences.set(NotSitSharedPreferences.LastTimeDuration, "0");
            mPreferences.set(NotSitSharedPreferences.IsSeated, "0");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dialog.cancel();
        dialog.dismiss();
        FirstCheckConnector.ScanWith(false);
        FirstCheckConnector = null;

        checkBLE();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuOperationItem = menu.findItem(R.id.action_operation);
        if (!mMac.equals("")) {
            int state = Integer.valueOf(mPreferences.get(NotSitSharedPreferences.BLEState));
            switch (state) {
                case BluetoothGatt.STATE_CONNECTED:
                    MenuOperationItem.setTitle(getString(R.string.disconnect));
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    MenuOperationItem.setTitle(getString(R.string.connect));
                    break;
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_operation) {
            if (item.getTitle().equals(getString(R.string.disconnect))) {
                DebugTools.Log("Stop Service");
                if (BluetoothServiceConnection != null)
                    unbindService(BluetoothServiceConnection);
                Intent intent = new Intent(MainActivity.this, CushionUpdateService.class);
                stopService(intent);
                mBleService = null;
                if (AlarmServiceConnection != null)
                    unbindService(AlarmServiceConnection);
                intent = new Intent(MainActivity.this, AlarmService.class);
                stopService(intent);
                mAlarmService = null;
            } else {
                checkBLE();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private ServiceConnection BluetoothServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            // cast the IBinder and get MyService instance
            CushionUpdateService.LocalBinder binder = (CushionUpdateService.LocalBinder) iBinder;
            mBleService = binder.getService();
            BLEServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            BLEServiceBound = false;
        }
    };

    private ServiceConnection AlarmServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AlarmService.LocalBinder binder = (AlarmService.LocalBinder) service;
            mAlarmService = binder.getService();
            AlarmServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            AlarmServiceBound = false;
        }
    };

    public void goCushionStatePage(View view) {
        view.startAnimation(ClickedAnimation);
        Intent intent = new Intent(this, CushionStateActivity.class);
        startActivity(intent);
    }

    public void goSitTimePage(View view) {
        view.startAnimation(ClickedAnimation);
        Intent intent = new Intent(this, SitTimeActivity.class);
        startActivity(intent);
    }

    public void goAlarmClockPage(View view) {
        view.startAnimation(ClickedAnimation);
        Intent intent = new Intent(this, AlarmClockActivity.class);
        startActivity(intent);
    }

    public void goAchievementPage(View view) {
        view.startAnimation(ClickedAnimation);
        Intent intent = new Intent(this, AchievementActivity.class);
        startActivity(intent);
    }

    public void goSettingPage(View view) {
        view.startAnimation(ClickedAnimation);
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }
}
