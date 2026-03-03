package utcb.fii;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utcb.fii.gui.GraphicManager;
import utcb.fii.io.FileManager;
import utcb.fii.model.Measurement;
import utcb.fii.service.ComputedResults;
import utcb.fii.service.HydraulicEngine;

import java.nio.file.Path;
import java.util.List;

/**
 * The {@code Laboratoire1} class represents the entry point for the Hydraulic Analysis System.
 * This class coordinates the file management, data processing, and graphic representation
 * of hydraulic measurements. It processes raw data from a CSV file, computes additional metrics
 * such as Reynolds numbers and friction factors, and outputs the results to an Excel file and
 * visual graphics.
 * <p>
 * The workflow includes:
 * 1. Reading measurement data from a CSV file.
 * 2. Validating and processing the raw data into computed results.
 * 3. Writing processed data to an Excel file.
 * 4. Logging model regression data.
 * 5. Plotting the data and regression results in a graphical representation.
 * <p>
 * This class integrates other components:
 * - {@code FileManager}: Handles reading and writing data to files.
 * - {@code HydraulicEngine}: Performs the hydraulic computations.
 * - {@code GraphicManager}: Visualizes the results in a graphic format.
 * <p>
 * Logging is used throughout the application to track progress, record errors, and
 * summarize calculations for debugging or auditing purposes.
 * <p>
 * The application terminates early if no valid measurements are found in the input CSV file.
 */
public class Laboratoire1 {
    private static final Logger logger = LoggerFactory.getLogger(Laboratoire1.class);

    public static void main(String[] args) {
        Path resourcesPath = Path.of("Laboratoire1","src", "main", "resources");
        String csvFilePath = resourcesPath.resolve("raw_data.csv").toString();
        String xlsxFilePath = resourcesPath.resolve("processed_data.xlsx").toString();
        logger.info("Start Hydraulic Analysis System...");
        FileManager fileManager = new FileManager();
        HydraulicEngine hydraulicEngine = new HydraulicEngine();
        GraphicManager graphicManager = new GraphicManager();
        logger.info("Reading CSV file from {}...", csvFilePath);
        List<Measurement> measurementsList = fileManager.readFromFile(csvFilePath);
        if (measurementsList.isEmpty()) {
            logger.error("CSV file is empty or no valid row was extracted. For preventing mathematical errors, the program will be terminated.");
            return;
        }
        ComputedResults results = hydraulicEngine.processData(measurementsList);
        logger.info("Writing processed data to Excel file at {}...", xlsxFilePath);
        fileManager.writeToFile(xlsxFilePath, results.processedMeasurements());
        logger.info("Model: Lambda = {} * Re ^ ({})", String.format("%.5f", Math.exp(results.regression().getIntercept())), String.format("%.5f", results.regression().getSlope()));
        graphicManager.drawGraphic(results.processedMeasurements(), results.regression());
    }
}