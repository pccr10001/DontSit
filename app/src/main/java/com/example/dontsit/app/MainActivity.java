package com.example.dontsit.app;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
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
import com.example.dontsit.app.AlarmClockActivity.AlarmService;
import com.example.dontsit.app.Common.*;
import com.example.dontsit.app.CushionStateActivity.CushionStateActivity;
import com.example.dontsit.app.SettingActivity.SettingActivity;
import com.example.dontsit.app.SitTimeActivity.SitTimeActivity;
import com.rey.material.app.SimpleDialog;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DataAlwaysChanged {

    private CushionUpdateService mBleService;
    private AlarmService mAlarmService;
    private Boolean isDataCompelete = false;
    private boolean BLEServiceBound = false;
    private boolean AlarmServiceBound = false;
    private final static int REQUEST_ENABLE_BT = 1;
    private Animation animation_clicked;
    private MenuItem action_operation;
    private String mMac;
    private CushionStateDAO stateDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        try {
            CushionStateDAO dao = new CushionStateDAO(this);
            List<CushionState> states;
            states = dao.getAll();
            if (states.size() > 0)
                mMac = states.get(0).getMAC();
            dao.close();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
            CushionState state = new CushionState();
            state.setMAC(mac);
            Date now = Calendar.getInstance().getTime();
            state.setLastConnectTime(now);
            state.setLastNotifyTime(now);
            state.setLastTimeDuration(0);
            state.setSeated(false);
            stateDAO = new CushionStateDAO(this);
            try {
                if (stateDAO.getCount() == 0)
                    stateDAO.insert(state);
                else
                    stateDAO.update(state);
            } catch (ParseException e) {
                e.printStackTrace();
            }
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
        if (mMac != null)
            action_operation.setTitle(getString(R.string.connect));
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
    public void notifyConnect() {
        DebugTools.Log("Connect Cushion");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                action_operation.setTitle(getString(R.string.disconnect));
            }
        });
    }

    @Override
    public void notifyDisconnect() {
        DebugTools.Log("Disconnect Cushion");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                action_operation.setTitle(getString(R.string.connect));
            }
        });
    }

    @Override
    public void notifyScanResult() {
        DebugTools.Log("NotifyDataSetChanged()");
        DebugTools.Log(adapter == null);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void initData() {
        if (DebugTools.isDebugged()) {
            CushionStateDAO stateDAO = new CushionStateDAO(getApplicationContext());
            DurationLogDAO logDAO = new DurationLogDAO(getApplicationContext());

            // 如果資料庫是空的，就建立一些範例資料
            // 這是為了方便測試用的，完成應用程式以後可以拿掉
            try {
                if (stateDAO.getCount() == 0)
                    stateDAO.generate();
                if (logDAO.getCount() == 0)
                    ;//logDAO.generate();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            List<CushionState> states = null;
            List<Duration> durations = null;
            try {
                states = stateDAO.getAll();
                durations = logDAO.getAll();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (states != null) {
                if (states.size() > 0)
                    DebugTools.Log(states.get(0).toString());
            } else DebugTools.Log("Database is NULL");
            if (durations != null) {
                if (durations.size() > 0) {
                    for (Duration duration : durations) {
                        DebugTools.Log(duration.toString());
                    }
                }
            }
            stateDAO.close();
            logDAO.close();
        }
        isDataCompelete = true;
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
