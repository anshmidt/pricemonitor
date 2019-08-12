package com.anshmidt.pricemonitor;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.anshmidt.pricemonitor.exceptions.EmptyDataException;
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

    private DataManager dataManager;
    private Context context;
    private final String DATES_ON_AXIS_FORMAT = "dd/MM";
    private final int PADDING_TO_FIT_VERTICAL_AXIS_LABELS = 74;
    private final int GRAPH_LINE_THICKNESS = 8;

    private final int HORIZONTAL_AXIS_RANGE_IF_DATA_NOT_AVAILABLE_DAYS = 3;

    private final int POINTS_SIZE = 12;



    public GraphPlotter(Context context, DataManager dataManager) {
        this.context = context;
        this.dataManager = dataManager;
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
            try {
                sortedKeys = dataManager.getSortedKeys(data);
            } catch (EmptyDataException e) {
                throw new RuntimeException("Data is empty");
            }
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
        setYAxisRange(graph, data, isDataAvailable);


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATES_ON_AXIS_FORMAT);
        gridLabelRenderer.setLabelFormatter(new DateAsXAxisLabelFormatter(context, simpleDateFormat));

        gridLabelRenderer.setHumanRounding(false, true);

        graph.getViewport().setScrollable(false); // enables horizontal scrolling
        graph.getViewport().setScrollableY(false); // enables vertical scrolling
        graph.getViewport().setScalable(false); // enables horizontal zooming and scrolling
        graph.getViewport().setScalableY(false); // enables vertical zooming and scrolling
    }



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
            try {
                minDataY = dataManager.getMinValue(data);
                maxDataY = dataManager.getMaxValue(data);

                int averageDataY = (maxDataY + minDataY) / 2;
                final int MIN_AXIS_RANGE_Y = averageDataY / 100;

                if (maxDataY - minDataY < MIN_AXIS_RANGE_Y) {
                    isDataRangeSmall = true;
                } else {
                    isDataRangeSmall = false;
                }
            } catch (EmptyDataException e) {
                throw new RuntimeException("Data is empty");
            }
        }


        if ( (isDataRangeSmall || !isDataAvailable) && (currentAxisLabelCount != NORMAL_GRAPHS_AXIS_LABELS_COUNT) ) {
            axisLabelsCount = SMALL_DATA_RANGE_GRAPHS_AXIS_LABELS_COUNT;

            //make graph height smaller
            ViewGroup.LayoutParams layoutParams = graph.getLayoutParams();
            layoutParams.height = getPixelsFromDp(SMALL_DATA_RANGE_GRAPH_HEIGHT_DP);
            graph.setLayoutParams(layoutParams);
        } else {
            axisLabelsCount = NORMAL_GRAPHS_AXIS_LABELS_COUNT;
        }


        graph.getGridLabelRenderer().setNumVerticalLabels(axisLabelsCount);
    }

    private int getPixelsFromDp(float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        int pixels = (int) (dp * scale + 0.5f);
        return pixels;
    }

}
