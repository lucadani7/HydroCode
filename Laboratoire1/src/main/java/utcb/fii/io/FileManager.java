package utcb.fii.io;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
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

    /**
     * Reads measurement data from a CSV file and returns a list of {@code Measurement} objects.
     * The method processes each record from the file and extracts relevant measurement fields.
     * Rows with invalid or missing data are logged and skipped.
     *
     * @param pathFile the path to the CSV file to be read
     * @return a list of {@code Measurement} objects extracted from the file;
     *         if no valid rows are found, the list is empty
     */
    public List<Measurement> readFromFile(String pathFile) {
        List<Measurement> measurementsList = new ArrayList<>();
        try (Reader reader = new FileReader(pathFile)) {
            CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
            try (CSVParser csvParser = new CSVParser(reader, format)) {
                for (CSVRecord record : csvParser) {
                    try {
                        int nr = Integer.parseInt(record.get("Nr"));
                        double q = Double.parseDouble(record.get("Q"));
                        double dp = Double.parseDouble(record.get("dP"));
                        double d = Double.parseDouble(record.get("D"));
                        if (d <= 0 || q <= 0) {
                            logger.warn("Row {} skipped completed: diameter or debit is zero / negative.", nr);
                            continue;
                        }
                        if (dp <= 0) {
                            logger.warn("Warning at row {}: dP={}. It will appear in Excel, but will be skipped on the graphic!", nr, dp);
                        }
                        measurementsList.add(new Measurement(nr, q, dp, dp, d, 0, 0)); // while reading, re and lambda are set to 0
                    } catch (NumberFormatException e) {
                        logger.error("Format error: {}", e.getMessage());
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
     * Writes the given list of {@code Measurement} objects to an Excel file at the specified path.
     * Each measurement is written to a row in the file, and column headers are included in the first row.
     * The Excel file is automatically formatted with adjusted column widths.
     * Handles I/O exceptions by logging errors.
     *
     * @param pathFile The path to the Excel file to write the measurements to.
     * @param measurementsList The list of {@code Measurement} objects to be written to the file.
     */
    public void writeToFile(String pathFile, List<Measurement> measurementsList) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Hydraulique Laboratoire 1");
            Row headerRow = sheet.createRow(0);
            String[] cols = {"Nr", "Q (l/s)", "dP (mbar)", "dP (Pa)", "D (mm)", "Re", "Lambda"};
            for (int i = 0; i < cols.length; ++i) {
                headerRow.createCell(i).setCellValue(cols[i]);
            }
            int rowNum = 1;
            for (Measurement measurement : measurementsList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(measurement.nr());
                row.createCell(1).setCellValue(measurement.q_ls());
                row.createCell(2).setCellValue(measurement.dp_mbar());
                row.createCell(3).setCellValue(measurement.dp_pa());
                row.createCell(4).setCellValue(measurement.d_mm());
                row.createCell(5).setCellValue(measurement.re());
                row.createCell(6).setCellValue(measurement.lambda());
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
