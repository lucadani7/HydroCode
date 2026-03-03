package utcb.fii.service;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import utcb.fii.model.Measurement;

import java.util.List;

/**
 * A record that represents the results of hydraulic computations.
 *
 * This record encapsulates a list of processed measurements and a regression model
 * that establishes the relationship between Reynolds number and the friction factor.
 *
 * @param processedMeasurements The list of processed {@code Measurement} objects, each containing
 *                              computed values such as Reynolds numbers and friction factors.
 * @param regression            A {@code SimpleRegression} instance representing the regression model
 *                              for the logarithmic relationship between Reynolds number and the friction factor.
 */
public record ComputedResults(List<Measurement> processedMeasurements, SimpleRegression regression) {
}
