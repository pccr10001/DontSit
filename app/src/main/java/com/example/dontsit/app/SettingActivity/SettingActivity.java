package com.example.dontsit.app.SettingActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.*;
import com.example.dontsit.app.Common.DebugTools;
import com.example.dontsit.app.Common.NotSitSharedPreferences;
import com.example.dontsit.app.Database.DurationLogDAO;
import com.example.dontsit.app.R;
import com.rey.material.widget.Switch;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class SettingActivity extends AppCompatActivity {

    private Spinner ScanModeSpinner;
    private TextView ClockSoundPathEditText;
    private EditText CushionMacEditText;
    private Switch DefaultSettingSwitch;
    private Switch ResetDataSwitch;
    private NotSitSharedPreferences preferences;
    private boolean IsChanged = false;

    private int ScanMode;
    private String ClockPath;
    private String CushionMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        preferences = new NotSitSharedPreferences(this);

        ScanMode = Integer.valueOf(preferences.get(NotSitSharedPreferences.ScanMode));
        ClockPath = preferences.get(NotSitSharedPreferences.ClockSoundPath);
        CushionMac = preferences.get(NotSitSharedPreferences.MAC);

        ScanModeSpinner = (Spinner) findViewById(R.id.spinner_label);
        String[] items = new String[]{
                getString(R.string.ScanModeLowFrequency),
                getString(R.string.ScanModeBalanced),
                getString(R.string.ScanModeLowLatency)};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_spn, items);
        adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
        ScanModeSpinner.setAdapter(adapter);
        ScanModeSpinner.setSelection(ScanMode);
        ScanModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ClockSoundPathEditText = (TextView) findViewById(R.id.ClockSoundPathEditText);
        ClockSoundPathEditText.setText(ClockPath);
        ClockSoundPathEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFileChooser();
            }
        });

        CushionMacEditText = (EditText) findViewById(R.id.CushionMacEditText);
        CushionMacEditText.setText(CushionMac.equals("") ? CushionMac : "");

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
                        ResetDataSwitch.setChecked(true);
                    }

                    @Override
                    public void NoThenDo() {
                        ResetDataSwitch.setChecked(false);
                    }
                });
            }

        });
    }

    private static final int REQ_CODE_PICK_SOUNDFILE = 0;

    MagicFileChooser chooser;

    private void showFileChooser() {
        chooser = new MagicFileChooser(this);
        chooser.showFileChooser("audio/*", "請選擇音效檔案", false);

//        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//        intent.setType("audio/*");
//        startActivityForResult(Intent.createChooser(intent,
//                "請選擇音效檔案"), REQ_CODE_PICK_SOUNDFILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        chooser.onActivityResult(requestCode, resultCode, data);
        if (chooser.getChosenFiles().length == 1) {
            preferences.set(NotSitSharedPreferences.ClockSoundPath,
                    chooser.getChosenFiles()[0].toString());
        }
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

}
