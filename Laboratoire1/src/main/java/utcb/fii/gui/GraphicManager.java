package utcb.fii.gui;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import utcb.fii.model.Measurement;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GraphicManager {

    /**
     * Renders a graphical representation of the relationship between Reynolds number (Re)
     * and the loss coefficient (Lambda) based on provided measurements and regression data.
     * The chart displays both the raw measurement points and a trend model derived
     * from the regression analysis.
     *
     * @param measurementsList a list of {@link Measurement} objects, each containing hydraulic
     *                         properties such as Reynolds number (Re) and loss coefficient (Lambda).
     *                         Only measurements with positive values for Re and Lambda will be included.
     * @param regression       a {@link SimpleRegression} object representing a pre-computed
     *                         regression model used to generate the trend curve on the chart.
     */
    public void drawGraphic(List<Measurement> measurementsList, SimpleRegression regression) {
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