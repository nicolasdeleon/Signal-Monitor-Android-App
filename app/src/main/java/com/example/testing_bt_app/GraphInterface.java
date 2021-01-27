package com.example.testing_bt_app;

import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;

public class GraphInterface {

    LineChart mChart;
    LineData data;

    public GraphInterface(LineChart _chart, String name, float scale) {
        mChart = _chart;
        data = new LineData();
        init(name, scale);
        initLegend();
        initXAxis();
        initYAxis(scale);

    }

    private void init(String name, float scale) {
        // --------- CHART INIT -----------
        mChart.getDescription().setEnabled(true);
        mChart.getDescription().setText(name);
        mChart.setTouchEnabled(false);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setDrawGridBackground(true);
        mChart.setPinchZoom(false);
        mChart.setBackgroundColor(Color.WHITE);
        mChart.setVisibleYRangeMaximum(scale*2, YAxis.AxisDependency.LEFT);
        mChart.setDrawBorders(true);

        // ---------- DATA INIT ------------
        data.setValueTextColor(Color.RED);
        mChart.setData(data);
    }

    private void initLegend() {
        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);
    }

    private void initXAxis() {
        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setDrawGridLines(false);
    }

    private void initYAxis(float scale) {
        // -------- LEFT Y AXIS ----------------
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMaximum(scale);
        leftAxis.setAxisMinimum(-scale);
        leftAxis.setDrawGridLines(true);

        // ------- RIGHT Y AXIS ----------------
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setTextColor(Color.WHITE);
    }
}
