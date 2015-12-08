package com.example.dontsit.app;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import com.example.dontsit.app.Common.DateFormatter;
import com.example.dontsit.app.Common.DebugTools;
import com.example.dontsit.app.Common.NotSitSharedPreferences;
import com.example.dontsit.app.CushionStateActivity.CushionStateActivity;
import com.example.dontsit.app.Main.AlarmService;
import com.example.dontsit.app.Main.CushionUpdateService;
import com.example.dontsit.app.Main.DataAlwaysChanged;
import com.example.dontsit.app.Main.ScanListViewAdapter;
import com.example.dontsit.app.SettingActivity.SettingActivity;
import com.example.dontsit.app.SitTimeActivity.SitTimeActivity;
import com.rey.material.app.SimpleDialog;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements DataAlwaysChanged {

    private CushionUpdateService mBleService;
    private AlarmService mAlarmService;
    private boolean BLEServiceBound = false;
    private boolean AlarmServiceBound = false;
    private final static int REQUEST_ENABLE_BT = 1;
    private Animation animation_clicked;
    private MenuItem action_operation;
    private String mMac;
    private NotSitSharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preferences = new NotSitSharedPreferences(this);
        initDebugMode();
        initCushionState();
        //initData();
        animation_clicked = AnimationUtils.loadAnimation(this, R.anim.image_click);
    }

    private void checkBLE() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showRequestMessage(getString(R.string.system_error), getString(R.string.notSupportBLE), null);
            finish();
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            showRequestMessage(getString(R.string.system_error), getString(R.string.enableBLE), new DialogAction() {
                @Override
                public void Action() {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            });
        } else {
            initBLEService();
            initAlarmService();
        }
    }

    private interface DialogAction {
        void Action();
    }

    private void showRequestMessage(String title, String message, final DialogAction listener) {
        final SimpleDialog builder = new SimpleDialog(this, R.style.SimpleDialog);
        builder.messageTextAppearance(R.styleable.SimpleDialog_di_messageTextAppearance)
                .message(message)
                .title(title)
                .negativeAction(getString(R.string.yes))
                .negativeActionClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.Action();
                        builder.cancel();
                    }
                })
                .show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBleService != null)
            mBleService.stopScan();
    }

    @Override
    protected void onDestroy() {
        if (BLEServiceBound) {
            unbindService(BluetoothServiceConnection);
            BLEServiceBound = false;
        }
        if (AlarmServiceBound) {
            unbindService(AlarmServiceConnection);
            AlarmServiceBound = false;
        }
        Intent intent = new Intent(this, CushionUpdateService.class);
        stopService(intent);
        intent = new Intent(this, AlarmService.class);
        stopService(intent);
        super.onDestroy();
    }

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

    private void initCushionState() {
        String temp = preferences.get(NotSitSharedPreferences.MAC);
        mMac = temp.equals("") ? null : temp;
    }

    BaseAdapter adapter;
    ListView devices;
    AlertDialog dialog;

    private void initDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.ScanDialog));
        View view = getLayoutInflater().inflate(R.layout.dialog_scan, null);
        devices = (ListView) view.findViewById(R.id.ScanListView);
        adapter = new ScanListViewAdapter(this, mBleService.getResults());
        devices.setAdapter(adapter);
        builder.setView(view);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void chooseMac(String mac) {
        mMac = mac;
        if (mBleService != null) {
//            DebugTools.Log("Choose Device");
            preferences.set(NotSitSharedPreferences.MAC, mac);
            Date now = Calendar.getInstance().getTime();
            try {
                preferences.set(NotSitSharedPreferences.LastConnectTime, DateFormatter.format(now));
                preferences.set(NotSitSharedPreferences.LastNotifyTime, DateFormatter.format(now));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            preferences.set(NotSitSharedPreferences.LastTimeDuration, "0");
            preferences.set(NotSitSharedPreferences.IsSeated, "0");
            mBleService.connect(mMac);
            action_operation.setTitle(getString(R.string.connect));
            dialog.cancel();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        action_operation = menu.findItem(R.id.action_operation);
        if (mMac != null) {
            int state = Integer.valueOf(preferences.get(NotSitSharedPreferences.BLEState));
            switch (state) {
                case BluetoothGatt.STATE_CONNECTED:
                    action_operation.setTitle(getString(R.string.disconnect));
                    break;
                case BluetoothGatt.STATE_DISCONNECTED:
                    action_operation.setTitle(getString(R.string.connect));
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
            checkBLE();
            if (item.getTitle().equals(getString(R.string.scan))) {
                if (mMac == null && mBleService != null)
                    initDialog();
            } else if (item.getTitle().equals(getString(R.string.connect))
                    && mBleService != null) {
                mBleService.connect(mMac);
            } else if (item.getTitle().equals(getString(R.string.disconnect))
                    && mBleService != null) {
                mBleService.disconnect();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void notifyScanResult() {
        DebugTools.Log("NotifyDataSetChanged()");
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyConnectStateChanged(int state) {
        preferences.set(NotSitSharedPreferences.BLEState, String.valueOf(state));
        switch (state) {
            case BluetoothGatt.STATE_CONNECTED:
                DebugTools.Log("Connect Cushion");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        action_operation.setTitle(getString(R.string.disconnect));
                    }
                });
                break;
            case BluetoothGatt.STATE_DISCONNECTED:
                DebugTools.Log("Disconnect Cushion");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        action_operation.setTitle(getString(R.string.connect));
                    }
                });
                break;
        }
    }

    /**
     * Callbacks for service binding, passed to bindService()
     */
    private ServiceConnection BluetoothServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            // cast the IBinder and get MyService instance
            CushionUpdateService.LocalBinder binder = (CushionUpdateService.LocalBinder) iBinder;
            mBleService = binder.getService();
            mBleService.setCallbacks(MainActivity.this); // register
            BLEServiceBound = true;
            if (mMac == null)
                initDialog();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBleService.setCallbacks(null); // unregister
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
        view.startAnimation(animation_clicked);
        Intent intent = new Intent(this, CushionStateActivity.class);
        startActivity(intent);
    }

    public void goSitTimePage(View view) {
        view.startAnimation(animation_clicked);
        Intent intent = new Intent(this, SitTimeActivity.class);
        startActivity(intent);
    }

    public void goAlarmClockPage(View view) {
        view.startAnimation(animation_clicked);
        Intent intent = new Intent(this, AlarmClockActivity.class);
        startActivity(intent);
    }

    public void goAchievementPage(View view) {
        view.startAnimation(animation_clicked);
        Intent intent = new Intent(this, AchievementActivity.class);
        startActivity(intent);
    }

    public void goSettingPage(View view) {
        view.startAnimation(animation_clicked);
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }
}
