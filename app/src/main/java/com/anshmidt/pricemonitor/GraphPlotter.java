package com.anshmidt.pricemonitor;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class GraphPlotter {

    DataManager dataManager = new DataManager();
    Context context;
//    GraphView graph;
    final String DATES_ON_AXIS_FORMAT = "dd/MM";
    final int PADDING_TO_FIT_VERTICAL_AXIS_LABELS = 74;
    final int GRAPH_LINE_THICKNESS = 8;

    final int HORIZONTAL_AXIS_RANGE_IF_DATA_NOT_AVAILABLE_DAYS = 3;
    final int VERTICAL_AXIS_RANGE_IF_DATA_NOT_AVAILABLE = 100;
    final int MIN_Y_IF_DATA_NOT_AVAILABLE = 0;
    final int MAX_Y_IF_DATA_NOT_AVAILABLE = MIN_Y_IF_DATA_NOT_AVAILABLE + VERTICAL_AXIS_RANGE_IF_DATA_NOT_AVAILABLE;
    final int Y_DATA_RANGE_SO_SMALL_THAT_CUSTOM_SCALE_NEEDED = 4;
    final int VERTICAL_AXIS_RANGE_IF_DATA_RANGE_SMALL = 4;

    final int POINTS_SIZE = 12;



    public GraphPlotter(Context context) {
        this.context = context;
    }

    public void createGraph(TreeMap<Date, Integer> data, GraphView graph, int graphColor, boolean pointsShown) {
        clearGraph(graph);
        addSeriesToGraph(data, graph, graphColor, pointsShown);
    }

    public void clearGraph(GraphView graph) {
        graph.removeAllSeries(); //clear existing data from graph
    }

    public void addSeriesToGraph(TreeMap<Date, Integer> data, GraphView graph, int graphColor, boolean pointsShown) {
        boolean isDataAvailable = false;

        ArrayList<Date> sortedKeys = null;
        if (data != null) {
            sortedKeys = dataManager.getSortedKeys(data);
            if (sortedKeys.size() > 0) {
                isDataAvailable = true;
            }
        }

        if (isDataAvailable) {
            DataPoint[] dataPoints = new DataPoint[data.size()];

            int i = 0;
            for (Date date : sortedKeys) {
                Integer value = data.get(date);
                dataPoints[i] = new DataPoint(date, value);
                i++;
            }

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
            graph.addSeries(series);
            series.setColor(graphColor);
            series.setThickness(GRAPH_LINE_THICKNESS);

            if (pointsShown) {
                PointsGraphSeries<DataPoint> points = new PointsGraphSeries<>(dataPoints);
                graph.addSeries(points);
                points.setSize(POINTS_SIZE);
                points.setColor(graphColor);
            }
        }

        GridLabelRenderer gridLabelRenderer = graph.getGridLabelRenderer();
        gridLabelRenderer.setPadding(PADDING_TO_FIT_VERTICAL_AXIS_LABELS);
        gridLabelRenderer.setVerticalLabelsColor(context.getColor(R.color.colorText));
        gridLabelRenderer.setHorizontalLabelsColor(context.getColor(R.color.colorText));

//        gridLabelRenderer.setGridStyle(GridLabelRenderer.GridStyle.NONE);
        gridLabelRenderer.setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);

//        setXAxisRange(graph, sortedKeys, isDataAvailable);
        setYAxisRangeNew(graph, data, isDataAvailable);


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATES_ON_AXIS_FORMAT);
        gridLabelRenderer.setLabelFormatter(new DateAsXAxisLabelFormatter(context, simpleDateFormat));

        gridLabelRenderer.setHumanRounding(false, true);

        graph.getViewport().setScrollable(false); // enables horizontal scrolling
        graph.getViewport().setScrollableY(false); // enables vertical scrolling
        graph.getViewport().setScalable(false); // enables horizontal zooming and scrolling
        graph.getViewport().setScalableY(false); // enables vertical zooming and scrolling
    }

//    private void setXAxisRange(GraphView graph, ArrayList<Date> sortedKeys, boolean isDataAvailable) {
//        final int HORIZONTAL_AXIS_LABELS_COUNT = 4;
//        final long MAX_X_IF_DATA_NOT_AVAILABLE = new Date().getTime();
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DAY_OF_YEAR, -HORIZONTAL_AXIS_RANGE_IF_DATA_NOT_AVAILABLE_DAYS);
//        final long MIN_X_IF_DATA_NOT_AVAILABLE = calendar.getTime().getTime();
//
//        long minAxisX = MIN_X_IF_DATA_NOT_AVAILABLE;
//        long maxAxisX = MAX_X_IF_DATA_NOT_AVAILABLE;
//
//        long minDataX;
//        long maxDataX;
//        if (isDataAvailable) {
//            minDataX = sortedKeys.get(0).getTime();
//            maxDataX = sortedKeys.get(sortedKeys.size() - 1).getTime();
//
//            if (maxDataX - minDataX < TimeUnit.DAYS.toMillis(HORIZONTAL_AXIS_RANGE_IF_DATA_NOT_AVAILABLE_DAYS)) {
//                minAxisX = Math.min(minDataX, MIN_X_IF_DATA_NOT_AVAILABLE);
//                maxAxisX = minAxisX + TimeUnit.DAYS.toMillis(HORIZONTAL_AXIS_RANGE_IF_DATA_NOT_AVAILABLE_DAYS);
//            } else {
//                minAxisX = minDataX;
//                maxAxisX = maxDataX;
//            }
//        }
//        graph.getViewport().setMinX(minAxisX);
//        graph.getViewport().setMaxX(maxAxisX);
//        graph.getViewport().setXAxisBoundsManual(true);
//        graph.getGridLabelRenderer().setNumHorizontalLabels(HORIZONTAL_AXIS_LABELS_COUNT);
//    }

    public void setXAxisRange(GraphView graph, Date minDate, Date maxDate, boolean isDataAvailable) {
        final int HORIZONTAL_AXIS_LABELS_COUNT = 4;
        final long MAX_X_IF_DATA_NOT_AVAILABLE = new Date().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -HORIZONTAL_AXIS_RANGE_IF_DATA_NOT_AVAILABLE_DAYS);
        final long MIN_X_IF_DATA_NOT_AVAILABLE = calendar.getTime().getTime();

        long minAxisX = MIN_X_IF_DATA_NOT_AVAILABLE;
        long maxAxisX = MAX_X_IF_DATA_NOT_AVAILABLE;
        long minDataX;
        long maxDataX;
        if (isDataAvailable) {
            minDataX = minDate.getTime();
            maxDataX = maxDate.getTime();
            if (maxDataX - minDataX < TimeUnit.DAYS.toMillis(HORIZONTAL_AXIS_RANGE_IF_DATA_NOT_AVAILABLE_DAYS)) {
                minAxisX = Math.min(minDataX, MIN_X_IF_DATA_NOT_AVAILABLE);
                maxAxisX = minAxisX + TimeUnit.DAYS.toMillis(HORIZONTAL_AXIS_RANGE_IF_DATA_NOT_AVAILABLE_DAYS);
            } else {
                minAxisX = minDataX;
                maxAxisX = maxDataX;
            }
        }
        graph.getViewport().setMinX(minAxisX);
        graph.getViewport().setMaxX(maxAxisX);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getGridLabelRenderer().setNumHorizontalLabels(HORIZONTAL_AXIS_LABELS_COUNT);
    }

    private void setYAxisRange(GraphView graph, TreeMap<Date, Integer> data, boolean isDataAvailable) {
        int minDataY;
        int maxDataY;
        if (isDataAvailable) {
            //find minDataY and maxDataY
            minDataY = dataManager.getMinValue(data);
            maxDataY = dataManager.getMaxValue(data);

            int averageDataY = (maxDataY + minDataY) / 2;
            final int MIN_AXIS_RANGE_Y = averageDataY / 100;

            if (maxDataY - minDataY < MIN_AXIS_RANGE_Y) {
                graph.getViewport().setMinY(minDataY);
                graph.getViewport().setMaxY(minDataY + MIN_AXIS_RANGE_Y);
                graph.getViewport().setYAxisBoundsManual(true);
            } else {
                int dataRange = maxDataY - minDataY;
                int minAxisY = minDataY - dataRange / 20;
                int maxAxisY = maxDataY + dataRange / 20;
                graph.getViewport().setMinY(minAxisY);
                graph.getViewport().setMaxY(maxAxisY);
            }
        } else {
            graph.getViewport().setMinY(MIN_Y_IF_DATA_NOT_AVAILABLE);
            graph.getViewport().setMaxY(MAX_Y_IF_DATA_NOT_AVAILABLE);
        }
        graph.getViewport().setYAxisBoundsManual(true);
    }

    private void setYAxisRangeNew(GraphView graph, TreeMap<Date, Integer> data, boolean isDataAvailable) {
        int currentAxisLabelCount; //set by other series
        currentAxisLabelCount = graph.getGridLabelRenderer().getNumVerticalLabels();

        final int NORMAL_GRAPHS_AXIS_LABELS_COUNT = 6;
        final int SMALL_DATA_RANGE_GRAPHS_AXIS_LABELS_COUNT = 2;
        int axisLabelsCount;
        final int SMALL_DATA_RANGE_GRAPH_HEIGHT_DP = 100;
        boolean isDataRangeSmall = true;

        int minDataY;
        int maxDataY;
        if (isDataAvailable) {
            minDataY = dataManager.getMinValue(data);
            maxDataY = dataManager.getMaxValue(data);

            int averageDataY = (maxDataY + minDataY) / 2;
            final int MIN_AXIS_RANGE_Y = averageDataY / 100;

            if (maxDataY - minDataY < MIN_AXIS_RANGE_Y) {
                isDataRangeSmall = true;
            } else {
                isDataRangeSmall = false;
            }
        }


        if ( (isDataRangeSmall || !isDataAvailable) && (currentAxisLabelCount != NORMAL_GRAPHS_AXIS_LABELS_COUNT) ) {
            axisLabelsCount = SMALL_DATA_RANGE_GRAPHS_AXIS_LABELS_COUNT;

            //make graph height smaller
            ViewGroup.LayoutParams layoutParams = graph.getLayoutParams();
            layoutParams.height = getPixelsFromDp(SMALL_DATA_RANGE_GRAPH_HEIGHT_DP, context);
            graph.setLayoutParams(layoutParams);
        } else {
            axisLabelsCount = NORMAL_GRAPHS_AXIS_LABELS_COUNT;
        }


        graph.getGridLabelRenderer().setNumVerticalLabels(axisLabelsCount);
    }

    private int getPixelsFromDp(float dp, Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (dp * scale + 0.5f);
        return pixels;
    }

}
