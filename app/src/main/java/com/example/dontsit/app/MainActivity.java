package com.example.dontsit.app;

import android.app.Activity;
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
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.example.dontsit.app.CheckActivity.CheckActivity;
import com.example.dontsit.app.AlarmClockActivity.AlarmClockActivity;
import com.example.dontsit.app.AnalysisActivity.AnalysisActivity;
import com.example.dontsit.app.Common.*;
import com.example.dontsit.app.CushionStateActivity.CushionStateActivity;
import com.example.dontsit.app.Database.*;
import com.example.dontsit.app.Main.AlarmService;
import com.example.dontsit.app.Main.CushionUpdateService;
import com.example.dontsit.app.Main.ScanListViewAdapter;
import com.example.dontsit.app.SettingActivity.SettingActivity;
import com.example.dontsit.app.SitTimeActivity.SitTimeActivity;

import java.text.ParseException;
import java.util.*;

public class MainActivity extends AppCompatActivity implements BLEConnectible {

    private CushionUpdateService mBleService;
    private AlarmService mAlarmService;
    private boolean BLEServiceBound = false;
    private boolean AlarmServiceBound = false;
    private final static int REQUEST_ENABLE_BT = 1;
    private Animation ClickedAnimation;
    private String mMac;
    private NotSitSharedPreferences mPreferences;

    private BLEConnector FirstCheckConnector;
    private int ScanMode = ScanSettings.SCAN_MODE_LOW_POWER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        initPreferences();
        initDebugMode();
        refreshLog();
        registerReceiver(mBLEStateReceiver, mBLEFilter);
    }

    private void initToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initPreferences() {
        mPreferences = new NotSitSharedPreferences(this);
        String mode = mPreferences.get(NotSitSharedPreferences.ScanMode);
        ScanMode = mode.equals("") ? 0 : Integer.valueOf(mode);
        mMac = mPreferences.get(NotSitSharedPreferences.MAC);
        ClickedAnimation = AnimationUtils.loadAnimation(this, R.anim.image_click);
    }

    private void initDebugMode() {
        DebugTools.initTools(getResources());
    }

    @Override
    protected void onResume() {
        if (mPreferences.get(NotSitSharedPreferences.IsChanged).equals("1")) {
            if (mPreferences.get(NotSitSharedPreferences.MAC).equals("")) {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                unbindService();
                StopService();
                mMac = null;
                if (adapter == null || !adapter.isEnabled()) {
                    ShowMenuItem(EnableIndex);
                } else {
                    ShowMenuItem(ScanIndex);
                }
                mPreferences.set(NotSitSharedPreferences.IsChanged, "0");
            }
        }
        super.onResume();
    }

    private void checkBLE() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showRequestMessage(getString(R.string.system_error), getString(R.string.notSupportBLE), null);
            finish();
        }

        //Start Setting
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            ShowMenuItem(EnableIndex);
            showRequestMessage(getString(R.string.system_error),
                    getString(R.string.enableBLE), new DialogAction() {
                        @Override
                        public void Action() {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        }
                    });
        } else {
            if (!mMac.equals(""))
                BluetoothStateAction(Integer.valueOf(mPreferences.get(NotSitSharedPreferences.BLEState)));
            else
                ShowMenuItem(ScanIndex);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    checkBLE();
                }
                break;
        }
    }

    private void StartScan() {
        if (mMac != null) {
            if (!mMac.equals("")) {
                initBLEService();
                initAlarmService();
                return;
            }
        }
        FirstCheckConnector = new BLEConnector(BluetoothAdapter.getDefaultAdapter(), this);
        FirstCheckConnector.setScanMode(ScanMode);
        FirstCheckConnector.ScanWith(true);
        initDialog();
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

    private IntentFilter mBLEFilter = new IntentFilter(BLEStateChangedReceiver.ACTION_STATE_CHANGED);

    private BLEStateChangedReceiver mBLEStateReceiver = new BLEStateChangedReceiver() {
        public void onReceive(Context context, Intent intent) {
//            DebugTools.Log(mPreferences.get(NotSitSharedPreferences.BLEState));
            BluetoothStateAction(Integer.valueOf(mPreferences.get(NotSitSharedPreferences.BLEState)));
        }
    };

    private void initBLEService() {
        if (mBleService == null) {
            DebugTools.Log("bindBLEService");
            Intent intent = new Intent(this, CushionUpdateService.class);
            bindService(intent, BluetoothServiceConnection, Context.BIND_AUTO_CREATE);
            if (!BLEServiceBound) {
                DebugTools.Log("initBLEService");
                startService(intent);
            }
        }
    }

    private void initAlarmService() {
        if (mAlarmService == null) {
            DebugTools.Log("bindAlarmService");
            Intent intent = new Intent(this, AlarmService.class);
            bindService(intent, AlarmServiceConnection, Context.BIND_AUTO_CREATE);
            if (!AlarmServiceBound) {
                DebugTools.Log("initAlarmService");
                startService(intent);
            }
        }
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

        StartScan();
    }

    private MenuItem MenuEnableItem;
    private MenuItem MenuScanItem;
    private MenuItem MenuConnectItem;
    private MenuItem MenuDisconnectItem;

    private static int EnableIndex = 0;
    private static int ScanIndex = 1;
    private static int ConnectIndex = 2;
    private static int DisconnectIndex = 3;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuEnableItem = menu.findItem(R.id.action_bluetooth);
        MenuScanItem = menu.findItem(R.id.action_scan);
        MenuConnectItem = menu.findItem(R.id.action_connect);
        MenuDisconnectItem = menu.findItem(R.id.action_disconnect);

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled())
            ShowMenuItem(EnableIndex);
        else {
            if (!mMac.equals(""))
                BluetoothStateAction(Integer.valueOf(mPreferences.get(NotSitSharedPreferences.BLEState)));
            else
                ShowMenuItem(ScanIndex);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_bluetooth:
                checkBLE();
                break;
            case R.id.action_scan:
                StartScan();
                break;
            case R.id.action_connect:
                StartScan();
                break;
            case R.id.action_disconnect:
                unbindService();
                StopService();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void BluetoothStateAction(int state) {
        switch (state) {
            case BluetoothGatt.STATE_CONNECTED:
                ShowMenuItem(DisconnectIndex);
                break;
            case BluetoothGatt.STATE_DISCONNECTED:
                if (!mPreferences.get(NotSitSharedPreferences.MAC).equals(""))
                    ShowMenuItem(ConnectIndex);
                break;
        }
    }

    private void ShowMenuItem(int index) {
        MenuEnableItem.setVisible(false);
        MenuScanItem.setVisible(false);
        MenuConnectItem.setVisible(false);
        MenuDisconnectItem.setVisible(false);

        switch (index) {
            case 0:
                MenuEnableItem.setVisible(true);
                break;
            case 1:
                MenuScanItem.setVisible(true);
                break;
            case 2:
                MenuConnectItem.setVisible(true);
                break;
            case 3:
                MenuDisconnectItem.setVisible(true);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
        StopService();
        unregisterReceiver(mBLEStateReceiver);
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
        Intent intent = new Intent(this, CheckActivity.class);
        startActivity(intent);
    }

    public void goSettingPage(View view) {
        view.startAnimation(ClickedAnimation);
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    public void goAnalysisPage(View view) {
        view.startAnimation(ClickedAnimation);
        Intent intent = new Intent(this, AnalysisActivity.class);
        startActivity(intent);
    }

    private void unbindService() {
        if (mBleService != null && BLEServiceBound)
            unbindService(BluetoothServiceConnection);
        if (mAlarmService != null && AlarmServiceBound)
            unbindService(AlarmServiceConnection);
    }

    private void StopService() {
        DebugTools.Log("Stop Service");

        Intent intent = new Intent(MainActivity.this, CushionUpdateService.class);
        stopService(intent);
        mBleService = null;

        intent = new Intent(MainActivity.this, AlarmService.class);
        stopService(intent);
        mAlarmService = null;
    }

    private void refreshLog() {
        try {
            DayDurationLogDAO logDayDAO = new DayDurationLogDAO(this);
            DurationLogDAO logDAO = new DurationLogDAO(this);

            Calendar SevenDayAge = Calendar.getInstance();
            SevenDayAge.setTimeZone(TimeZone.getDefault());
            SevenDayAge.set(Calendar.HOUR_OF_DAY, 0);
            SevenDayAge.set(Calendar.MINUTE, 0);
            SevenDayAge.set(Calendar.SECOND, 0);
            SevenDayAge.add(Calendar.DAY_OF_YEAR, -6);
            DebugTools.Log("*" + SevenDayAge.getTime());
            DebugTools.Log("*" + logDAO.getBefore(SevenDayAge.getTime()));

            if (logDAO.getBefore(SevenDayAge.getTime()).size() > 0) {
                SevenDayAge.add(Calendar.SECOND, -1);
                DayDuration duration = new DayDuration();
                duration.setSitTime(logDAO.getDayTimeAt(SevenDayAge.getTime()));
                duration.setChangeTime(logDAO.getDayTimesAt(SevenDayAge.getTime()));
                duration.setDate(SevenDayAge.getTime());
                logDayDAO.insert(duration);
                logDAO.deleteBefore(SevenDayAge.getTime());
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
