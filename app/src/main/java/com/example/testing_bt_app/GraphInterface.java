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

    protected void addEntry(String input_data) {
        if(data != null) {
            ILineDataSet set = data.getDataSetByIndex(0);
            if(set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            int intFromString = 0;
            try {
                intFromString = Integer.parseInt(input_data.trim());
            } catch (NumberFormatException nfe) {
                System.out.println(input_data + " is not a number");
            }
            data.addEntry(new Entry(set.getEntryCount(), intFromString), 0);
            data.notifyDataChanged();

            mChart.notifyDataSetChanged();
            mChart.setMaxVisibleValueCount(150);
            mChart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Dynamic Data");
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
