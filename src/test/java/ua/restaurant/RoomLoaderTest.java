package ua.restaurant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ua.restaurant.exception.InvalidDataException;
import ua.restaurant.loader.RoomLoader;
import ua.restaurant.model.Room;
import ua.restaurant.model.RoomType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomLoaderTest {

    @Test
    void shouldCreateValidRoom() {
        Room room = new Room(101, RoomType.SINGLE, 850.0);

        assertEquals(101, room.roomNumber());
        assertEquals(RoomType.SINGLE, room.type());
        assertEquals(850.0, room.pricePerNight());
    }

    @Test
    void shouldRejectInvalidRoomNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Room(0, RoomType.SINGLE, 850.0)
        );
    }

    @Test
    void shouldRejectInvalidPrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Room(101, RoomType.SINGLE, 0)
        );
    }

    @Test
    void shouldCalculateTotalPriceForSeveralNights() {
        Room room = new Room(101, RoomType.SINGLE, 850.0);

        assertEquals(2550.0, room.totalPrice(3));
    }

    @Test
    void shouldRejectInvalidNumberOfNights() {
        Room room = new Room(101, RoomType.SINGLE, 850.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> room.totalPrice(0)
        );
    }

    @Test
    void shouldParseValidCsvLine() throws InvalidDataException {
        RoomLoader loader = new RoomLoader();

        Room room = loader.parseLine("101,SINGLE,850.0");

        assertEquals(101, room.roomNumber());
        assertEquals(RoomType.SINGLE, room.type());
        assertEquals(850.0, room.pricePerNight());
    }

    @Test
    void shouldThrowInvalidDataExceptionForWrongColumnCount() {
        RoomLoader loader = new RoomLoader();

        assertThrows(
                InvalidDataException.class,
                () -> loader.parseLine("101,SINGLE")
        );
    }

    @Test
    void shouldThrowInvalidDataExceptionForInvalidNumber() {
        RoomLoader loader = new RoomLoader();

        assertThrows(
                InvalidDataException.class,
                () -> loader.parseLine("abc,SINGLE,850.0")
        );
    }

    @Test
    void shouldThrowInvalidDataExceptionForInvalidRoomType() {
        RoomLoader loader = new RoomLoader();

        assertThrows(
                InvalidDataException.class,
                () -> loader.parseLine("101,UNKNOWN,850.0")
        );
    }

    @Test
    void shouldLoadOnlyValidRows(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("rooms.csv");

        Files.writeString(
                file,
                """
                roomNumber,type,pricePerNight
                101,SINGLE,850.0
                bad,SINGLE,900.0
                102,DELUXE,2400.0
                """
        );

        RoomLoader loader = new RoomLoader();

        List<Room> rooms = loader.load(file);

        assertEquals(2, rooms.size());
        assertEquals(101, rooms.get(0).roomNumber());
        assertEquals(102, rooms.get(1).roomNumber());
    }
}