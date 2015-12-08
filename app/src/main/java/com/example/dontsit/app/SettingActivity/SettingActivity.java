package com.example.dontsit.app.SettingActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import com.example.dontsit.app.Common.NotSitSharedPreferences;
import com.example.dontsit.app.R;
import com.rey.material.widget.EditText;
import com.rey.material.widget.Spinner;
import com.rey.material.widget.Switch;

public class SettingActivity extends AppCompatActivity {

    private Spinner ScanModeSpinner;
    private EditText ClockSoundPathEditText;
    private EditText CushionMacEditText;
    private Switch DefaultSettingSwitch;
    private NotSitSharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ScanModeSpinner = (Spinner) findViewById(R.id.spinner_label);
        String[] items = new String[]{"低頻率", "平衡", "低閒置"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.row_spn, items);
        ScanModeSpinner.setAdapter(adapter);
        preferences = new NotSitSharedPreferences(this);

        ClockSoundPathEditText = (EditText) findViewById(R.id.ClockSoundPathEditText);
        CushionMacEditText = (EditText) findViewById(R.id.CushionMacEditText);
        DefaultSettingSwitch = (Switch) findViewById(R.id.DefaultSettingSwitch);

        DefaultSettingSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                preferences.set(NotSitSharedPreferences.ClockSoundPath, );
            }
        });
    }
}
