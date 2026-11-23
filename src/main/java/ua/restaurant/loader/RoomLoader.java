package ua.restaurant.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.restaurant.exception.InvalidDataException;
import ua.restaurant.model.Room;
import ua.restaurant.model.RoomType;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RoomLoader {

    private static final Logger log =
            LoggerFactory.getLogger(RoomLoader.class);

    public List<Room> load(Path path) {
        List<Room> rooms = new ArrayList<>();

        log.info("Starting to load rooms from {}", path);

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line = reader.readLine();

            if (line == null) {
                log.warn("CSV file is empty: {}", path);
                return rooms;
            }

            int rowNumber = 1;

            while ((line = reader.readLine()) != null) {
                rowNumber++;

                try {
                    Room room = parseLine(line, rowNumber);
                    rooms.add(room);

                    log.info(
                            "Created room: number={}, type={}, pricePerNight={}",
                            room.roomNumber(),
                            room.type(),
                            room.pricePerNight()
                    );

                } catch (InvalidDataException e) {
                    log.warn(
                            "Skipping invalid CSV row: {}",
                            e.getMessage()
                    );
                }
            }

            log.info(
                    "Loaded {} rooms from {}",
                    rooms.size(),
                    path
            );

        } catch (IOException | SecurityException e) {
            log.error("Unable to read CSV file: {}", path, e);
        } finally {
            log.info("Finished loading attempt for {}", path);
        }

        return rooms;
    }

    public Room parseLine(String line) throws InvalidDataException {
        String[] columns = line.split(",", -1);

        if (columns.length != 3) {
            throw new InvalidDataException(
                    "Expected 3 columns, got " + columns.length
            );
        }

        try {
            int roomNumber = Integer.parseInt(columns[0].trim());
            RoomType type = RoomType.valueOf(columns[1].trim());
            double price = Double.parseDouble(columns[2].trim());

            return new Room(roomNumber, type, price);

        } catch (IllegalArgumentException e) {
            throw new InvalidDataException(
                    "Invalid room data: " + line, e
            );
        }
    }

    public Room parseLine(String line, int rowNumber)
            throws InvalidDataException {
        try {
            return parseLine(line);
        } catch (InvalidDataException e) {
            throw new InvalidDataException(
                    "Row " + rowNumber + ": " + e.getMessage(),
                    e
            );
        }
    }
}