package utcb.fii.service;

import org.junit.jupiter.api.Test;
import utcb.fii.model.Measurement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link HydraulicEngine}.
 * <p>
 * The `processData` method is tested for a variety of scenarios:
 * - It processes a list of raw data into calculated measurements.
 * - It computes Reynolds numbers and friction factors correctly.
 * - It performs regression modeling on valid data points only.
 */
public class HydraulicEngineTest {

    @Test
    public void testProcessDataWithValidInput() {
        // Arrange
        List<Measurement> rawData = new ArrayList<>();
        rawData.add(new Measurement(1, 500, 100, 0, 50, 0, 0)); // Example values
        HydraulicEngine engine = new HydraulicEngine();

        // Act
        ComputedResults results = engine.processData(rawData);

        // Assert
        assertNotNull(results);
        assertNotNull(results.processedMeasurements());
        assertEquals(1, results.processedMeasurements().size());

        Measurement processed = results.processedMeasurements().getFirst();
        assertEquals(500, processed.q_ls());
        assertEquals(100 * 100, processed.dp_pa()); // mbar -> Pa conversion
        assertTrue(processed.re() > 0); // Ensure Reynold's number is computed
        assertTrue(processed.lambda() > 0); // Ensure a friction factor is computed
    }

    @Test
    public void testProcessDataWithZeroPipeDiameter() {
        // Arrange
        List<Measurement> rawData = new ArrayList<>();
        rawData.add(new Measurement(1, 500, 100, 0, 0, 0, 0)); // Zero diameter
        HydraulicEngine engine = new HydraulicEngine();

        // Act
        ComputedResults results = engine.processData(rawData);

        // Assert
        assertNotNull(results);
        assertNotNull(results.processedMeasurements());
        assertEquals(1, results.processedMeasurements().size());

        Measurement processed = results.processedMeasurements().getFirst();
        assertEquals(0, processed.re()); // Zero Reynolds number due to zero diameter
        assertEquals(0, processed.lambda()); // Zero friction factor due to invalid velocity
    }

    @Test
    public void testProcessDataWithNegativeFlowRate() {
        // Arrange
        List<Measurement> rawData = new ArrayList<>();
        rawData.add(new Measurement(1, -500, 100, 0, 50, 0, 0)); // Negative flow rate
        HydraulicEngine engine = new HydraulicEngine();

        // Act
        ComputedResults results = engine.processData(rawData);

        // Assert
        assertNotNull(results);
        assertNotNull(results.processedMeasurements());
        assertEquals(1, results.processedMeasurements().size());

        Measurement processed = results.processedMeasurements().getFirst();
        assertTrue(processed.re() < 0); // Negative Reynolds number due to negative flow rate
        assertEquals(0, processed.lambda()); // Zero friction factor due to invalid velocity
    }

    @Test
    public void testProcessDataWithRegressionModel() {
        // Arrange
        List<Measurement> rawData = new ArrayList<>();
        rawData.add(new Measurement(1, 500, 100, 0, 50, 0, 0));
        rawData.add(new Measurement(2, 600, 200, 0, 60, 0, 0));
        HydraulicEngine engine = new HydraulicEngine();

        // Act
        ComputedResults results = engine.processData(rawData);

        // Assert
        assertNotNull(results);
        assertNotNull(results.processedMeasurements());
        assertEquals(2, results.processedMeasurements().size());
        assertNotNull(results.regression());
        assertTrue(results.regression().getN() > 0); // Regression should have data points
    }

    @Test
    public void testProcessDataWithEmptyInput() {
        // Arrange
        List<Measurement> rawData = new ArrayList<>();
        HydraulicEngine engine = new HydraulicEngine();

        // Act
        ComputedResults results = engine.processData(rawData);

        // Assert
        assertNotNull(results);
        assertNotNull(results.processedMeasurements());
        assertTrue(results.processedMeasurements().isEmpty());
        assertNotNull(results.regression());
        assertEquals(0, results.regression().getN()); // No data in regression
    }
}