package com.example.dontsit.app.SitTimeActivity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
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
import java.util.TimeZone;

public class SitTimeActivity extends AppCompatActivity implements YAxisValueFormatter {

    private LineChart SitTimeChart;
    private Typeface typeface = Typeface.DEFAULT;
    private DurationLogDAO logDAO;
    private final static float textSize = 14f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sittime);
        initToolbar();

        logDAO = new DurationLogDAO(this);
        if (logDAO.getCount() == 0)
            try {
                logDAO.generate();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        logDAO.close();

        SitTimeChart = (LineChart) findViewById(R.id.sit_time_chart);

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

        CustomMarkerView mv = new CustomMarkerView(getApplicationContext(), R.layout.marketview);
        SitTimeChart.setMarkerView(mv);

        setData(7);
    }

    private void initToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.sitTime_toolbar);
        setSupportActionBar(myToolbar);
    }

    private DecimalFormat mFormat = new DecimalFormat("###,###,###,##0");

    public String getFormattedValue(float value, YAxis yAxis) {
        return mFormat.format(value);
    }

    private ArrayList<String> xVals;

    private void setData(int range) {

        logDAO = new DurationLogDAO(this);

        try {
//            for (Duration duration : logDAO.getAll())
//                DebugTools.Log(duration);

            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTimeZone(TimeZone.getDefault());
            calendar1.set(Calendar.HOUR_OF_DAY, 23);
            calendar1.set(Calendar.MINUTE, 59);
            calendar1.set(Calendar.SECOND, 59);
            calendar1.add(Calendar.DAY_OF_YEAR, -range + 1);

            ArrayList<Entry> yVals1 = new ArrayList<Entry>();

            xVals = new ArrayList<String>();
            for (int i = 0; i < 6 * 24 + calendar1.get(Calendar.HOUR_OF_DAY); i++) {
                yVals1.add(new Entry(logDAO.getHourTimeAt(calendar1.getTime()), i));
//                DebugTools.Log(calendar1.getTime());
                xVals.add(DateFormatter.short_format(calendar1.getTime()));
                calendar1.add(Calendar.HOUR_OF_DAY, 1);
            }

            logDAO.close();

            LineDataSet set1 = new LineDataSet(yVals1, "近7日每小時坐著時間(秒)");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void BackParent(View view) {
        onBackPressed();
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
}
