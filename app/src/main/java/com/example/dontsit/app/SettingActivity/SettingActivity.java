package com.example.dontsit.app.SettingActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.example.dontsit.app.Common.DebugTools;
import com.example.dontsit.app.Common.NotSitSharedPreferences;
import com.example.dontsit.app.Database.DayDurationLogDAO;
import com.example.dontsit.app.Database.DurationLogDAO;
import com.example.dontsit.app.R;
import com.rey.material.widget.Switch;


public class SettingActivity extends AppCompatActivity {

    private Spinner ScanModeSpinner;
    private TextView ClockSoundPathEditText;
    private TextView CushionMacEditText;
    private Switch DefaultSettingSwitch;
    private Switch ResetDataSwitch;
    private NotSitSharedPreferences preferences;
    private boolean IsChanged = false;

    private int ScanMode;
    private String ClockPath;
    private String CushionMac;
    MagicFileChooser chooser;

    private void initToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initToolbar();

        preferences = new NotSitSharedPreferences(this);
        ScanModeSpinner = (Spinner) findViewById(R.id.spinner_label);
        String[] items = new String[]{
                getString(R.string.ScanModeLowFrequency),
                getString(R.string.ScanModeBalanced),
                getString(R.string.ScanModeLowLatency)};
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_spn, items);
        adapter.setDropDownViewResource(R.layout.row_spn_dropdown);

        ScanModeSpinner.setAdapter(adapter);
        ScanModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                IsChanged = preferences.set(NotSitSharedPreferences.ScanMode, String.valueOf(position));
                setScanMode();
                setDefaultSettingSwitch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ClockSoundPathEditText = (TextView) findViewById(R.id.ClockSoundPathEditText);
        ClockSoundPathEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooser = new MagicFileChooser(SettingActivity.this);
                chooser.showFileChooser("audio/*", "請選擇音效檔案", false);
                setDefaultSettingSwitch();
            }
        });

        CushionMacEditText = (TextView) findViewById(R.id.CushionMacEditText);

        DefaultSettingSwitch = (Switch) findViewById(R.id.DefaultSettingSwitch);
        DefaultSettingSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRequestMessage(getString(R.string.SureToResetSetting), new DialogAction() {
                    @Override
                    public void YesThenDo() {
                        preferences.clear(NotSitSharedPreferences.ScanMode);
                        preferences.clear(NotSitSharedPreferences.ClockSoundPath);
                        preferences.clear(NotSitSharedPreferences.MAC);

                        setScanMode();
                        setCushionMac();
                        setClockPath();

                        IsChanged = true;

                        DefaultSettingSwitch.setEnabled(false);
                    }

                    @Override
                    public void NoThenDo() {
                        DefaultSettingSwitch.setChecked(false);
                    }
                });
            }
        });

        ResetDataSwitch = (Switch) findViewById(R.id.ResetDataSwitch);
        ResetDataSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRequestMessage(getString(R.string.SureToClearLog), new DialogAction() {
                    @Override
                    public void YesThenDo() {
                        new DurationLogDAO(SettingActivity.this).removeAll();
                        new DayDurationLogDAO(SettingActivity.this).removeAll();
                        ResetDataSwitch.setChecked(true);
                        DefaultSettingSwitch.setEnabled(false);
                    }

                    @Override
                    public void NoThenDo() {
                        ResetDataSwitch.setChecked(false);
                    }
                });
            }

        });

        setScanMode();
        setCushionMac();
        setClockPath();
    }

    private void setScanMode() {
        ScanMode = Integer.valueOf(preferences.get(NotSitSharedPreferences.ScanMode));
        ScanModeSpinner.setSelection(ScanMode);
    }

    private void setClockPath() {
        ClockPath = preferences.get(NotSitSharedPreferences.ClockSoundPath);
        ClockSoundPathEditText.setText(
                ClockPath.equals(NotSitSharedPreferences.ClockSoundDefaultPath) ?
                        getString(R.string.Default) : ClockPath);
    }

    private void setCushionMac() {
        CushionMac = preferences.get(NotSitSharedPreferences.MAC);
        CushionMacEditText.setText(!CushionMac.equals("") ? CushionMac : "");
    }

    private void setDefaultSettingSwitch() {
        if (IsChanged) {
            DefaultSettingSwitch.setChecked(false);
            DefaultSettingSwitch.setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        chooser.onActivityResult(requestCode, resultCode, data);
        if (chooser.getChosenFiles() != null)
            if (chooser.getChosenFiles().length == 1) {
                IsChanged = preferences.set(NotSitSharedPreferences.ClockSoundPath,
                        chooser.getChosenFiles()[0].toString());
                setClockPath();
                DefaultSettingSwitch.setChecked(false);
                DefaultSettingSwitch.setEnabled(true);
            }
    }

    public void BackParent(View view) {
        onBackPressed();
    }

    private interface DialogAction {
        void YesThenDo();

        void NoThenDo();
    }

    private void showRequestMessage(String title, final DialogAction listener) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setNegativeButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.YesThenDo();
                    }
                })
                .setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.NoThenDo();
                    }
                })
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (IsChanged) {
            preferences.set(NotSitSharedPreferences.IsChanged, "1");
            DebugTools.Log(preferences.get(NotSitSharedPreferences.IsChanged));
        }
        super.onBackPressed();
    }
}
