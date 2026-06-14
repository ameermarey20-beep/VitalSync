package com.vitalsync.vitalsync;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;

public class StatsFragment extends Fragment {

    private LineChart statsTrendChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        statsTrendChart = view.findViewById(R.id.statsTrendChart);
        
        setupPremiumChart();
        return view;
    }

    private void setupPremiumChart() {
        // UI Setup
        statsTrendChart.getDescription().setEnabled(false);
        statsTrendChart.setTouchEnabled(true);
        statsTrendChart.setDrawGridBackground(false);
        statsTrendChart.setPadding(0, 0, 0, 0);

        // X-Axis (Timeline)
        XAxis xAxis = statsTrendChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.parseColor("#9FB3C8"));
        xAxis.setTextSize(10f);
        xAxis.setYOffset(10f);

        // Y-Axis (BPM)
        YAxis leftAxis = statsTrendChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#F0F4F8"));
        leftAxis.setTextColor(Color.parseColor("#9FB3C8"));
        leftAxis.setTextSize(10f);
        leftAxis.setXOffset(10f);
        leftAxis.setAxisMinimum(40f);
        leftAxis.setAxisMaximum(130f);

        statsTrendChart.getAxisRight().setEnabled(false);
        statsTrendChart.getLegend().setEnabled(false);

        // Dummy Data for Premium Visuals
        ArrayList<Entry> values = new ArrayList<>();
        values.add(new Entry(0, 72));
        values.add(new Entry(4, 75));
        values.add(new Entry(8, 88));
        values.add(new Entry(12, 82));
        values.add(new Entry(16, 95));
        values.add(new Entry(20, 78));
        values.add(new Entry(24, 74));

        LineDataSet set1 = new LineDataSet(values, "Heart Rate Trend");
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set1.setCubicIntensity(0.2f);
        set1.setDrawFilled(true);
        set1.setDrawCircles(true);
        set1.setLineWidth(3f);
        set1.setCircleRadius(5f);
        set1.setCircleColor(Color.parseColor("#102A43"));
        set1.setColor(Color.parseColor("#102A43"));
        set1.setFillColor(Color.parseColor("#102A43"));
        set1.setFillAlpha(20);
        set1.setDrawHorizontalHighlightIndicator(false);

        LineData data = new LineData(set1);
        data.setDrawValues(false);
        
        statsTrendChart.setData(data);
        statsTrendChart.animateY(1500);
        statsTrendChart.invalidate();
    }
}
