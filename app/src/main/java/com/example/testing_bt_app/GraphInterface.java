package com.example.testing_bt_app;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

public class GraphInterface {

    LineChart mChart;
    LineData data;
    String name = "Default Graph";

    public GraphInterface(LineChart _chart, String name, float scale) {
        this.name = name;
        mChart = _chart;
        data = new LineData();
        init(scale);
        initLegend();
        initXAxis();
        initYAxis(scale);
    }

    private void init(float scale) {
        // --------- CHART INIT -----------
        mChart.getDescription().setEnabled(false);
        mChart.setTouchEnabled(false);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(true);
        mChart.setPinchZoom(false);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setVisibleYRangeMaximum(scale, YAxis.AxisDependency.LEFT);
        mChart.setDrawBorders(true);

        if(scale == -1)
            mChart.setAutoScaleMinMaxEnabled(true);


        // ---------- DATA INIT ------------
        data.setValueTextColor(Color.RED);
        mChart.setData(data);
    }

    private void initLegend() {
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
    }

    private void initXAxis() {
        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
    }

    private void initYAxis(float scale) {
        // -------- LEFT Y AXIS ----------------
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(true);
        leftAxis.setEnabled(false);

        if(scale != -1)
            leftAxis.setAxisMaximum(scale);

        // ------- RIGHT Y AXIS ----------------
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setTextColor(Color.WHITE);
    }

    protected void addEntry(Integer input_data, int visibility) {
        if(data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if(set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), input_data), 0);
            data.notifyDataChanged();

            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(visibility);
            mChart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, name);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setLineWidth(3f);
        set.setColor(Color.RED);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity((0.2f));
        return set;
    }
}
