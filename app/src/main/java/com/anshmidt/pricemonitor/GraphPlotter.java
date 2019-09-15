package com.anshmidt.pricemonitor;

import android.content.Context;
import android.view.ViewGroup;

import com.anshmidt.pricemonitor.data.DataManager;
import com.anshmidt.pricemonitor.room.entity.Price;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GraphPlotter {

    /**
     * X axis - Date
     * Y axis - Price
     */

    private DataManager dataManager;
    private Context context;
    private final String DATE_LABELS_FORMAT = "dd/MM";
//    private final String DATE_LABELS_FORMAT = "dd"; //temp
    private final int PADDING_TO_FIT_VERTICAL_AXIS_LABELS = 74;
    private final int GRAPH_LINE_THICKNESS = 8;

    private final int HORIZONTAL_AXIS_RANGE_IF_DATA_NOT_AVAILABLE_DAYS = 3;

    private final int POINTS_SIZE = 12;




    public GraphPlotter(Context context, DataManager dataManager) {
        this.context = context;
        this.dataManager = dataManager;
    }

    public void createGraph(List<Price> prices, GraphView graph, int graphColor, boolean pointsShown) {
        clearGraph(graph);
        addSeriesToGraph(prices, graph, graphColor, pointsShown);
    }

    public void clearGraph(GraphView graph) {
        graph.removeAllSeries(); //clear existing data from graph
    }

    public void addSeriesToGraph(List<Price> prices, GraphView graph, int graphColor, boolean pointsShown) {
        boolean isDataAvailable = false;

        List<Date> sortedDates = dataManager.getSortedDates(prices);
        if (sortedDates.size() > 0) {
            isDataAvailable = true;
        }

        if (isDataAvailable) {
            DataPoint[] dataPoints = new DataPoint[prices.size()];

            int i = 0;
            for (Price price : prices) {
                Integer value = price.price;
                Date date = price.date;
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

        gridLabelRenderer.setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);

        setYAxisRange(graph, prices, isDataAvailable);

        gridLabelRenderer.setHumanRounding(false, true);

        graph.getViewport().setScrollable(false); // enables horizontal scrolling
        graph.getViewport().setScrollableY(false); // enables vertical scrolling
        graph.getViewport().setScalable(false); // enables horizontal zooming and scrolling
        graph.getViewport().setScalableY(false); // enables vertical zooming and scrolling

    }



    public void setXAxisRange(GraphView graph, Date minDate, Date maxDate, boolean isDataAvailable) {
        final int HORIZONTAL_AXIS_LABELS_COUNT = 4;
//        final int HORIZONTAL_AXIS_LABELS_COUNT = 3;
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

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_LABELS_FORMAT);
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(context, simpleDateFormat));

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(minAxisX);
        graph.getViewport().setMaxX(maxAxisX);
        graph.getGridLabelRenderer().setNumHorizontalLabels(HORIZONTAL_AXIS_LABELS_COUNT);

    }



    private void setYAxisRange(GraphView graph, List<Price> prices, boolean isDataAvailable) {
        int currentAxisLabelCount; //set by other series
        currentAxisLabelCount = graph.getGridLabelRenderer().getNumVerticalLabels();

        final int NORMAL_GRAPHS_AXIS_LABELS_COUNT = 6;
        final int SMALL_DATA_RANGE_GRAPHS_AXIS_LABELS_COUNT = 2;
        int axisLabelsCount;
        final int SMALL_DATA_RANGE_GRAPH_HEIGHT_DP = 100;
        boolean isDataRangeSmall = true;

        int minPrice;
        int maxPrice;
        if (isDataAvailable) {
            minPrice = dataManager.getMinPrice(prices).price;
            maxPrice = dataManager.getMaxPrice(prices).price;

            int averagePrice = (maxPrice + minPrice) / 2;
            final int MIN_AXIS_RANGE_Y = averagePrice / 100;

            if (maxPrice - minPrice < MIN_AXIS_RANGE_Y) {
                isDataRangeSmall = true;
            } else {
                isDataRangeSmall = false;
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

//    public void addData(GraphView graph, Price newPrice) {
//        series.appendData(new DataPoint(newPrice.date, newPrice.price))
//    }

}
