package utcb.fii.io;

import org.junit.jupiter.api.Test;
import utcb.fii.model.Measurement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileManagerTest {

    @Test
    void testReadFromFile_WithValidData() throws IOException {
        // Arrange
        String csvContent = """
                Nr,Q,dP,D
                1,10.5,25.0,50.0
                2,5.2,15.0,30.0
                """;

        File tempFile = File.createTempFile("test_valid_data", ".csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write(csvContent);
        }
        FileManager fileManager = new FileManager();

        // Act
        List<Measurement> measurements = fileManager.readFromFile(tempFile.getAbsolutePath());

        // Assert
        assertEquals(2, measurements.size(), "Expected 2 valid measurements to be read");
        assertEquals(1, measurements.getFirst().nr());
        assertEquals(10.5, measurements.getFirst().q_ls());
        assertEquals(25.0, measurements.getFirst().dp_mbar());
    }

    @Test
    void testReadFromFile_WithInvalidRows() throws IOException {
        // Arrange
        String csvContent = """
                Nr,Q,dP,D
                1,10.5,25.0,50.0
                2,invalid,15.0,30.0
                3,5.0,-10.0,40.0
                """;

        File tempFile = File.createTempFile("test_invalid_data", ".csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write(csvContent);
        }
        FileManager fileManager = new FileManager();

        // Act
        List<Measurement> measurements = fileManager.readFromFile(tempFile.getAbsolutePath());

        // Assert
        // Așteptăm 2 rânduri:
        // - Rândul 1 (valid)
        // - Rândul 3 (dP negativ, dar păstrat pentru afișarea în Excel)
        // Rândul 2 (format text invalid) trebuie săpat complet.
        assertEquals(2, measurements.size(), "Expected 2 measurements to be read");

        assertEquals(1, measurements.get(0).nr());

        assertEquals(3, measurements.get(1).nr());
        assertEquals(-10.0, measurements.get(1).dp_mbar());
    }

    @Test
    void testReadFromFile_AllInvalidRows() throws IOException {
        // Arrange
        String csvContent = """
                Nr,Q,dP,D
                1,-10.5,25.0,50.0
                2,5.2,-15.0,30.0
                3,5.0,15.0,-40.0
                """;

        File tempFile = File.createTempFile("test_all_invalid", ".csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write(csvContent);
        }
        FileManager fileManager = new FileManager();

        // Act
        List<Measurement> measurements = fileManager.readFromFile(tempFile.getAbsolutePath());

        // Assert
        // Doar rândul 2 supraviețuiește (pentru că doar dP e negativ, Q și D sunt bune)
        assertEquals(1, measurements.size(), "Expected exactly 1 measurement to be kept (the one with negative dP)");
        assertEquals(2, measurements.getFirst().nr());
        assertEquals(-15.0, measurements.getFirst().dp_mbar());
    }

    @Test
    void testReadFromFile_EmptyFile() throws IOException {
        // Arrange
        File tempFile = File.createTempFile("test_empty_file", ".csv");
        FileManager fileManager = new FileManager();

        // Act
        List<Measurement> measurements = fileManager.readFromFile(tempFile.getAbsolutePath());

        // Assert
        assertTrue(measurements.isEmpty(), "Expected no measurements for an empty file");
    }

    @Test
    void testReadFromFile_NonExistentFile() {
        // Arrange
        String nonExistentPath = "this_file_does_not_exist.csv";
        FileManager fileManager = new FileManager();

        // Act
        List<Measurement> measurements = fileManager.readFromFile(nonExistentPath);

        // Assert
        assertTrue(measurements.isEmpty(), "Expected no measurements when the file does not exist");
    }
}