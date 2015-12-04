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
import com.example.dontsit.app.CushionStateActivity.CushionStateActivity;
import com.example.dontsit.app.SettingActivity.SettingActivity;
import com.example.dontsit.app.SitTimeActivity.SitTimeActivity;

import java.text.ParseException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DataAlwaysChanged {

    private CushionUpdateService mService;
    private Boolean isDataCompelete = false;
    private boolean bound = false;
    private final static int REQUEST_ENABLE_BT = 1;
    private Animation animation_clicked;
    private MenuItem action_operation;
    private String mMac;

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
            showRequestMessage(getString(R.string.system_error), getString(R.string.enableBLE), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            });
        } else {
            initService();
        }
    }

    private void showRequestMessage(String title, String message,
                                    DialogInterface.OnClickListener NegativeButtonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        if (NegativeButtonListener != null)
            builder.setNegativeButton(getString(R.string.yes), NegativeButtonListener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mService.stopScan();
    }

    @Override
    protected void onDestroy() {
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
        Intent intent = new Intent(this, CushionUpdateService.class);
        stopService(intent);
        super.onDestroy();
    }

    private void initService() {
        if (mService == null) {
            DebugTools.Log("initService");
            Intent intent = new Intent(this, CushionUpdateService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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
        adapter = new ScanListViewAdapter(this, mService.getResults());
        devices.setAdapter(adapter);
        builder.setView(view);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void chooseMac(String mac) {
        mMac = mac;
        if (mService != null) {
//            DebugTools.Log("Choose Device");
            mService.connect(mMac);
            action_operation.setTitle(getString(R.string.connect));
            dialog.cancel();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        action_operation = menu.findItem(R.id.action_operation);
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
            if (item.getTitle().equals(getString(R.string.scan))) {
                checkBLE();
                if (mMac == null && mService != null)
                    initDialog();
            } else if (item.getTitle().equals(getString(R.string.connect))
                    && mService != null) {
                mService.connect(mMac);
            } else if (item.getTitle().equals(getString(R.string.disconnect))
                    && mService != null) {
                mService.disconnect();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void notifyDataChanged() {
        //call every activity which use database;
        DebugTools.Log("NotifyDataChanged");
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
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            // cast the IBinder and get MyService instance
            CushionUpdateService.LocalBinder binder = (CushionUpdateService.LocalBinder) iBinder;
            mService = binder.getService();
            mService.setCallbacks(MainActivity.this); // register
            bound = true;
            if (mMac == null)
                initDialog();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService.setCallbacks(null); // unregister
            bound = false;
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
