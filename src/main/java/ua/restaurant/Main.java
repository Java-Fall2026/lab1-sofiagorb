package ua.restaurant;

import ua.restaurant.loader.TableLoader;
import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.TableZone;
import ua.restaurant.repository.GenericRepository;
import ua.restaurant.util.RestaurantUtils;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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

        System.out.println("Terrace price coefficient: " + TableZone.TERRACE.getPriceCoefficient());

        try {
            ReservationStatus.valueOf("UNKNOWN");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid enum value: " + e.getMessage());
        }
        System.out.println();

        // 4. Switch expressions
        System.out.println("=== SWITCH EXPRESSIONS ===");
        System.out.println("Zone description: " + RestaurantUtils.zoneDescription(table.zone()));
        System.out.println("Available action: " + RestaurantUtils.availableAction(reservation.getStatus()));
        System.out.println();

        // 5. Invalid record data
        System.out.println("=== INVALID RECORD DATA ===");
        try {
            Customer.of("", "Test", "test@example.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid Customer: " + e.getMessage());
        }

        try {
            new RestaurantTable(0, 4, TableZone.VIP);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid RestaurantTable: " + e.getMessage());
        }
        System.out.println();

        // 6. Two equal record objects
        System.out.println("=== RECORD EQUALS AND HASHCODE ===");
        Customer sameCustomer = Customer.of(" +380671234567 ", "sofia", "Sofia@EXAMPLE.COM");
        System.out.println("customer == sameCustomer: " + (customer == sameCustomer));
        System.out.println("customer.equals(sameCustomer): " + customer.equals(sameCustomer));
        System.out.println("customer.hashCode(): " + customer.hashCode());
        System.out.println("sameCustomer.hashCode(): " + sameCustomer.hashCode());
        System.out.println();
        // 7. Generated record toString()
        System.out.println("=== RECORD TOSTRING ===");
        System.out.println(customer);
        System.out.println(table);
        System.out.println();

        // 8. Existing computed methods
        System.out.println("=== COMPUTED METHODS ===");
        System.out.println("Reservation duration: " + RestaurantUtils.durationMinutes(reservation) + " minutes");
        System.out.println("Final order amount: " + RestaurantUtils.formatMoney(RestaurantUtils.finalAmount(order)));
        System.out.println();

        // 9. Mutable fields in regular classes
        System.out.println("=== MUTABLE FIELDS ===");
        reservation.setStatus(ReservationStatus.CONFIRMED);
        System.out.println("Status after setter: " + reservation.getStatus());

        try {
            order.setDiscountAmount(2000.0);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid discount: " + e.getMessage());
        }
        System.out.println();

        // 10. Loading tables from CSV (Lab 3)
        System.out.println("=== CSV TABLES ===");
        TableLoader loader = new TableLoader();
        List<RestaurantTable> loadedTables = loader.load(Path.of("data", "tables.csv"));
        System.out.println("Завантажено столиків: " + loadedTables.size());
        for (RestaurantTable t : loadedTables) {
            System.out.println("Столик №" + t.tableNumber() + ", місць: " + t.capacity() + ", зона: " + t.zone());
        }
        System.out.println();

        // 11. Loading a missing file (Lab 3)
        System.out.println("=== MISSING FILE ===");
        loader.load(Path.of("data", "missing-tables.csv"));
        System.out.println("Програму не завершено після спроби прочитати відсутній файл.");
        System.out.println();

        // 12. GenericRepository demonstration (Lab 4)
        System.out.println("=== GENERIC REPOSITORY (КЛІЄНТИ) ===");
        GenericRepository<Customer, String> customerRepo =
                new GenericRepository<>(Customer::phone, "Customer");

        Customer customer2 = Customer.of(" +380509876543 ", "andrii", "andrii@example.com");
        customerRepo.add(customer);
        customerRepo.add(customer2);

        Optional<Customer> foundCustomer = customerRepo.findByIdentity("+380671234567");
        foundCustomer.ifPresent(c -> System.out.println("Знайдено клієнта: " + c.name()));

        Customer duplicateCustomer = Customer.of("+380671234567", "Інша Софія", "other@example.com");
        System.out.println("Чи додано дублікат клієнта: " + customerRepo.add(duplicateCustomer));
        System.out.println("Усього клієнтів у репозиторії: " + customerRepo.size());
        System.out.println();

        System.out.println("=== GENERIC REPOSITORY (СТОЛИКИ ТА ЗАМОВЛЕННЯ) ===");
        GenericRepository<RestaurantTable, Integer> tableRepo =
                new GenericRepository<>(RestaurantTable::tableNumber, "RestaurantTable");
        for (RestaurantTable t : loadedTables) {
            tableRepo.add(t);
        }
        tableRepo.add(table);

        RestaurantTable duplicateTable = new RestaurantTable(15, 6, TableZone.VIP);
        System.out.println("Чи додано дублікат столика №15: " + tableRepo.add(duplicateTable));
        tableRepo.findByIdentity(15).ifPresent(t ->
                System.out.println("Знайдено столик №15: зона " + t.zone() + ", місць " + t.capacity())
        );

        GenericRepository<RestaurantOrder, String> orderRepo =
                new GenericRepository<>(RestaurantOrder::getOrderId, "RestaurantOrder");
        orderRepo.add(order);
        orderRepo.findByIdentity("ORD-501").ifPresent(o ->
                System.out.println("Знайдено замовлення: " + o.getOrderId())
        );
    }
}