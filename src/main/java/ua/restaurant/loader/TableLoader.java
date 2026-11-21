package ua.restaurant.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.restaurant.exception.InvalidDataException;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.TableZone;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads {@link RestaurantTable} entities from a CSV file.
 */
public class TableLoader {

    private static final Logger log = LoggerFactory.getLogger(TableLoader.class);

    public List<RestaurantTable> load(Path path) {
        List<RestaurantTable> tables = new ArrayList<>();
        log.info("Starting to load tables from {}", path);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line = reader.readLine();
            if (line == null) {
                log.warn("CSV file is empty: {}", path);
                return tables;
            }

            int rowNumber = 1;
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                try {
                    RestaurantTable table = parseLine(line, rowNumber);
                    tables.add(table);
                    log.info("Loaded table: number={}, capacity={}, zone={}",
                            table.tableNumber(), table.capacity(), table.zone());
                } catch (InvalidDataException e) {
                    log.warn("Skipping invalid CSV row: {}", e.getMessage());
                }
            }
            log.info("Successfully loaded {} tables from {}", tables.size(), path);
        } catch (IOException | SecurityException e) {
            log.error("Unable to read CSV file: {}", path, e);
        } finally {
            log.info("Finished loading attempt for {}", path);
        }

        return tables;
    }

    public RestaurantTable parseLine(String line) throws InvalidDataException {
        String[] columns = line.split(",", -1);
        if (columns.length != 3) {
            throw new InvalidDataException("Expected 3 columns, got " + columns.length);
        }

        try {
            int tableNumber = Integer.parseInt(columns[0].trim());
            int capacity = Integer.parseInt(columns[1].trim());
            TableZone zone = TableZone.valueOf(columns[2].trim());
            return new RestaurantTable(tableNumber, capacity, zone);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Invalid table data: " + line, e);
        }
    }

    public RestaurantTable parseLine(String line, int rowNumber) throws InvalidDataException {
        try {
            return parseLine(line);
        } catch (InvalidDataException e) {
            throw new InvalidDataException("Row " + rowNumber + ": " + e.getMessage(), e);
        }
    }
}