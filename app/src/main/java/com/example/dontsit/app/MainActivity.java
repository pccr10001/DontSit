package com.example.dontsit.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.text.ParseException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DataAlwaysChanged{

    private CushionUpdateService mService;
    private Boolean isDataCompelete = false;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDebugMode();
        initService();

        new initDataTask().execute();
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(this, CushionUpdateService.class);
        stopService(intent);
        super.onDestroy();
    }

    private void initService() {
        Intent intent = new Intent(this, CushionUpdateService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
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
    protected void onStart() {
        super.onStart();
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

    class initDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
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
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            isDataCompelete = true;
        }
    }

    /** Callbacks for service binding, passed to bindService() */
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

}
