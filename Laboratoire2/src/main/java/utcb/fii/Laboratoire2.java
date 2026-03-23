package utcb.fii;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utcb.fii.gui.GraphicManager;
import utcb.fii.io.FileManager;
import utcb.fii.model.Measurement;

import java.nio.file.Path;
import java.util.List;

public class Laboratoire2 {
    private static final Logger logger = LoggerFactory.getLogger(Laboratoire2.class);

    public static void main(String[] args) {
        Path resourcesPath = Path.of("Laboratoire2","src", "main", "resources");
        String csvFilePath = resourcesPath.resolve("raw_data.csv").toString();
        String xlsxFilePath = resourcesPath.resolve("processed_results.xlsx").toString();
        logger.info("Start Hydraulic Analysis System...");
        FileManager fileManager = new FileManager();
        GraphicManager graphicManager = new GraphicManager();
        logger.info("Reading CSV file from {}...", csvFilePath);
        List<Measurement> measurementsList = fileManager.readFromFile(csvFilePath);
        if (measurementsList.isEmpty()) {
            logger.error("CSV file is empty or no valid row was extracted. For preventing mathematical errors, the program will be terminated.");
            return;
        }
        logger.info("Exporting processed data to Excel: {}", xlsxFilePath);
        fileManager.writeToFile(xlsxFilePath, measurementsList);
        logger.info("Starting graphical analysis...");
        try {
            graphicManager.generateZetaComparisonChart(measurementsList, resourcesPath);
            graphicManager.generateAllCharts(measurementsList, resourcesPath);
            logger.info("All charts (PNG) have been saved in the resources folder.");
        } catch (Exception e) {
            logger.error("Error during chart generation: {}", e.getMessage());
        }
    }
}