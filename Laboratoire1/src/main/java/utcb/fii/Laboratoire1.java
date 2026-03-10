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
import java.util.Map;
import java.util.stream.Collectors;


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
        Map<Double, List<Measurement>> differentPieces = results.processedMeasurements().stream().collect(Collectors.groupingBy(Measurement::d_mm));
        differentPieces.forEach((diameter, points) -> {
            var specificRegression = hydraulicEngine.processData(points);
            String fileName = "diagrama_fi" + diameter.intValue() + ".png";
            graphicManager.saveGraphicAsPng(resourcesPath.resolve(fileName).toString(), points, specificRegression.regression());
        });
    }
}