package indicators;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import loaders.CsvTradesLoader;
import java.awt.*;
import java.util.Date;
import org.ta4j.core.indicators.ChopIndicator;


public class CandlestickChartWithChopIndicator {
	private static final int CHOP_INDICATOR_TIMEFRAME = 14;
	private static final double CHOP_UPPER_THRESHOLD = 61.8;
	private static final double CHOP_LOWER_THRESHOLD = 38.2;
	private static final int VOLUME_DATASET_INDEX = 1;
	private static final int CHOP_SCALE_VALUE = 100;
    private static CombinedDomainXYPlot combinedPlot;
	private static JFreeChart combinedChart;
	static DateAxis xAxis = new DateAxis( "Time" );
	private static ChartPanel combinedChartPanel;
	private static XYPlot indicatorXYPlot;
	static Stroke dashedThinLineStyle = 
			new BasicStroke(  0.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] {8.0f, 4.0f}, 0.0f );
	static TimeSeries series;

    private static OHLCDataset createOHLCDataset(TimeSeries series) {
        final int nbBars = series.getBarCount();

        Date[] dates = new Date[nbBars];
        double[] opens = new double[nbBars];
        double[] highs = new double[nbBars];
        double[] lows = new double[nbBars];
        double[] closes = new double[nbBars];
        double[] volumes = new double[nbBars];

        for (int i = 0; i < nbBars; i++) {
            Bar bar = series.getBar(i);
            dates[i] = new Date(bar.getEndTime().toEpochSecond() * 1000);
            opens[i] = bar.getOpenPrice().doubleValue();
            highs[i] = bar.getMaxPrice().doubleValue();
            lows[i] = bar.getMinPrice().doubleValue();
            closes[i] = bar.getClosePrice().doubleValue();
            volumes[i] = bar.getVolume().doubleValue();
        }

        return new DefaultHighLowDataset("btc", dates, highs, lows, opens, closes, volumes);
    }


    private static TimeSeriesCollection createAdditionalDataset(TimeSeries series) {
        ClosePriceIndicator indicator = new ClosePriceIndicator(series);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries("Btc price");
        for (int i = 0; i < series.getBarCount(); i++) {
            Bar bar = series.getBar(i);
            chartTimeSeries.add(new Second(new Date(bar.getEndTime().toEpochSecond() * 1000)), indicator.getValue(i).doubleValue());
        }
        dataset.addSeries(chartTimeSeries);
        return dataset;
    }

    private static TimeSeriesCollection createChopDataset(TimeSeries series) {
		ChopIndicator indicator = new ChopIndicator( series, CHOP_INDICATOR_TIMEFRAME, CHOP_SCALE_VALUE );
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries("CHOP_14");
        for (int i = 0; i < series.getBarCount(); i++) {
            Bar bar = series.getBar(i);
            if ( i < CHOP_INDICATOR_TIMEFRAME ) continue;
            chartTimeSeries.add(new Second(new Date(bar.getEndTime().toEpochSecond() * 1000)), indicator.getValue(i).doubleValue());
        }
        dataset.addSeries(chartTimeSeries);
		return dataset;
	}
    

    private static void displayChart(XYDataset ohlcDataset, XYDataset xyDataset, XYDataset chopSeries) {

    	CandlestickRenderer renderer = new CandlestickRenderer();
    	XYPlot pricePlot = new XYPlot( ohlcDataset, xAxis, new NumberAxis( "Price" ), renderer );
    	renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);

    	pricePlot.setDataset(VOLUME_DATASET_INDEX, xyDataset);
    	pricePlot.mapDatasetToRangeAxis(VOLUME_DATASET_INDEX, 0);

    	XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer(true, false);
    	renderer2.setSeriesPaint(VOLUME_DATASET_INDEX, Color.blue);
    	pricePlot.setRenderer(VOLUME_DATASET_INDEX, renderer2);

    	pricePlot.setRangeGridlinePaint(Color.lightGray);
    	pricePlot.setBackgroundPaint(Color.white);
    	NumberAxis numberAxis = (NumberAxis) pricePlot.getRangeAxis();
    	pricePlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
    	renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);

    	pricePlot.setRangeGridlinePaint(Color.lightGray);
    	pricePlot.setBackgroundPaint(Color.white);
    	numberAxis.setAutoRangeIncludesZero(false);
    	pricePlot.setDatasetRenderingOrder( DatasetRenderingOrder.FORWARD );

    	indicatorXYPlot = new XYPlot( /*null, xAxis, yAxis, renderer*/);
    	indicatorXYPlot.setDataset( chopSeries );
    	indicatorXYPlot.setRangeAxis( 0, new NumberAxis( "" ) );
    	indicatorXYPlot.setRenderer( 0, new XYLineAndShapeRenderer() );
    	NumberAxis yIndicatorAxis = new NumberAxis( "" );
    	yIndicatorAxis.setRange( 0, CHOP_SCALE_VALUE );
		indicatorXYPlot.setRangeAxis( 0, yIndicatorAxis );
		

    	combinedPlot = new CombinedDomainXYPlot( xAxis );	
    	combinedPlot.setGap( 10.0 );
//    	combinedPlot.setDomainAxis( xAxis );
    	combinedPlot.setBackgroundPaint( Color.LIGHT_GRAY );
    	combinedPlot.setDomainGridlinePaint( Color.GRAY );
    	combinedPlot.setRangeGridlinePaint( Color.GRAY );
    	combinedPlot.setOrientation( PlotOrientation.VERTICAL );
    	combinedPlot.add( pricePlot, 70 );
    	combinedPlot.add( indicatorXYPlot, 30 );


    	combinedChart = new JFreeChart( "Bitstamp BTC price with Chop indicator", null, combinedPlot, true );
    	combinedChart.setBackgroundPaint( Color.LIGHT_GRAY );

    	// combinedChartPanel to contain combinedChart
    	combinedChartPanel = new ChartPanel( combinedChart );
    	combinedChartPanel.setLayout( new GridLayout(0,1) );
    	combinedChartPanel.setBackground( Color.LIGHT_GRAY );
    	combinedChartPanel.setPreferredSize(new java.awt.Dimension(740, 300));
		
    	// Application frame
    	ApplicationFrame frame = new ApplicationFrame("Ta4j example - Candlestick chart");
    	frame.setContentPane( combinedChartPanel );
    	frame.pack();
    	RefineryUtilities.centerFrameOnScreen(frame);
    	frame.setVisible(true);
    	
		// CHOP oscillator upper/lower threshold guidelines
		XYLineAnnotation lineAnnotation = new XYLineAnnotation( (double)series.getFirstBar().getBeginTime().toEpochSecond() * 1000d, CHOP_LOWER_THRESHOLD, 
				(double)series.getLastBar().getEndTime().toEpochSecond() * 1000d, CHOP_LOWER_THRESHOLD, dashedThinLineStyle, Color.GREEN );
		lineAnnotation.setToolTipText("tradable below this");
		indicatorXYPlot.addAnnotation( lineAnnotation );
		lineAnnotation = new XYLineAnnotation( (double)series.getFirstBar().getBeginTime().toEpochSecond() * 1000d, CHOP_UPPER_THRESHOLD, 
				(double)series.getLastBar().getEndTime().toEpochSecond() * 1000d, CHOP_UPPER_THRESHOLD, dashedThinLineStyle, Color.RED );
		lineAnnotation.setToolTipText("too choppy above this");
		indicatorXYPlot.addAnnotation( lineAnnotation );
    }

    public static void main(String[] args) {
    	series = CsvTradesLoader.loadBitstampSeries();

    	OHLCDataset ohlcDataset = createOHLCDataset(series);

    	TimeSeriesCollection xyDataset = createAdditionalDataset(series);

    	TimeSeriesCollection chopSeries = createChopDataset( series );

    	displayChart( ohlcDataset, xyDataset, chopSeries );
    }
}
