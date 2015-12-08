package com.example.dontsit.app.AchievementActivity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.example.dontsit.app.Common.DebugTools;
import com.example.dontsit.app.Database.AchievementDAO;
import com.example.dontsit.app.R;

import java.util.ArrayList;
import java.util.List;

public class AchievementActivity extends AppCompatActivity {

    private TextView AchevementTextView;
    private ProgressBar AchevementProgressBar;
    private ListView AchievementLeftListView;

    private AchievementDAO achievementDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);
        AchevementTextView = (TextView) findViewById(R.id.AchievementTextView);
        AchevementProgressBar = (ProgressBar) findViewById(R.id.AchievementProgressBar);
        AchievementLeftListView = (ListView) findViewById(R.id.AchievementListView1);

        achievementDAO = new AchievementDAO(this);
        if (achievementDAO.getCount() == 0)
            achievementDAO.getDefault();

        List<Achievement> all = achievementDAO.getAll();
        AchievementLeftListView.setAdapter(new AchievementListViewAdapter(this, all));
    }
}
