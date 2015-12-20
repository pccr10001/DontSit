package com.example.dontsit.app.CushionStateActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import com.example.dontsit.app.AnalysisActivity.AvgTimeListViewAdapter;
import com.example.dontsit.app.AnalysisActivity.ShowData;
import com.example.dontsit.app.Common.DebugTools;
import com.example.dontsit.app.Common.NotSitSharedPreferences;
import com.example.dontsit.app.Database.CushionDatabaseChangedReceiver;
import com.example.dontsit.app.R;

import java.util.ArrayList;
import java.util.List;

public class CushionStateActivity extends AppCompatActivity {

    private ListView CushionStateListView;
    private CushionStateListViewAdapter listViewAdapter;
    private List<ShowData> showDatas;
    private NotSitSharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);
        initToolbar();

        CushionStateListView = (ListView) findViewById(R.id.CushionStateListView);
        preferences = new NotSitSharedPreferences(this);
        initData();
        listViewAdapter = new CushionStateListViewAdapter(this, showDatas);
        CushionStateListView.setAdapter(listViewAdapter);
        DebugTools.Log(showDatas);

        registerReceiver(mCushionDatabaseReceiver, mCushionFilter);
    }

    private void initToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.state_toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public void onStop() {
        unregisterReceiver(mCushionDatabaseReceiver);
        super.onStop();
    }

    private void initData() {
        showDatas = new ArrayList<ShowData>();
        showDatas.add(new ShowData("坐墊MAC",
                preferences.get(NotSitSharedPreferences.MAC), Color.WHITE));
        showDatas.add(new ShowData("上次持續時間",
                preferences.get(NotSitSharedPreferences.LastTimeDuration) + " 微秒", Color.WHITE));
        showDatas.add(new ShowData("上次改變時間",
                preferences.get(NotSitSharedPreferences.LastNotifyTime), Color.WHITE));
        showDatas.add(new ShowData("上次連線時間",
                preferences.get(NotSitSharedPreferences.LastConnectTime), Color.WHITE));
        showDatas.add(new ShowData("是否有人坐著",
                preferences.get(NotSitSharedPreferences.IsSeated).equals("1") ? "是" : "否", Color.WHITE));
    }

    private CushionDatabaseChangedReceiver mCushionDatabaseReceiver = new CushionDatabaseChangedReceiver() {
        public void onReceive(Context context, Intent intent) {
            showDatas.get(0).setValue(
                    preferences.get(NotSitSharedPreferences.MAC));
            showDatas.get(1).setValue(
                    preferences.get(NotSitSharedPreferences.LastTimeDuration) + " 微秒");
            showDatas.get(2).setValue(
                    preferences.get(NotSitSharedPreferences.LastNotifyTime));
            showDatas.get(3).setValue(
                    preferences.get(NotSitSharedPreferences.LastConnectTime));
            showDatas.get(4).setValue(
                    preferences.get(NotSitSharedPreferences.IsSeated).equals("1") ? "是" : "否");
            listViewAdapter.notifyDataSetChanged();
        }
    };

    private IntentFilter mCushionFilter
            = new IntentFilter(CushionDatabaseChangedReceiver.ACTION_DATABASE_CHANGED);

    public void BackParent(View view) {
        onBackPressed();
    }
}
