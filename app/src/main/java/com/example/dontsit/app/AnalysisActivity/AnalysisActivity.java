package com.example.dontsit.app.AnalysisActivity;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.example.dontsit.app.Common.DateFormatter;
import com.example.dontsit.app.Common.DebugTools;
import com.example.dontsit.app.Database.DayDuration;
import com.example.dontsit.app.Database.DayDurationLogDAO;
import com.example.dontsit.app.Database.DurationLogDAO;
import com.example.dontsit.app.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.highlight.Highlight;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AnalysisActivity extends AppCompatActivity {

    private BarChart AllDaySitTimeBarChart;
    private LinearLayout AllDaySitTimeListView;
    private LineChart EverydayLineChart;
    //    private LinearLayout EverydayListView;
//    private BarChart EveryWeekStackChart;
//    private LinearLayout EveryWeekListView;
    private CombinedChart StandUpTimeCombinedChart;
    private LinearLayout StandUpTimeListView;
    private Typeface typeface = Typeface.DEFAULT;

    private DurationLogDAO logDAO;
    private DayDurationLogDAO logDAO1;
    private List<DayDuration> durations = new ArrayList<DayDuration>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);
        initToolbar();

        AllDaySitTimeBarChart = (BarChart) findViewById(R.id.AnalysisPieChart);
        AllDaySitTimeListView = (LinearLayout) findViewById(R.id.AnalysisPieChartListView);

        EverydayLineChart = (LineChart) findViewById(R.id.AnalysisHorizontalBarChart);

//        EveryWeekStackChart = (BarChart) findViewById(R.id.AnalysisBarChart);
//        EveryWeekListView = (LinearLayout) findViewById(R.id.AnalysisBarChartListView);

        StandUpTimeCombinedChart = (CombinedChart) findViewById(R.id.AnalysisCombinedChart);
        StandUpTimeListView = (LinearLayout) findViewById(R.id.AnalysisCombinedChartListView);

        try {
            DayDurationLogDAO dao = new DayDurationLogDAO(this);
            if (dao.getCount() == 0)
                dao.generate();
            dao.close();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        initData();

        initAllDayBarChart();
        initAllDayBarChartListView();

        initEverydaySitTimeData();
        initEverydayLineChart();

        initStandUpTimeData();
        initStandUpTimeCombinedChart();
        initStandUpTimeListView();

    }

    private void initToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.analysis_toolbar);
        setSupportActionBar(myToolbar);
    }

    private void initData() {
        logDAO = new DurationLogDAO(this);
        logDAO1 = new DayDurationLogDAO(this);
        Calendar Today = Calendar.getInstance();
        Today.setTimeZone(TimeZone.getDefault());

        COUNT_RANGE[1] = Today.get(Calendar.DAY_OF_WEEK);
        COUNT_RANGE[2] = Today.get(Calendar.DAY_OF_MONTH);
        COUNT_RANGE[3] = Today.get(Calendar.DAY_OF_YEAR);

        try {
            durations = logDAO1.getAll();
            Collections.reverse(durations);
            Calendar Someday = Calendar.getInstance();
            Someday.setTimeZone(TimeZone.getDefault());
            for (int i = 0; i < 7; i++) {
                DayDuration duration = new DayDuration();
                duration.setDate(Someday.getTime());
                duration.setSitTime(logDAO.getDayTimeAt(Someday.getTime()));
                duration.setChangeTime(logDAO.getDayTimesAt(Someday.getTime()));
                durations.add(i, duration);
                Someday.add(Calendar.DAY_OF_WEEK, -1);
            }
//            DebugTools.Log(durations);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private List<ShowData> AvgTimeBarChartData = new ArrayList<ShowData>();

    private static String[] DateChartTitle = {"今天", "本週", "本月", "今年"};

    private static int[] AvgTimeBarChartColors = {
            Color.parseColor("#EE7700"),
            Color.parseColor("#00AA00"),
            Color.parseColor("#0044BB"),
            Color.parseColor("#7A0099")};

    private int[] COUNT_RANGE = new int[]{1, 0, 0, 0};

    private void initAvgTimeData() {
        float count[] = new float[]{0f, 0f, 0f, 0f};
        Calendar Someday = Calendar.getInstance();

        Someday.set(Calendar.HOUR_OF_DAY, 23);
        Someday.set(Calendar.MINUTE, 59);
        Someday.set(Calendar.SECOND, 59);

        try {
            count[0] = logDAO.getDayTimeAt(Someday.getTime());
            count[1] += count[0];
            for (int i = 0; i < COUNT_RANGE[1] - 1; i++) {
                Someday.add(Calendar.DAY_OF_WEEK, -1);
                count[1] += logDAO.getDayTimeAt(Someday.getTime());
//                DebugTools.Log(logDAO.getDayTimeAt(Someday.getTime()));
            }
            count[2] += count[1];
            for (int i = 0; i < COUNT_RANGE[2] - COUNT_RANGE[1]; i++) {
                Someday.add(Calendar.DAY_OF_MONTH, -1);
                DayDuration duration = logDAO1.get(Someday.getTime());
                count[2] += duration != null ? duration.getSitTime() : 0;
//                DebugTools.Log(duration == null ? "": duration);
            }
            count[3] += count[2];
            for (int i = 0; i < COUNT_RANGE[3] - COUNT_RANGE[2]; i++) {
                Someday.add(Calendar.DAY_OF_YEAR, -1);
                DayDuration duration = logDAO1.get(Someday.getTime());
                count[3] += duration != null ? duration.getSitTime() : 0;
//                DebugTools.Log(duration == null ? "": duration);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        DebugTools.Log(COUNT_RANGE[0]);
        DebugTools.Log(COUNT_RANGE[1]);
        DebugTools.Log(COUNT_RANGE[2]);
        DebugTools.Log(COUNT_RANGE[3]);

        for (int i = 0; i < COUNT_RANGE.length; i++)
            AvgTimeBarChartData.add(new ShowData(DateChartTitle[i],
                    String.format("%.1f", count[i] / COUNT_RANGE[i]), AvgTimeBarChartColors[i]));
        logDAO.close();
        logDAO1.close();
    }

    private void initAllDayBarChart() {
        AllDaySitTimeBarChart.setDescription("");
        AllDaySitTimeBarChart.setMaxVisibleValueCount(60);
        AllDaySitTimeBarChart.setPinchZoom(false);

        AllDaySitTimeBarChart.setDrawBarShadow(false);
        AllDaySitTimeBarChart.setDrawGridBackground(false);

        XAxis xAxis = AllDaySitTimeBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(0);
        xAxis.setDrawGridLines(false);

        AllDaySitTimeBarChart.getAxisLeft().setDrawGridLines(false);

        YAxis leftAxis = AllDaySitTimeBarChart.getAxisLeft();
        leftAxis.setTypeface(typeface);
        leftAxis.setTextSize(11f);
        leftAxis.setLabelCount(5, false);

        YAxis rightAxis = AllDaySitTimeBarChart.getAxisRight();
        rightAxis.setTypeface(typeface);
        rightAxis.setTextSize(11f);
        rightAxis.setLabelCount(5, false);

        // add a nice and smooth animation
        AllDaySitTimeBarChart.animateY(2500);

        AllDaySitTimeBarChart.getLegend().setEnabled(false);

        initAvgTimeData();
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < AvgTimeBarChartData.size(); i++) {
            ShowData temp = AvgTimeBarChartData.get(i);
//            DebugTools.Log(temp);
            yVals1.add(new BarEntry(Float.parseFloat(temp.getValue()), i));
            xVals.add(temp.getTitle());
        }

        BarDataSet set1 = new BarDataSet(yVals1, "Data Set");
        set1.setColors(AvgTimeBarChartColors);
        set1.setDrawValues(false);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(set1);

        BarData data = new BarData(xVals, dataSets);

        AllDaySitTimeBarChart.setData(data);
        AllDaySitTimeBarChart.invalidate();
    }

    private void initAllDayBarChartListView() {
        ListAdapter adapter = new AvgTimeListViewAdapter(this, AvgTimeBarChartData);
        final int adapterCount = adapter.getCount();

        for (int i = 0; i < adapterCount; i++) {
            View item = adapter.getView(i, null, null);
            AllDaySitTimeListView.addView(item);
        }
    }

    private List[] Everyday = new List[]{
            new ArrayList<ShowData>(),
            new ArrayList<ShowData>(),
            new ArrayList<ShowData>()};

    private static String[] EverydayChartTitle
            = {"週日", "週一", "週二", "週三", "週四", "週五", "週六"};

    private static int[] EverydayLineChartColors = {
            Color.parseColor("#CC0000"),
            Color.parseColor("#BBBB00"),
            Color.parseColor("#00DDDD")};

    private void initEverydaySitTimeData() {
        long[][] day_data = new long[3][7];
        float[][] count = new float[3][7];

        Calendar Someday = Calendar.getInstance();
        Someday.setTimeZone(TimeZone.getDefault());

        for (int i = 0; i < day_data.length; i++) {
            for (int j = 0; j < day_data[i].length; j++) {
                day_data[i][j] = 0;
                count[i][j] = 0;
            }
        }

        int[] temp = new int[]{COUNT_RANGE[2] - 1, 7 - COUNT_RANGE[2] % 7, 7 - COUNT_RANGE[3] % 7};
        for (int i = 0; i < COUNT_RANGE[1]; i++)
            count[0][(temp[0] + i) % 7]++;
        for (int i = 0; i < COUNT_RANGE[2]; i++)
            count[1][(temp[1] + i) % 7]++;
        for (int i = 0; i < COUNT_RANGE[3]; i++)
            count[2][(temp[2] + i) % 7]++;
//      DebugTools.Log(Someday.getTime());

        int position = 0;
        for (int i = 0; i < COUNT_RANGE[3]; i++) {
            if (i + 1 > durations.size())
                break;
            int index = Someday.get(Calendar.DAY_OF_WEEK) - 1;
            DayDuration duration = durations.get(position);
            DebugTools.Log(durations.get(i));
            try {
                if (DateFormatter.middle_format(duration.getDate())
                        .equals(DateFormatter.middle_format(Someday.getTime()))) {
                    if (i < COUNT_RANGE[1])
                        day_data[0][index] += duration.getSitTime();
                    if (i < COUNT_RANGE[2])
                        day_data[1][index] += duration.getSitTime();
                    if (i < COUNT_RANGE[3])
                        day_data[2][index] += duration.getSitTime();
                    position++;
                }
                Someday.add(Calendar.DAY_OF_WEEK, -1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        for (int i = 0; i < count.length; i++) {
            for (int j = 0; j < count[i].length; j++) {
                if (count[i][j] == 0) count[i][j] = 1;
                Everyday[i].add(new ShowData(EverydayChartTitle[j],
                        String.format("%.1f", (day_data[i][j] / count[i][j])),
                        EverydayLineChartColors[i]));
            }
        }

    }

    private void initEverydayLineChart() {
        EverydayLineChart.setDescription("");
        EverydayLineChart.setDrawGridBackground(false);
        EverydayLineChart.setHighlightPerDragEnabled(true);
        // if disabled, scaling can be done on x- and y-axis separately
        EverydayLineChart.setPinchZoom(true);
        // set an alternative background color
        EverydayLineChart.setBackgroundColor(Color.WHITE);

        XAxis xAxis = EverydayLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(typeface);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);

        YAxis leftAxis = EverydayLineChart.getAxisLeft();
        leftAxis.setTypeface(typeface);
        leftAxis.setTextSize(11f);
        leftAxis.setLabelCount(1, false);

        YAxis rightAxis = EverydayLineChart.getAxisRight();
        rightAxis.setTypeface(typeface);
        rightAxis.setTextSize(11f);
        rightAxis.setLabelCount(1, false);
        rightAxis.setDrawGridLines(false);

        // do not forget to refresh the chart
        // holder.chart.invalidate();
        EverydayLineChart.animateX(750);

        ArrayList<String> xVals = new ArrayList<String>();

        xVals.addAll(Arrays.asList(EverydayChartTitle));

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();

        for (int i = 0; i < 3; i++) {

//            DebugTools.Log(Everyday[i]);
            ArrayList<Entry> values = new ArrayList<Entry>();

            for (int j = 0; j < Everyday[i].size(); j++) {
                values.add(new Entry(Float.parseFloat(((ShowData) Everyday[i].get(j)).getValue()), j));
            }

            LineDataSet d = new LineDataSet(values, DateChartTitle[i + 1]);
            d.setValueTextSize(0f);
            d.setLineWidth(2.5f);
            d.setCircleSize(2.5f);

            int color = EverydayLineChartColors[i];
            d.setColor(color);
            d.setCircleColor(color);
            d.setValueTextColor(color);
            dataSets.add(d);
        }

        CustomMarkerView mv = new CustomMarkerView(getApplicationContext(), R.layout.marketview);
        EverydayLineChart.setMarkerView(mv);

        LineData data = new LineData(xVals, dataSets);
        EverydayLineChart.setData(data);
        EverydayLineChart.invalidate();
    }

    private List<Integer> ChangeTimes = new ArrayList<Integer>();
    private List<Integer> SitTimes = new ArrayList<Integer>();
    private List<ShowData> Day30ShowData = new ArrayList<ShowData>();

    private static int[] StandUpTimeChartColors = {
            Color.parseColor("#EE7700"),
            Color.parseColor("#00DD00")};

    private void initStandUpTimeData() {
        int near_day = 30;
        for (int i = 0; i < near_day; i++) {
            if (i + 1 > durations.size()) {
                ChangeTimes.add(0);
                SitTimes.add(0);
            } else {
                DayDuration duration = durations.get(i);
                ChangeTimes.add(duration.getChangeTime());
                SitTimes.add(duration.getSitTime());
            }
        }
//        DebugTools.Log(SitTimes);
        Collections.reverse(ChangeTimes);
        Collections.reverse(SitTimes);

        int change_time_max = ChangeTimes.size() > 0 ? Collections.max(ChangeTimes) : 0;
        int sit_time_max = SitTimes.size() > 0 ? Collections.max(SitTimes) : 0;

        Day30ShowData.add(new ShowData("起身次數",
                String.format("%d 次", +change_time_max), StandUpTimeChartColors[0]));
        Day30ShowData.add(new ShowData("久坐時間",
                String.format("%d 秒", sit_time_max), StandUpTimeChartColors[1]));
    }

    private void initStandUpTimeCombinedChart() {
        StandUpTimeCombinedChart.setDescription("");
        StandUpTimeCombinedChart.setDrawGridBackground(false);
        StandUpTimeCombinedChart.setDrawBarShadow(false);

        // draw bars behind lines
        StandUpTimeCombinedChart.setDrawOrder(new CombinedChart.DrawOrder[]{
                CombinedChart.DrawOrder.BAR,
                CombinedChart.DrawOrder.LINE,
        });

        YAxis rightAxis = StandUpTimeCombinedChart.getAxisRight();
        rightAxis.setDrawGridLines(false);

        YAxis leftAxis = StandUpTimeCombinedChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);

        XAxis xAxis = StandUpTimeCombinedChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);

//        DebugTools.Log(ChangeTimes);
//        DebugTools.Log(ChangeTimes.size());
//        DebugTools.Log(SitTimes);
//        DebugTools.Log(SitTimes.size());

        LineData d = new LineData();

        ArrayList<Entry> entries = new ArrayList<Entry>();

        for (int index = 0; index < ChangeTimes.size(); index++)
            entries.add(new Entry(ChangeTimes.get(index), index));

        LineDataSet set = new LineDataSet(entries, "近30日起身次數");
        set.setColor(StandUpTimeChartColors[0]);
        set.setLineWidth(2.5f);
        set.setCircleColor(StandUpTimeChartColors[0]);
        set.setCircleSize(2.5f);
        set.setDrawCubic(true);
        set.setValueTextSize(0f);

        set.setAxisDependency(YAxis.AxisDependency.RIGHT);

        d.addDataSet(set);

        BarData b = new BarData();

        ArrayList<BarEntry> entries1 = new ArrayList<BarEntry>();

        for (int index = 0; index < SitTimes.size(); index++)
            entries1.add(new BarEntry(SitTimes.get(index), index));

        BarDataSet set1 = new BarDataSet(entries1, "近30日久坐時間");
        set1.setColor(StandUpTimeChartColors[1]);
        set1.setValueTextSize(0f);
        b.addDataSet(set1);

        set1.setAxisDependency(YAxis.AxisDependency.LEFT);

        Calendar now = Calendar.getInstance();
        String[] temp = new String[30];
        for (int i = 30; i > 0; i--) {
            temp[i - 1] = new SimpleDateFormat("MM/dd", Locale.getDefault()).format(now.getTime());
            now.add(Calendar.DAY_OF_YEAR, -1);
        }
        CombinedData data = new CombinedData(temp);

        data.setData(d);
        data.setData(b);

        CustomMarkerView mv = new CustomMarkerView(getApplicationContext(), R.layout.marketview);
        StandUpTimeCombinedChart.setMarkerView(mv);

//        StandUpTimeCombinedChart.setDragEnabled(false);

        StandUpTimeCombinedChart.setData(data);
        StandUpTimeCombinedChart.invalidate();
    }

    private void initStandUpTimeListView() {
        ListAdapter adapter = new StandUpListViewAdapter(this, Day30ShowData);
        final int adapterCount = adapter.getCount();

        for (int i = 0; i < adapterCount; i++) {
            View item = adapter.getView(i, null, null);
            StandUpTimeListView.addView(item);
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
            tvContent.setText(String.valueOf(e.getVal()));
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
