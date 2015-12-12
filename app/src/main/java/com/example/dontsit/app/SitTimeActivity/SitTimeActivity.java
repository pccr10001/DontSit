package com.example.dontsit.app.SitTimeActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.dontsit.app.Common.*;
import com.example.dontsit.app.Database.CushionDatabaseChangedReceiver;
import com.example.dontsit.app.Database.Duration;
import com.example.dontsit.app.Database.DurationLogDAO;
import com.example.dontsit.app.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class SitTimeActivity extends AppCompatActivity
        implements SeekBar.OnSeekBarChangeListener, YAxisValueFormatter {

    private LineChart SitTimeChart;
    private SeekBar DaySeekBar;
    private TextView DayTextView;
    private Typeface typeface = Typeface.DEFAULT;
    private DurationLogDAO logDAO;
    private float textSize = 14f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sittime);
        logDAO = new DurationLogDAO(this);
        if (logDAO.getCount() == 0)
            try {
                logDAO.generate();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        logDAO.close();

        SitTimeChart = (LineChart) findViewById(R.id.sit_time_chart);
        DaySeekBar = (SeekBar) findViewById(R.id.day_seekBar);
        DayTextView = (TextView) findViewById(R.id.day_textView);

        SitTimeChart.setDescription("");
        SitTimeChart.setMaxVisibleValueCount(60);

        SitTimeChart.setPinchZoom(false);
        SitTimeChart.setDrawGridBackground(false);

        YAxisValueFormatter custom = new DefaultAxisValueFormatter();

        XAxis xAxis = SitTimeChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(typeface);
        xAxis.setTextSize(textSize);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);

        YAxis rightAxis = SitTimeChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setTypeface(typeface);
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(custom);
        rightAxis.setTextSize(textSize);
        rightAxis.setAxisMaxValue(3600f);
        rightAxis.setSpaceTop(15f);

        YAxis leftAxis = SitTimeChart.getAxisLeft();
        leftAxis.setTypeface(typeface);
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(custom);
        leftAxis.setTextSize(textSize);
        leftAxis.setAxisMaxValue(3600f);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);

        Legend l = SitTimeChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(textSize);
        l.setXEntrySpace(4f);

        DayTextView.setText("7天內");
        DaySeekBar.setProgress(7);
        DaySeekBar.setMax(365);
        DaySeekBar.setOnSeekBarChangeListener(this);

        CustomMarkerView mv = new CustomMarkerView(getApplicationContext(), R.layout.marketview);
        SitTimeChart.setMarkerView(mv);

        setData(7);
    }

    private DecimalFormat mFormat = new DecimalFormat("###,###,###,##0");

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

        @Override
        public String toString() {
            return date + " - " + time;
        }
    }

    private ArrayList<String> xVals;

    private void setData(int range) {

        logDAO = new DurationLogDAO(this);

        try {
            Calendar calendar1 = Calendar.getInstance(), calendar2 = Calendar.getInstance();
            calendar1.set(Calendar.HOUR_OF_DAY, 24);
            calendar1.set(Calendar.MINUTE, 0);
            calendar1.set(Calendar.SECOND, 0);
            calendar1.add(Calendar.DAY_OF_YEAR, -range);

            List<Duration> list = logDAO.getBetween(calendar1.getTime(), calendar2.getTime());

            logDAO.close();

            if (list.size() == 0) return;

            List<ChartData> datas = new ArrayList<ChartData>();
            ArrayList<Entry> yVals1 = new ArrayList<Entry>();

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
//            DebugTools.Log(datas.toString());

            xVals = new ArrayList<String>();
            int count = 0;
            for (int i = 0; i < range * 24; i++) {
                if (count < datas.size())
                    temp = datas.get(count);
                else
                    temp = new ChartData("", 0);
                String now = DateFormatter.short_format(calendar1.getTime());
//                DebugTools.Log(DateFormatter.short_format(calendar1.getTime()));
                if (now.equals(temp.date)) {
                    count++;
                    yVals1.add(new Entry(temp.time / 1000, i));
                } else {
                    yVals1.add(new Entry(0, i));
                }
                xVals.add(now);
                calendar1.add(Calendar.HOUR_OF_DAY, 1);
            }

            LineDataSet set1 = new LineDataSet(yVals1, "坐下時間(秒)");
            set1.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set1.setDrawFilled(true);
            set1.setDrawCircles(false);

            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
            dataSets.add(set1);

            LineData data = new LineData(xVals, dataSets);
            data.setValueTextSize(10f);
            data.setValueTypeface(typeface);
            data.setValueFormatter(new DefaultAxisValueFormatter());

            SitTimeChart.setData(data);
            SitTimeChart.invalidate();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public class CustomMarkerView extends MarkerView {

        protected TextView tvContent;

        public CustomMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            // this markerview only displays a textview
            tvContent = (TextView) findViewById(R.id.tvContent);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            if (xVals != null)
                tvContent.setText(xVals.get(e.getXIndex()) + " " + ((int) e.getVal()));
            // set the entry-value as the display text
        }

        @Override
        public int getXOffset(float xpos) {
            // this will center the marker-view horizontally
            return -(getWidth() / 2);
        }

        @Override
        public int getYOffset(float ypos) {
            // this will cause the marker-view to be above the selected value
            return -getHeight();
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
