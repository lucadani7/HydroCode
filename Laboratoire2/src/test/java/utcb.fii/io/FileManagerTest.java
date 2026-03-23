package utcb.fii.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utcb.fii.model.Measurement;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileManagerTest {
    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Test correct reading from CSV")
    void testReadFromCsv() throws IOException {
        Path tempCsv = tempDir.resolve("temp_raw_data.csv");
        try (FileWriter writer = new FileWriter(tempCsv.toFile())) {
            writer.write("nr,q,dp,type\n");
            writer.write("1,0.41,631,1 rouge\n");
            writer.write("7,0.41,233,2 robineti\n");
        }
        FileManager fm = new FileManager();
        List<Measurement> list = fm.readFromFile(tempCsv.toString());
        assertEquals(2, list.size(), "It should read 2 measurements from the CSV file");
        assertEquals("1 rouge", list.get(0).type());
        assertEquals("2 robineti", list.get(1).type());
    }

    @Test
    @DisplayName("Test writing to Excel file")
    void testWriteToExcel() {
        FileManager fm = new FileManager();
        List<Measurement> mockData = List.of(new Measurement(1, 0.41, 631, "test"));
        Path tempExcel = tempDir.resolve("test_output.xlsx");
        assertDoesNotThrow(() -> fm.writeToFile(tempExcel.toString(), mockData));
        assertTrue(tempExcel.toFile().exists(), "The Excel file should have been created");
    }
}
