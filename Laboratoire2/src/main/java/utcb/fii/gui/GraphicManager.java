package utcb.fii.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import utcb.fii.model.Measurement;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GraphicManager {
    public void generateAllCharts(List<Measurement> measurements, Path outputFolder) throws IOException {
        Map<String, List<Measurement>> groupedData = measurements.stream().collect(Collectors.groupingBy(Measurement::type));
        generateXYChart(groupedData, "Pressure Drop vs Flow Rate", "Flow Rate (L/s)", "Pressure Drop (mbar)", outputFolder.resolve("chart_pressure_drop.png").toFile(), false);
        generateXYChart(groupedData, "Loss Coefficient (Zeta) vs Reynolds Number", "Reynolds Number (-)", "Zeta (-)", outputFolder.resolve("chart_zeta_re.png").toFile(), true);
        generateZetaComparisonChart(measurements, outputFolder);
    }

    public void generateZetaComparisonChart(List<Measurement> measurements, Path outputFolder) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> averageZetaMap = measurements.stream().collect(Collectors.groupingBy(Measurement::type, Collectors.averagingDouble(Measurement::getZeta)));
        averageZetaMap.forEach((type, avgZeta) -> dataset.addValue(avgZeta, "Average Zeta", type));
        JFreeChart barChart = ChartFactory.createBarChart(
                "Average Loss Coefficient (Zeta) Comparison",
                "Component Type",
                "Average Zeta (-)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        BarRenderer renderer = getBarRenderer(barChart);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        File outputFile = outputFolder.resolve("chart_zeta_comparison.png").toFile();
        ChartUtils.saveChartAsPNG(outputFile, barChart, 800, 600);
    }

    private static BarRenderer getBarRenderer(JFreeChart barChart) {
        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.BLACK);
        DecimalFormat dfNoDecimals = new DecimalFormat("0");
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setNumberFormatOverride(dfNoDecimals);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(79, 129, 189));
        return renderer;
    }

    private void generateXYChart(Map<String, List<Measurement>> groupedData, String title, String xAxisLabel, String yAxisLabel, File outputFile, boolean isZetaChart) throws IOException {
        XYSeriesCollection dataset = getXySeriesCollection(groupedData, isZetaChart);
        JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, true);
        plot.setRenderer(renderer);
        DecimalFormat dfNoDecimals = new DecimalFormat("0");
        ((NumberAxis) plot.getDomainAxis()).setNumberFormatOverride(dfNoDecimals);
        ((NumberAxis) plot.getRangeAxis()).setNumberFormatOverride(dfNoDecimals);
        ChartUtils.saveChartAsPNG(outputFile, chart, 800, 600);
    }

    private static XYSeriesCollection getXySeriesCollection(Map<String, List<Measurement>> groupedData, boolean isZetaChart) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        for (Map.Entry<String, List<Measurement>> entry : groupedData.entrySet()) {
            XYSeries series = new XYSeries(entry.getKey());
            for (Measurement m : entry.getValue()) {
                if (isZetaChart) {
                    series.add(m.getReynolds(), m.getZeta());
                } else {
                    series.add(m.q(), m.dp());
                }
            }
            dataset.addSeries(series);
        }
        return dataset;
    }
}
