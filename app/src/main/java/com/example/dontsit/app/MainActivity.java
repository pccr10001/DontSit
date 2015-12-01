package com.example.dontsit.app;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.text.ParseException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Boolean isDataCompelete = false;
    private Boolean isDebug;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDebugMode();

        new initDataTask().execute();
    }

    private void initDebugMode() {
        Resources resources = getResources();
        isDebug = resources.getBoolean(R.bool.debug);
        if (isDebug) {
            TAG = resources.getString(R.string.tag);
        }
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

    class initDataTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            CushionStateDAO stateDAO = new CushionStateDAO(getApplicationContext());
            DurationLogDAO logDAO = new DurationLogDAO(getApplicationContext());

            // 如果資料庫是空的，就建立一些範例資料
            // 這是為了方便測試用的，完成應用程式以後可以拿掉
            try {
                if (stateDAO.getCount() == 0)
                    stateDAO.generate();
                if (logDAO.getCount() == 0)
                    logDAO.generate();
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
            if (isDebug) {
                if (states != null) {
                    if (states.size() > 0)
                        Log.i(TAG, states.get(0).toString());
                } else Log.i(TAG, "Database is NULL");
                if (durations != null) {
                    if (durations.size() > 0) {
                        for (Duration duration : durations) {
                            Log.i(TAG, duration.toString());
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
}
