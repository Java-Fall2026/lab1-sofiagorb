package ua.restaurant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.restaurant.loader.AppConfig;
import ua.restaurant.loader.DataLoader;
import ua.restaurant.loader.LoadResult;
import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.TableZone;
import ua.restaurant.repository.CustomerRepository;
import ua.restaurant.repository.RestaurantOrderRepository;
import ua.restaurant.storage.FileStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final String CONFIG_RESOURCE = "app.properties";

    public static void main(String[] args) {
        log.info("Starting Lab 6 demonstration: files, serialization, and validation");

        System.out.println("=== 1. Читання конфігурації з app.properties ===");
        AppConfig config = AppConfig.loadFromClasspath(CONFIG_RESOURCE);
        Path customersPath = config.getRequiredPath(AppConfig.KEY_CUSTOMERS_FILE);
        Path ordersPath = config.getRequiredPath(AppConfig.KEY_ORDERS_FILE);
        Path brokenPath = config.getRequiredPath(AppConfig.KEY_BROKEN_FILE);
        Path outputOrdersPath = config.getRequiredPath(AppConfig.KEY_OUTPUT_ORDERS_FILE);

        System.out.println("Файл клієнтів: " + customersPath);
        System.out.println("Файл замовлень: " + ordersPath);
        System.out.println("Пошкоджений файл: " + brokenPath);
        System.out.println("Вихідний файл для збереження: " + outputOrdersPath);

        CustomerRepository customerRepo = new CustomerRepository();
        RestaurantOrderRepository orderRepo = new RestaurantOrderRepository();

        FileStorage<Customer> customerStorage = new FileStorage<>(Customer.class);
        FileStorage<RestaurantOrder> orderStorage = new FileStorage<>(RestaurantOrder.class);

        DataLoader<Customer> customerLoader = new DataLoader<>(customerStorage);
        DataLoader<RestaurantOrder> orderLoader = new DataLoader<>(orderStorage);

        System.out.println("\n=== 2. Завантаження файлу клієнтів (JSON) ===");
        LoadResult customersResult = customerLoader.loadInto(customersPath, customerRepo);
        printLoadSummary(customersResult);

        System.out.println("\n=== 3. Завантаження файлу замовлень (YAML) ===");
        LoadResult ordersResult = orderLoader.loadInto(ordersPath, orderRepo);
        printLoadSummary(ordersResult);

        System.out.println("\n=== 4. Спроба завантажити пошкоджений файл broken.json ===");
        CustomerRepository tempBrokenRepo = new CustomerRepository();
        LoadResult brokenResult = customerLoader.loadInto(brokenPath, tempBrokenRepo);
        printLoadSummary(brokenResult);

        System.out.println("\n=== 5. Зміна вмісту репозиторію та збереження у новий YAML-файл ===");
        Customer newCustomer = Customer.of("+380509998877", "Вікторія Мельничук", "viktoriia.m@gmail.com");
        customerRepo.add(newCustomer);

        RestaurantTable vipTable = new RestaurantTable(15, 6, TableZone.VIP);
        Reservation newReservation = new Reservation(
                newCustomer,
                vipTable,
                LocalDate.of(2026, 12, 1),
                LocalTime.of(19, 0),
                LocalTime.of(21, 30),
                4,
                ReservationStatus.CONFIRMED
        );
        RestaurantOrder newOrder = RestaurantOrder.of("ORD-999", newReservation, 2100.00, 150.00);
        orderRepo.add(newOrder);
        orderRepo.removeByIdentity("ORD-109");

        System.out.println("Кількість замовлень у репозиторії після змін: " + orderRepo.size());
        try {
            orderStorage.save(outputOrdersPath, orderRepo.findAll());
            System.out.println("Успішно збережено оновлений список замовлень у: " + outputOrdersPath);
        } catch (IOException e) {
            log.error("Failed to save updated orders to '{}'", outputOrdersPath, e);
            throw new IllegalStateException("Cannot save orders file", e);
        }

        System.out.println("\n=== 6. Повторне завантаження збереженого файлу та перевірка збігу ===");
        RestaurantOrderRepository reloadedRepo = new RestaurantOrderRepository();
        LoadResult reloadResult = orderLoader.loadInto(outputOrdersPath, reloadedRepo);
        printLoadSummary(reloadResult);

        List<RestaurantOrder> originalOrders = orderRepo.findAll();
        List<RestaurantOrder> reloadedOrders = reloadedRepo.findAll();
        boolean matches = originalOrders.equals(reloadedOrders);
        System.out.println("Вміст оригінального та повторно завантаженого репозиторіїв збігається: " + matches);

        log.info("Completed Lab 6 demonstration");
    }

    private static void printLoadSummary(LoadResult result) {
        System.out.printf("Джерело: %s | Прочитано: %d | Додано: %d | Відкинуто: %d%n",
                result.source(), result.totalRead(), result.addedCount(), result.rejectedCount());
        if (!result.rejected().isEmpty()) {
            System.out.println("Відкинуті записи:");
            for (LoadResult.RejectedRecord rejected : result.rejected()) {
                System.out.println("  - " + rejected);
            }
        }
    }
}