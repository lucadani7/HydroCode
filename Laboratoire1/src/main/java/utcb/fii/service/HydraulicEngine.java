package utcb.fii.service;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import utcb.fii.model.Measurement;

import java.util.ArrayList;
import java.util.List;

public class HydraulicEngine {
    private static final double RHO = 1000.0;
    private static final double NIU = 1e-6;
    private static final double L = 1.0;

    /**
     * Processes a list of raw measurements to compute additional data, such as
     * Reynolds numbers, friction factors, and a regression model for the relationship
     * between Reynolds number and the friction factor.
     *
     * @param rawData the list of raw measurements that include flow rate, pressure drop,
     *                and pipe diameter, among other properties.
     * @return a {@code ComputedResults} object that contains the processed measurements
     *         with computed values and a regression model for analysis.
     */
    public ComputedResults processData(List<Measurement> rawData) {
        List<Measurement> processedData = new ArrayList<>();
        SimpleRegression regression = new SimpleRegression();
        for (Measurement measurement : rawData) {
            double q = measurement.q_ls() / 1000.0;
            double dp_pa = measurement.dp_mbar() * 100.0; // 1 mbar = 100 Pa
            double d = measurement.d_mm() / 1000.0;
            double area = (Math.PI * Math.pow(d, 2)) / 4.0;
            double v = (area <= 0) ? 0 : q / area;
            double re = (d <= 0) ? 0 : (4 * q) / (Math.PI * d * NIU);
            double lambda = (v <= 0) ? 0 : (dp_pa * d * 2) / (L * RHO * Math.pow(v, 2));
            if (re > 0 && lambda > 0) {
                regression.addData(Math.log(re), Math.log(lambda));
            }
            processedData.add(new Measurement(measurement.nr(), measurement.q_ls(), measurement.dp_mbar(), dp_pa, measurement.d_mm(), re, lambda));
        }
        return new ComputedResults(processedData, regression);
    }
}
