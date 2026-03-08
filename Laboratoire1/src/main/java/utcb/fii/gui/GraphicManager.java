package utcb.fii.gui;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utcb.fii.model.Measurement;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class GraphicManager {
    private static final Logger logger = LoggerFactory.getLogger(GraphicManager.class);

    /**
     * Saves a graphical representation of the relationship between Reynolds number (Re)
     * and the loss coefficient (Lambda) as a PNG image file. The graph is created using
     * the provided measurement data and regression model.
     *
     * @param filePath          the file path where the PNG image should be saved.
     *                           The path must include the filename and ".png" extension.
     * @param measurementsList  a list of {@link Measurement} objects, each containing
     *                           data such as the Reynolds number (Re) and the loss coefficient (Lambda).
     *                           Only measurements with positive Re and Lambda values are considered
     *                           for generating the chart.
     * @param regression        a {@link SimpleRegression} object representing
     *                           the pre-computed regression model used to generate
     *                           the trend line displayed on the chart.
     */
    public void saveGraphicAsPng(String filePath, List<Measurement> measurementsList, SimpleRegression regression) {
        JFreeChart chart = createChart(measurementsList, regression);
        try {
            File outputFile = new File(filePath);
            ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
            logger.info("Graphic successfully saved as PNG at: {}", outputFile.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save graphic: {}", e.getMessage());
        }
    }

    /**
     * Draws a graphical representation of the relationship between Reynolds number (Re)
     * and the loss coefficient (Lambda) using the provided measurement data and regression model.
     * The graph is displayed in a new window with an interactive chart.
     *
     * @param measurementsList a list of {@link Measurement} objects. Each measurement contains
     *                         properties such as the Reynolds number (Re) and the loss coefficient (Lambda).
     *                         These values are used to plot the measured data points in the chart.
     *                         Only measurements with positive Re and Lambda values are considered.
     * @param regression       a {@link SimpleRegression} object representing the pre-computed regression
     *                         model used to generate a trend line in the graph. This model provides
     *                         analytical insights into the relationship between Re and Lambda.
     */
    public void drawGraphic(List<Measurement> measurementsList, SimpleRegression regression) {
        JFreeChart chart = createChart(measurementsList, regression);
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("UTCB Hydraulique - Laboratoire 1");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(800, 600));
            frame.add(chartPanel, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    /**
     * Creates a JFreeChart object that visualizes the relationship between Reynolds number (Re)
     * and the loss coefficient (Lambda) using an XY line chart. The chart includes a dataset
     * with measurement points and a trend model created using the provided regression data.
     * <p>
     * The chart configuration includes:
     * - Logarithmic axes for both the x-axis ("Reynolds Number (Re)") and y-axis ("Loss Coef. (Lambda)").
     * - A clean, styled viewport with grid lines for improved readability.
     * - Rendering settings for distinguishing between measurement points and the trend line.
     *
     * @param measurementsList a list of {@link Measurement} objects. Each measurement must include
     *                         the Reynolds number (Re) and the loss coefficient (Lambda). Only
     *                         measurements with positive values for Re and Lambda are considered when
     *                         creating the dataset.
     * @param regression       a {@link SimpleRegression} object containing the pre-computed trend
     *                         model parameters. This model is used to generate the trend line in
     *                         the chart.
     * @return a {@link JFreeChart} object configured to display the XY line chart for the given data.
     */
    private JFreeChart createChart(List<Measurement> measurementsList, SimpleRegression regression) {
        XYSeriesCollection dataset = getXySeriesCollection(measurementsList, regression);
        JFreeChart chart = ChartFactory.createXYLineChart("Lambda Diagram = f(Re)", "Reynolds Number (Re)",
                "Loss Coef. (Lambda)", dataset, PlotOrientation.VERTICAL, true, true, false);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        LogAxis xAxis = new LogAxis("Reynolds Number (Re)");
        LogAxis yAxis = new LogAxis("Loss Coef. (Lambda)");
        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-3, -3, 6, 6));
        renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesPaint(1, Color.RED);
        renderer.setSeriesStroke(1, new BasicStroke(3.0f));
        plot.setRenderer(renderer);
        return chart;
    }

    /**
     * Creates an {@link XYSeriesCollection} dataset containing measurement points and a trend model
     * for plotting the relationship between Reynolds number (Re) and the loss coefficient (Lambda).
     * <p>
     * The dataset includes two series:
     * - "Measurements": Represents the raw data points extracted from the provided measurements list.
     * - "Trend Model": Represents a computed trend curve based on the provided regression data.
     * <p>
     * Only measurements with positive Reynolds number and positive loss coefficient are considered.
     * The trend model is computed using the exponential relationship lambda = A * Re^n, where
     * A and n are derived from the regression parameters.
     *
     * @param measurementsList a list of {@link Measurement} objects, each containing properties
     *                         such as Reynolds number (Re) and loss coefficient (Lambda).
     *                         Only measurements with positive values for Re and Lambda are included.
     * @param regression       a {@link SimpleRegression} object representing the pre-computed
     *                         regression model used to define the trend curve in the dataset.
     * @return an {@link XYSeriesCollection} dataset containing the raw measurement points and the
     *         computed trend model for visualization.
     */
    private XYSeriesCollection getXySeriesCollection(List<Measurement> measurementsList, SimpleRegression regression) {
        XYSeries dateExp = new XYSeries("Measurements");
        double minRe = Double.MAX_VALUE;
        double maxRe = Double.MIN_VALUE;
        for (Measurement m : measurementsList) {
            if (m.re() > 0 && m.lambda() > 0) {
                dateExp.add(m.re(), m.lambda());
                minRe = Math.min(minRe, m.re());
                maxRe = Math.max(maxRe, m.re());
            }
        }
        double n = regression.getSlope();
        double A = Math.exp(regression.getIntercept());
        XYSeries trend = new XYSeries("Trend Model");
        if (minRe < maxRe) {
            double step = (maxRe - minRe) / 100;
            for (double re = minRe; re <= maxRe; re += step) {
                double lambdaTrend = A * Math.pow(re, n);
                trend.add(re, lambdaTrend);
            }
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dateExp);
        dataset.addSeries(trend);
        return dataset;
    }
}