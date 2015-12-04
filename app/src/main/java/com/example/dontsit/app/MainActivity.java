package com.example.dontsit.app;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.example.dontsit.app.SitTimeActivity.SitTimeActivity;

import java.text.ParseException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DataAlwaysChanged {

    private CushionUpdateService mService;
    private Boolean isDataCompelete = false;
    private boolean bound = false;
    private boolean enable = false;
    private final static int REQUEST_ENABLE_BT = 1;
    private Animation animation_clicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDebugMode();
        initData();
        animation_clicked = AnimationUtils.loadAnimation(this, R.anim.image_click);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkBLE();
        Intent intent = new Intent(this, CushionUpdateService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
    }

    private void progressToNextInit() {
        initService();
    }

    private void checkBLE() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showRequestMessage("系統錯誤", "不支援低耗藍牙", null);
            finish();
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            showRequestMessage("系統錯誤", "藍牙服務未開啟，需開啟才能繼續執行。", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            });
        } else {
            enable = true;
            progressToNextInit();
        }
    }

    private void showRequestMessage(String title, String message, DialogInterface.OnClickListener NegativeButtonListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        if (NegativeButtonListener != null)
            builder.setNegativeButton("確定", NegativeButtonListener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void notifyDataChanged() {
        //call every activity which use database;
        DebugTools.Log("NotifyDataChanged");
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
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService.setCallbacks(null); // unregister
            bound = false;
        }
    };

    public void goSitTimePage(View view) {
        view.startAnimation(animation_clicked);
        Intent intent = new Intent(this, SitTimeActivity.class);
        startActivity(intent);
    }

}
