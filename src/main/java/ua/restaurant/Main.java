package ua.restaurant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.TableZone;
import ua.restaurant.repository.CustomerRepository;
import ua.restaurant.repository.RestaurantOrderRepository;
import ua.restaurant.repository.SortOrder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        log.info("Starting Lab 5 demonstration");

        CustomerRepository customerRepo = new CustomerRepository();
        RestaurantOrderRepository orderRepo = new RestaurantOrderRepository();

        Customer c1 = Customer.of("+380501112233", "Олена Коваль", "olena@gmail.com");
        Customer c2 = Customer.of("+380672223344", "Андрій Мельник", "andriy@ukr.net");
        Customer c3 = Customer.of("+380933334455", "Олена Коваль", "olena.work@gmail.com");
        Customer c4 = Customer.of("+380504445566", "Богдан Ткаченко", "bohdan@yahoo.com");

        customerRepo.addAll(List.of(c1, c2, c3, c4));

        RestaurantTable table1 = new RestaurantTable(1, 4, TableZone.MAIN_HALL);
        RestaurantTable table2 = new RestaurantTable(2, 2, TableZone.TERRACE);
        RestaurantTable table3 = new RestaurantTable(3, 6, TableZone.VIP);

        LocalDate date1 = LocalDate.of(2026, 5, 10);
        LocalDate date2 = LocalDate.of(2026, 5, 12);

        Reservation r1 = new Reservation(c1, table1, date1, LocalTime.of(18, 0), LocalTime.of(20, 0), 2, ReservationStatus.CONFIRMED);
        Reservation r2 = new Reservation(c2, table2, date2, LocalTime.of(19, 0), LocalTime.of(21, 0), 2, ReservationStatus.COMPLETED);
        Reservation r3 = new Reservation(c3, table3, date2, LocalTime.of(17, 0), LocalTime.of(19, 30), 4, ReservationStatus.CONFIRMED);
        Reservation r4 = new Reservation(c4, table1, date1, LocalTime.of(20, 30), LocalTime.of(22, 0), 3, ReservationStatus.CANCELLED);

        RestaurantOrder o1 = RestaurantOrder.of("ORD-102", r1, 850.50, 50.0);
        RestaurantOrder o2 = RestaurantOrder.of("ORD-101", r2, 1250.00, 100.0);
        RestaurantOrder o3 = RestaurantOrder.of("ORD-104", r3, 850.25, 0.0);
        RestaurantOrder o4 = RestaurantOrder.of("ORD-103", r4, 420.00, 20.0);

        orderRepo.addAll(List.of(o1, o2, o3, o4));

        System.out.println("=== 1. Природний порядок (Comparable) ===");
        List<RestaurantOrder> naturalOrders = new ArrayList<>(orderRepo.findAll());
        Collections.sort(naturalOrders);
        naturalOrders.forEach(o -> System.out.println(o.getOrderId() + " -> " + o.getTotalAmount() + " грн"));

        System.out.println("\n=== 2. Сортування за доменними критеріями ===");
        System.out.println("Клієнти (ім'я ASC):");
        customerRepo.sortByNameAsc()
                .forEach(c -> System.out.println("  " + c.name() + " | " + c.phone() + " | " + c.email()));

        System.out.println("Клієнти (ім'я ASC, email DESC):");
        customerRepo.sortByNameThenEmailDesc()
                .forEach(c -> System.out.println("  " + c.name() + " | " + c.email()));

        System.out.println("Замовлення (дата DESC, сума ASC):");
        orderRepo.sortByDateDescThenAmountAsc()
                .forEach(o -> System.out.println("  " + o.getOrderId() + " | " + o.getReservation().getReservationDate() + " | " + o.getTotalAmount() + " грн"));
                System.out.println("\n=== 3. Сортування за ключем (ASC / DESC) ===");
        System.out.println("Замовлення за ID (ASC): " + orderRepo.findAllSortedByIdentity(SortOrder.ASC).stream().map(RestaurantOrder::getOrderId).toList());
        System.out.println("Замовлення за ID (DESC): " + orderRepo.findAllSortedByIdentity(SortOrder.DESC).stream().map(RestaurantOrder::getOrderId).toList());

        System.out.println("\n=== 4. TreeSet із власним компаратором (не узгодженим з equals) ===");
        Set<Customer> customersByNameOnly = new TreeSet<>(Comparator.comparing(Customer::name));
        customersByNameOnly.add(c1);
        customersByNameOnly.add(c3);
        System.out.println("Додано 2 різних клієнти з однаковим іменем. Розмір TreeSet: " + customersByNameOnly.size());

        System.out.println("\n=== 5. Пошук через Stream API ===");
        List<Customer> foundByName = customerRepo.findByNameContaining("Олена");
        System.out.println("Пошук клієнтів за ім'ям 'Олена' (" + foundByName.size() + "):");
        foundByName.forEach(c -> System.out.println("  " + c.name() + " | " + c.phone() + " | " + c.email()));

        List<Customer> foundByPhoneRange = customerRepo.findByPhoneRange("+380501112233", "+380672223344");
        System.out.println("Клієнти в діапазоні телефонів [+380501112233, +380672223344]:");
        foundByPhoneRange.forEach(c -> System.out.println("  " + c.name() + " | " + c.phone()));

        List<Customer> foundByNameAndDomain = customerRepo.findByNameAndEmailDomain("Олена", "@gmail.com");
        System.out.println("Клієнти з ім'ям 'Олена' та доменом '@gmail.com':");
        foundByNameAndDomain.forEach(c -> System.out.println("  " + c.name() + " | " + c.email()));

        System.out.println("Замовлення клієнтів з ім'ям 'Олена': " + orderRepo.findByCustomerNameContaining("Олена").stream().map(RestaurantOrder::getOrderId).toList());
        System.out.println("Замовлення в діапазоні [850.25, 850.50]: " + orderRepo.findByTotalAmountRange(850.25, 850.50).stream().map(RestaurantOrder::getOrderId).toList());
        System.out.println("Підтверджені замовлення від 850 грн: " + orderRepo.findByStatusAndMinAmount(ReservationStatus.CONFIRMED, 850.0).stream().map(RestaurantOrder::getOrderId).toList());
        System.out.println("Топ унікальних доменів клієнтів: " + customerRepo.findTopUniqueEmailDomains(3));

        System.out.println("\n=== 6. Пошук одного об'єкта (Optional без isPresent + get) ===");
        Optional<Customer> foundGmail = customerRepo.findFirstByEmailDomain("@gmail.com");
        foundGmail.ifPresentOrElse(
                c -> System.out.println("Знайдено клієнта з gmail: " + c.name() + " (" + c.email() + ")"),
                () -> System.out.println("Клієнта з gmail не знайдено")
        );

        Optional<Customer> missingDomain = customerRepo.findFirstByEmailDomain("@nonexistent.org");
        String fallbackMessage = missingDomain
                .map(Customer::name)
                .orElse("Клієнта з таким доменом не існує");
        System.out.println("Результат пошуку неіснуючого домену: " + fallbackMessage);

        System.out.println("\n=== 7. Групування та агрегація ===");
        Map<ReservationStatus, List<RestaurantOrder>> byStatus = orderRepo.groupByReservationStatus();
        byStatus.forEach((status, list) -> System.out.println("  Статус " + status + ": " + list.size() + " замовлень"));

        Map<ReservationStatus, Long> countsByStatus = orderRepo.countByReservationStatus();
        System.out.println("Підрахунок по статусах: " + countsByStatus);
        System.out.println("Загальна виручка всіх замовлень: " + orderRepo.calculateTotalRevenue() + " грн");

        System.out.println("\n=== 8. Повторне використання спожитого стріму ===");
        Stream<Customer> customerStream = customerRepo.stream();
        long firstCount = customerStream.count();
        System.out.println("Перший прохід стріму: " + firstCount + " елементів");
        try {
            long secondCount = customerStream.count();
            System.out.println("Другий прохід стріму: " + secondCount);
        } catch (IllegalStateException ex) {
            log.error("Attempted to reuse an already consumed stream", ex);
            System.out.println("Перехоплено очікувану помилку при повторному використанні стріму: " + ex.getMessage());
        }

        log.info("Completed Lab 5 demonstration");
    }
}