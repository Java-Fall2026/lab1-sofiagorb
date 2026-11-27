package ua.restaurant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.restaurant.exception.InvalidDataException;
import ua.restaurant.loader.TableLoader;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.TableZone;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TableLoader and CSV parsing tests")
class TableLoaderTest {

    private final TableLoader loader = new TableLoader();

    @Test
    @DisplayName("Should parse valid CSV line into RestaurantTable")
    void shouldParseValidCsvLine() throws InvalidDataException {
        RestaurantTable table = loader.parseLine("10,4,TERRACE");

        assertEquals(10, table.tableNumber());
        assertEquals(4, table.capacity());
        assertEquals(TableZone.TERRACE, table.zone());
    }

    @Test
    @DisplayName("Should include row number and cause in InvalidDataException")
    void shouldIncludeRowNumberAndCause() {
        InvalidDataException exception = assertThrows(
                InvalidDataException.class,
                () -> loader.parseLine("abc,4,VIP", 5)
        );

        assertTrue(exception.getMessage().contains("Row 5"));
        assertNotNull(exception.getCause());
        assertInstanceOf(InvalidDataException.class, exception.getCause());
    }

    @Test
    @DisplayName("Should throw InvalidDataException when column count is wrong")
    void shouldThrowForWrongColumnCount() {
        assertThrows(
                InvalidDataException.class,
                () -> loader.parseLine("10,4", 2)
        );
    }

    @Test
    @DisplayName("Should throw InvalidDataException for invalid table zone")
    void shouldThrowForInvalidZone() {
        assertThrows(
                InvalidDataException.class,
                () -> loader.parseLine("10,4,ROOFTOP", 3)
        );
    }

    @Test
    @DisplayName("Should load only valid rows from temporary CSV file")
    void shouldLoadOnlyValidRows(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("tables.csv");
        Files.writeString(file, """
                tableNumber,capacity,zone
                1,4,MAIN_HALL
                bad,2,VIP
                2,6,TERRACE
                """);

        List<RestaurantTable> tables = loader.load(file);

        assertEquals(2, tables.size());
        assertEquals(1, tables.get(0).tableNumber());
        assertEquals(2, tables.get(1).tableNumber());
    }
}