package ua.restaurant;

import ua.restaurant.exception.DuplicateIdentityException;
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

public class Main {

    public static void main(String[] args) {

        // 1. Creating objects using record and enum
        Customer customer = Customer.of(
                " +380671234567 ",
                "sofia",
                "Sofia@Example.com"
        );

        RestaurantTable table = new RestaurantTable(
                15,
                4,
                TableZone.TERRACE
        );

        Reservation reservation = new Reservation(
                customer,
                table,
                LocalDate.now(),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                4,
                ReservationStatus.PENDING
        );

        RestaurantOrder order = RestaurantOrder.of(
                "ORD-501",
                reservation,
                1250.0,
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
        System.out.println("Завантажено рядків столиків із CSV: " + loadedTables.size());
        System.out.println();

        // 11. Loading a missing file (Lab 3)
        System.out.println("=== MISSING FILE ===");
        loader.load(Path.of("data", "missing-tables.csv"));
        System.out.println("Програму не завершено після спроби прочитати відсутній файл.");
        System.out.println();

        // 12. GenericRepository demonstration (Lab 4)
        System.out.println("=== РЕПОЗИТОРІЙ 1: КЛІЄНТИ (Customer) ===");
        GenericRepository<Customer> customerRepo = new GenericRepository<>("Customer", Customer::phone);
        Customer customer2 = Customer.of("+380509876543", "Andrii", "andrii@example.com");

        customerRepo.add(customer);
        customerRepo.add(customer2);

        // Пошук: знайдено та не знайдено
        System.out.println("Пошук наявного клієнта: " + customerRepo.findByIdentity("+380671234567").isPresent());
        System.out.println("Пошук відсутнього клієнта: " + customerRepo.findByIdentity("+380000000000").isPresent());

        // Дублікат через add() у try/catch поруч із addIfAbsent()
        Customer duplicateCustomer = Customer.of("+380671234567", "Інша Софія", "other@example.com");
        try {
            customerRepo.add(duplicateCustomer);
        } catch (DuplicateIdentityException e) {
            System.out.println("Спіймано виключення при add(): " + e.getMessage());
        }
        boolean addedIfAbsent = customerRepo.addIfAbsent(duplicateCustomer);
        System.out.println("Результат addIfAbsent() для дубліката: " + addedIfAbsent);

        // Видалення наявного та відсутнього елемента
        System.out.println("Видалення наявного клієнта: " + customerRepo.removeByIdentity("+380509876543"));
        System.out.println("Видалення відсутнього клієнта: " + customerRepo.removeByIdentity("+380509876543"));
        System.out.println("Усього клієнтів у репозиторії: " + customerRepo.findAll().size());
        System.out.println();

        System.out.println("=== РЕПОЗИТОРІЙ 2: СТОЛИКИ (RestaurantTable + CSV) ===");
        GenericRepository<RestaurantTable> tableRepo =
                new GenericRepository<>("RestaurantTable", t -> String.valueOf(t.tableNumber()));

        // Додавання з CSV (де є дублікат столика №1)
        for (RestaurantTable t : loadedTables) {
            try {
                tableRepo.add(t);
            } catch (DuplicateIdentityException e) {
                System.out.println("Дублікат із CSV при add(): " + e.getMessage());
            }
        }
        tableRepo.addIfAbsent(table);
        // Пошук: знайдено та не знайдено
        System.out.println("Пошук столика №15: " + tableRepo.findByIdentity("15").isPresent());
        System.out.println("Пошук столика №999: " + tableRepo.findByIdentity("999").isPresent());

        // Видалення наявного та відсутнього елемента
        System.out.println("Видалення столика №2: " + tableRepo.removeByIdentity("2"));
        System.out.println("Повторне видалення столика №2: " + tableRepo.removeByIdentity("2"));
        System.out.println();

        System.out.println("=== РЕПОЗИТОРІЙ 3: ЗАМОВЛЕННЯ (RestaurantOrder) ===");
        GenericRepository<RestaurantOrder> orderRepo =
                new GenericRepository<>("RestaurantOrder", RestaurantOrder::getOrderId);

        orderRepo.add(order);
        RestaurantOrder order2 = RestaurantOrder.of("ORD-502", reservation, 800.0, 0.0);
        orderRepo.addAll(List.of(order2));

        // Пошук: знайдено та не знайдено
        System.out.println("Пошук замовлення ORD-501: " + orderRepo.findByIdentity("ORD-501").isPresent());
        System.out.println("Пошук замовлення ORD-999: " + orderRepo.findByIdentity("ORD-999").isPresent());

        // Дублікат: add() vs addIfAbsent()
        try {
            orderRepo.add(order);
        } catch (DuplicateIdentityException e) {
            System.out.println("Спіймано виключення при add(): " + e.getMessage());
        }
        System.out.println("Результат addIfAbsent() для дубліката замовлення: " + orderRepo.addIfAbsent(order));

        // Видалення наявного та відсутнього елемента
        System.out.println("Видалення ORD-502: " + orderRepo.removeByIdentity("ORD-502"));
        System.out.println("Видалення відсутнього ORD-777: " + orderRepo.removeByIdentity("ORD-777"));
    }
}