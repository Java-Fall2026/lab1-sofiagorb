package ua.restaurant;

import ua.restaurant.loader.RoomLoader;
import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Room;
import ua.restaurant.model.TableZone;
import ua.restaurant.util.RestaurantUtils;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // 1. Creating objects using record and enum

        Customer customer = Customer.of(
                " +380671234567 ",
                "sofia",
                "sofia@EXAMPLE.COM"
        );

        RestaurantTable table = new RestaurantTable(
                15,
                4,
                TableZone.TERRACE
        );

        Reservation reservation = new Reservation(
                customer,
                table,
                LocalDate.of(2026, 9, 25),
                LocalTime.of(18, 0),
                LocalTime.of(20, 30),
                3,
                ReservationStatus.PENDING
        );

        RestaurantOrder order = RestaurantOrder.of(
                "ORD-501",
                reservation,
                1200.0,
                150.0
        );

        // 2. Record component access

        System.out.println("=== RECORD COMPONENTS ===");
        System.out.println("Customer phone: " + customer.phone());
        System.out.println("Customer name: " + customer.name());
        System.out.println("Customer email: " + customer.email());

        System.out.println("Table number: " + table.tableNumber());
        System.out.println("Table capacity: " + table.capacity());
        System.out.println("Table zone: " + table.zone());
        System.out.println();

        // 3. Enum values(), field and valueOf()

        System.out.println("=== ENUM ===");

        System.out.println("Reservation statuses:");

        for (ReservationStatus status : ReservationStatus.values()) {
            System.out.println(status);
        }

        System.out.println(
                "Terrace price coefficient: "
                        + TableZone.TERRACE.getPriceCoefficient()
        );

        try {
            ReservationStatus.valueOf("UNKNOWN");
        } catch (IllegalArgumentException e) {
            System.out.println(
                    "Invalid enum value: " + e.getMessage()
            );
        }

        System.out.println();

        // 4. Switch expressions

        System.out.println("=== SWITCH EXPRESSIONS ===");

        System.out.println(
                "Zone description: "
                        + RestaurantUtils.zoneDescription(table.zone())
        );

        System.out.println(
                "Available action: "
                        + RestaurantUtils.availableAction(
                                reservation.getStatus()
                        )
        );

        System.out.println();

        // 5. Invalid record data

        System.out.println("=== INVALID RECORD DATA ===");

        try {
            Customer.of(
                    "",
                    "Test",
                    "test@example.com"
            );
        } catch (IllegalArgumentException e) {
            System.out.println(
                    "Invalid Customer: " + e.getMessage()
            );
        }

        try {
            new RestaurantTable(
                    0,
                    4,
                    TableZone.VIP
            );
        } catch (IllegalArgumentException e) {
            System.out.println(
                    "Invalid RestaurantTable: " + e.getMessage()
            );
        }

        System.out.println();

        // 6. Two equal record objects

        System.out.println("=== RECORD EQUALS AND HASHCODE ===");

        Customer sameCustomer = Customer.of(
                " +380671234567 ",
                "sofia",
                "Sofia@EXAMPLE.COM"
        );
        System.out.println(
                "customer == sameCustomer: "
                        + (customer == sameCustomer)
        );

        System.out.println(
                "customer.equals(sameCustomer): "
                        + customer.equals(sameCustomer)
        );

        System.out.println(
                "customer.hashCode(): "
                        + customer.hashCode()
        );

        System.out.println(
                "sameCustomer.hashCode(): "
                        + sameCustomer.hashCode()
        );

        System.out.println();

        // 7. Generated record toString()

        System.out.println("=== RECORD TOSTRING ===");

        System.out.println(customer);
        System.out.println(table);

        System.out.println();

        // 8. Existing computed methods

        System.out.println("=== COMPUTED METHODS ===");

        System.out.println(
                "Reservation duration: "
                        + RestaurantUtils.durationMinutes(reservation)
                        + " minutes"
        );

        System.out.println(
                "Final order amount: "
                        + RestaurantUtils.formatMoney(
                                RestaurantUtils.finalAmount(order)
                        )
        );

        System.out.println();

        // 9. Mutable fields in regular classes

        System.out.println("=== MUTABLE FIELDS ===");

        reservation.setStatus(ReservationStatus.CONFIRMED);

        System.out.println(
                "Status after setter: "
                        + reservation.getStatus()
        );

        try {
            order.setDiscountAmount(2000.0);
        } catch (IllegalArgumentException e) {
            System.out.println(
                    "Invalid discount: " + e.getMessage()
            );
        }

        System.out.println();

        // 10. Loading rooms from CSV

        System.out.println("=== CSV ROOMS ===");

        RoomLoader loader = new RoomLoader();

        List<Room> rooms = loader.load(
                Path.of("data", "rooms.csv")
        );

        System.out.println(
                "Завантажено кімнат: " + rooms.size()
        );

        for (Room room : rooms) {
            System.out.println(
                    "Кімната " + room.roomNumber()
                            + ", тип: " + room.type()
                            + ", ціна: " + room.pricePerNight()
            );
        }

        System.out.println();

        // 11. Loading a missing file

        System.out.println("=== MISSING FILE ===");

        loader.load(
                Path.of("data", "missing-rooms.csv")
        );

        System.out.println(
                "Програму не завершено після спроби прочитати відсутній файл."
        );
    }
}