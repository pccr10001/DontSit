package com.example.dontsit.app.CheckActivity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.dontsit.app.Common.DateFormatter;
import com.example.dontsit.app.Common.DebugTools;
import com.example.dontsit.app.Database.DayDuration;
import com.example.dontsit.app.Database.DayDurationLogDAO;
import com.example.dontsit.app.Database.Duration;
import com.example.dontsit.app.Database.DurationLogDAO;
import com.example.dontsit.app.R;

import java.text.ParseException;
import java.util.*;

public class CheckActivity extends AppCompatActivity {

    private TextView CheckTextView;
    private ProgressBar CheckProgressBar;
    private ListView CheckListView;
    private CheckListViewAdapter checkListViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);
        initToolbar();

        CheckTextView = (TextView) findViewById(R.id.CheckTextView);
        CheckProgressBar = (ProgressBar) findViewById(R.id.CheckProgressBar);
        CheckListView = (ListView) findViewById(R.id.ChecklistView);

        initData();
        CheckItems();

        checkListViewAdapter = new CheckListViewAdapter(this, items);
        CheckListView.setAdapter(checkListViewAdapter);

        CheckProgressBar.setMax(items.size());
        CheckProgressBar.setProgress(check_time);
        CheckTextView.setText(check_time + " / " + items.size());
    }

    private void initToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.check_toolbar);
        setSupportActionBar(myToolbar);
    }

    private List<CheckItem> items = new ArrayList<CheckItem>();

    private DurationLogDAO logDAO;
    private DayDurationLogDAO logDAO1;
    private List<Duration> durations;
    private List<DayDuration> dayDurations;

    private void initData() {

        logDAO = new DurationLogDAO(this);
        logDAO1 = new DayDurationLogDAO(this);

        try {
            durations = logDAO.getAll();
            dayDurations = logDAO1.getByRange(23);
            Collections.reverse(dayDurations);
            Calendar Someday = Calendar.getInstance();
            Someday.setTimeZone(TimeZone.getDefault());
            for (int i = 0; i < 7; i++) {
                DayDuration duration = new DayDuration();
                duration.setDate(Someday.getTime());
                duration.setSitTime(logDAO.getDayTimeAt(Someday.getTime()));
                duration.setChangeTime(logDAO.getDayTimesAt(Someday.getTime()));
                dayDurations.add(i, duration);
                Someday.add(Calendar.DAY_OF_WEEK, -1);
            }
//            DebugTools.Log(durations);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        logDAO.close();
        logDAO1.close();
//        for (int i = 0; i < Day30Record.size(); i++) {
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime(Day30Record.get(i).getDate());
//            DebugTools.Log(calendar.getTime() + ", " + calendar.get(Calendar.DAY_OF_WEEK));
//        }
    }

    //    private String[] Days = {"日", "一", "二", "三", "四", "五", "六"};
    private int[][] checks = {{3, 5, 7}, {10, 20, 30}, {5, 15, 25}};
    private int check_time = 0;
    private static final int limit = 8;
    private static final int lower_limit = 2;
    private static final int day_bound = 30;

    private void CheckItems() {
        Calendar temp = Calendar.getInstance();
        int tip = 7, safeTime[] = {0, 0, 0, 0};


        //check max overtime a week
        for (int i = 0; i < tip; i++) {
            if (dayDurations.get(i).getSitTime() < limit * 3600)
                safeTime[0]++;
//            DebugTools.Log(dayDurations.get(i));
        }
//        DebugTools.Log(safeTime[0]);

        //check one time overtime a week
        int[] count = new int[]{0, 0, 0, 0, 0, 0, 0};
        for (Duration duration : durations) {
            temp.setTime(duration.getStartTime());
            if (duration.getTime() / 1000 > lower_limit * 3600) {
                count[temp.get(Calendar.DAY_OF_WEEK) - 1]++;
                safeTime[1]++;
            }
//            DebugTools.Log(duration);
        }
//        DebugTools.Log(safeTime[1]);

        //check max overtime a month
        for (DayDuration duration : dayDurations) {
            if (duration.getSitTime() < limit * 3600)
                safeTime[2]++;
//            DebugTools.Log(duration);
        }
//        DebugTools.Log(safeTime[2]);

        for (int i = 0; i < checks[0].length; i++) {
            CheckItem item = new CheckItem("過去 " + tip + " 天有 "
                    + checks[0][i] + " 天少於 " + limit + " 小時", 0);
            if (safeTime[0] >= checks[0][i])
                item.setChecked(true);
            items.add(item);
            item = new CheckItem("過去 " + tip + " 天每次超過 " +
                    lower_limit + " 小時少於 " + checks[1][i] + " 次", 1);
            if (safeTime[1] <= checks[1][i])
                item.setChecked(true);
            items.add(item);
            item = new CheckItem("過去 " + day_bound + " 天有 "
                    + checks[2][i] + " 天少於 " + limit + " 小時", 2);
            if (safeTime[2] >= checks[2][i])
                item.setChecked(true);
            items.add(item);
        }

        for (CheckItem item : items)
            if (item.isChecked())
                check_time++;

        Collections.sort(items);
//        DebugTools.Log(items);
    }

    public void BackParent(View view) {
        onBackPressed();
    }
}
