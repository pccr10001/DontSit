package com.example.dontsit.app.SitTimeActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.dontsit.app.*;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SitTimeActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{

    private BarChart SitTimeChart;
    private SeekBar DaySeekBar;
    private TextView DayTextView;
    private Typeface typeface = Typeface.DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sittime);
        SitTimeChart = (BarChart) findViewById(R.id.sit_time_chart);
        DaySeekBar = (SeekBar) findViewById(R.id.day_seekBar);
        DayTextView = (TextView) findViewById(R.id.day_textView);

        SitTimeChart.setDrawBarShadow(false);
        SitTimeChart.setDrawValueAboveBar(true);

        SitTimeChart.setDescription("");
        SitTimeChart.setMaxVisibleValueCount(60);

        SitTimeChart.setPinchZoom(false);
        SitTimeChart.setDrawGridBackground(false);

        XAxis xAxis = SitTimeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(typeface);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);

        YAxis leftAxis = SitTimeChart.getAxisLeft();
        leftAxis.setTypeface(typeface);
        leftAxis.setLabelCount(8, false);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);

        Legend l = SitTimeChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);

        DayTextView.setText("7天內");
        DaySeekBar.setProgress(7);
        DaySeekBar.setMax(365);
        DaySeekBar.setOnSeekBarChangeListener(this);

        setData(7);
    }

    private void setData(int range) {

        DurationLogDAO logDAO = new DurationLogDAO(this);

        try {
            Calendar calendar1 = Calendar.getInstance(), calendar2 = Calendar.getInstance();
            calendar1.add(Calendar.DAY_OF_YEAR, -range);

            List<Duration> list = logDAO.getBetween(calendar1.getTime(), calendar2.getTime());

            if (list.size() == 0) return;

            ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

            DebugTools.Log(list);

            String LastDurationDayHour = "";
            boolean IsSameHour = true;
            int time = 0, count = 0;
            for (Duration duration : list) {
                if (IsSameHour)
                    time += duration.getTime();
                else {
                    //DebugTools.Log(LastDurationDayHour + " " + (time / 1000) + " " + count);
                    yVals1.add(new BarEntry(time / 1000, count++));
                    time = duration.getTime();
                }
                IsSameHour = DateFormatter.format(duration.getStartTime()).equals(LastDurationDayHour);
                LastDurationDayHour = DateFormatter.format(duration.getStartTime());
            }
            if (time != 0) {
                //DebugTools.Log("---" + LastDurationDayHour + " " + (time / 1000) + " " + count);
                yVals1.add(new BarEntry(time / 1000, count));
            }

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i <= count; i++) {
                calendar1.setTime(list.get(0).getStartTime());
                calendar1.add(Calendar.HOUR, i);
                xVals.add(DateFormatter.format(calendar1.getTime()));
                //DebugTools.Log(DateFormatter.format(calendar1.getTime()));
            }

            BarDataSet set1 = new BarDataSet(yVals1, "坐下時間(秒)");
            set1.setBarSpacePercent(35f);

            ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(xVals, dataSets);
            data.setValueTextSize(10f);
            data.setValueTypeface(typeface);

            SitTimeChart.setData(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int value = seekBar.getProgress();
        if (value == 0) {
            value = 1;
            DaySeekBar.setProgress(value);
        }
        setData(value);
        DayTextView.setText(value + " 天內");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
