package com.example.dontsit.app.SitTimeActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.dontsit.app.*;
import com.example.dontsit.app.Common.*;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SitTimeActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener, YAxisValueFormatter {

    private BarChart SitTimeChart;
    private SeekBar DaySeekBar;
    private TextView DayTextView;
    private Typeface typeface = Typeface.DEFAULT;

    private CushionDatabaseChangedReceiver mReceiver = new CushionDatabaseChangedReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (DaySeekBar != null)
                onProgressChanged(DaySeekBar, DaySeekBar.getProgress(), false);
        }
    };

    private IntentFilter filter = new IntentFilter(CushionDatabaseChangedReceiver.ACTION_DATABASE_CHANGED);

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

        YAxisValueFormatter custom = new DefaultAxisValueFormatter();

        XAxis xAxis = SitTimeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(typeface);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);

        YAxis rightAxis = SitTimeChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setTypeface(typeface);
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);

        YAxis leftAxis = SitTimeChart.getAxisLeft();
        leftAxis.setTypeface(typeface);
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(custom);
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

    @Override
    protected void onStart() {
        registerReceiver(mReceiver, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(mReceiver);
        super.onStop();
    }

    private DecimalFormat mFormat= new DecimalFormat("###,###,###,##0.0");

    public String getFormattedValue(float value, YAxis yAxis) {
        return mFormat.format(value);
    }

    private class ChartData {
        String date;
        int time = 0;

        public ChartData(String date, int time) {
            this.date = date;
            this.time = time;
        }
    }

    private void setData(int range) {

        DurationLogDAO logDAO = new DurationLogDAO(this);

        try {
            Calendar calendar1 = Calendar.getInstance(), calendar2 = Calendar.getInstance();
            calendar1.set(Calendar.HOUR_OF_DAY, 23);
            calendar1.set(Calendar.MINUTE, 59);
            calendar1.set(Calendar.SECOND, 59);
            calendar1.add(Calendar.DAY_OF_YEAR, -range);

            List<Duration> list = logDAO.getBetween(calendar1.getTime(), calendar2.getTime());

            if (list.size() == 0) return;

            List<ChartData> datas = new ArrayList<ChartData>();
            ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

            Duration first = list.get(0);
            String LastDurationDayHour = DateFormatter.short_format(first.getStartTime());
            datas.add(new ChartData(LastDurationDayHour, first.getTime()));

            boolean IsSameHour;
            ChartData temp = datas.get(0);
            for (int i = 1; i < list.size(); i++) {
                Duration duration = list.get(i);
                IsSameHour = DateFormatter.short_format(duration.getStartTime()).equals(LastDurationDayHour);
                LastDurationDayHour = DateFormatter.short_format(duration.getStartTime());
                if (IsSameHour)
                    temp.time += duration.getTime();
                else {
//                    DebugTools.Log(LastDurationDayHour + " " + (temp.time / 1000));
                    datas.add(new ChartData(LastDurationDayHour, duration.getTime()));
                    temp = datas.get(datas.size() - 1);
                }
            }

            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < datas.size(); i++) {
                temp = datas.get(i);
                yVals1.add(new BarEntry(temp.time / 1000, i));
                xVals.add(temp.date);
                DebugTools.Log(DateFormatter.short_format(calendar1.getTime()));
            }

            BarDataSet set1 = new BarDataSet(yVals1, "坐下時間(秒)");
            set1.setBarSpacePercent(35f);

            ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(xVals, dataSets);
            data.setValueTextSize(10f);
            data.setValueTypeface(typeface);
            data.setValueFormatter(new DefaultAxisValueFormatter());

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
