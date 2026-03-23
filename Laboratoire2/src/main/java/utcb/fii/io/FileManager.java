package utcb.fii.io;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utcb.fii.model.Measurement;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    private static final Logger logger = LoggerFactory.getLogger(FileManager.class);

    public List<Measurement> readFromFile(String pathFile) {
        List<Measurement> measurementsList = new ArrayList<>();
        try (Reader reader = new FileReader(pathFile)) {
            CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
            try (CSVParser csvParser = new CSVParser(reader, format)) {
                for (CSVRecord record : csvParser) {
                    try {
                        int nr = Integer.parseInt(record.get("nr"));
                        double q = Double.parseDouble(record.get("q"));
                        double dp = Double.parseDouble(record.get("dp"));
                        String type = record.get("type");
                        Measurement measurement = new Measurement(nr, q, dp, type);
                        measurement.validate();
                        measurementsList.add(measurement);
                    } catch (NumberFormatException e) {
                        logger.error("Format error on row {}: {}", record.getRecordNumber(), e.getMessage());
                    } catch (IllegalArgumentException e) {
                        logger.error("Validation error: {}", e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logger.error("IO error: {}", e.getMessage());
        }
        if (measurementsList.isEmpty()) {
            logger.warn("Warning: no valid row extracted from CSV file!");
        }
        return measurementsList;
    }

    /**
     * Writes the provided list of measurements to an Excel file at the specified file path.
     * The method creates an Excel workbook with a single sheet containing measurement data.
     * Each row corresponds to a measurement, and relevant values are rounded to five decimal places.
     * Auto-sizing is applied to each column for better readability.
     *
     * @param pathFile          the path to the output Excel file
     * @param measurementsList  the list of {@code Measurement} objects to be written to the file
     */
    public void writeToFile(String pathFile, List<Measurement> measurementsList) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Hydraulique Laboratoire 2");
            Row headerRow = sheet.createRow(0);
            String[] cols = {"Nr", "Q (l/s)", "dP (mbar)", "Zeta", "Re", "Type"};
            for (int i = 0; i < cols.length; ++i) {
                headerRow.createCell(i).setCellValue(cols[i]);
            }
            int rowNum = 1;
            for (Measurement measurement : measurementsList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(measurement.nr());
                row.createCell(1).setCellValue((measurement.q()));
                row.createCell(2).setCellValue((measurement.dp()));
                row.createCell(3).setCellValue((measurement.getZeta()));
                row.createCell(4).setCellValue((measurement.getReynolds()));
                row.createCell(5).setCellValue((measurement.type()));
            }
            for (int i = 0; i < cols.length; ++i) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream fileOut = new FileOutputStream(pathFile)) {
                workbook.write(fileOut);
                logger.info("Excel saved successfully at: {}", pathFile);
            }
        } catch (IOException e) {
            logger.error("Excel error: {}", e.getMessage());
        }
    }
}
