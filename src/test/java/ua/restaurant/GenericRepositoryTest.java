package ua.restaurant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ua.restaurant.exception.DuplicateIdentityException;
import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.TableZone;
import ua.restaurant.repository.GenericRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GenericRepository and Collections tests")
class GenericRepositoryTest {

    private GenericRepository<Customer> customerRepo;
    private GenericRepository<RestaurantTable> tableRepo;

    @BeforeEach
    void setUp() {
        customerRepo = new GenericRepository<>("Customer", Customer::phone);
        tableRepo = new GenericRepository<>("RestaurantTable", t -> String.valueOf(t.tableNumber()));
    }

    @Test
    @DisplayName("Should add and retrieve entities of different types")
    void shouldAddAndRetrieveEntities() {
        Customer customer = Customer.of("+380671112233", "Olena", "olena@test.com");
        RestaurantTable table = new RestaurantTable(5, 4, TableZone.MAIN_HALL);

        customerRepo.add(customer);
        tableRepo.add(table);

        assertEquals(1, customerRepo.size());
        assertEquals(1, tableRepo.size());
        assertEquals(customer, customerRepo.findAll().get(0));
        assertEquals(table, tableRepo.findAll().get(0));
    }

    @Test
    @DisplayName("Should throw DuplicateIdentityException on add() with duplicate identity")
    void shouldThrowOnDuplicateAdd() {
        Customer first = Customer.of("+380671112233", "Olena", "olena@test.com");
        Customer duplicate = Customer.of("+380671112233", "Iryna", "iryna@test.com");

        customerRepo.add(first);

        DuplicateIdentityException exception = assertThrows(
                DuplicateIdentityException.class,
                () -> customerRepo.add(duplicate)
        );
        assertTrue(exception.getMessage().contains("+380671112233"));
        assertTrue(exception.getMessage().contains("Customer"));
        assertEquals(1, customerRepo.size());
    }

    @Test
    @DisplayName("Should return false and not change size on addIfAbsent() with duplicate")
    void shouldReturnFalseOnDuplicateAddIfAbsent() {
        Customer first = Customer.of("+380671112233", "Olena", "olena@test.com");
        Customer duplicate = Customer.of("+380671112233", "Iryna", "iryna@test.com");

        assertTrue(customerRepo.addIfAbsent(first));
        assertFalse(customerRepo.addIfAbsent(duplicate));
        assertEquals(1, customerRepo.size());
        assertEquals("Olena", customerRepo.findByIdentity("+380671112233").orElseThrow().name());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when adding null item or blank identity")
    void shouldThrowIllegalArgumentExceptionForNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> customerRepo.add(null));
        assertThrows(IllegalArgumentException.class, () -> customerRepo.addIfAbsent(null));

        GenericRepository<String> badRepo = new GenericRepository<>("BadRepo", s -> "   ");
        assertThrows(IllegalArgumentException.class, () -> badRepo.add("value"));
    }

    @Test
    @DisplayName("Should add all elements from collection via addAll()")
    void shouldAddAllFromCollection() {
        RestaurantTable t1 = new RestaurantTable(1, 2, TableZone.BAR);
        RestaurantTable t2 = new RestaurantTable(2, 4, TableZone.TERRACE);

        tableRepo.addAll(List.of(t1, t2));

        assertEquals(2, tableRepo.size());
    }

    @Test
    @DisplayName("Should find entity by identity and return empty Optional when missing")
    void shouldFindByIdentity() {
        RestaurantTable table = new RestaurantTable(12, 6, TableZone.VIP);
        tableRepo.add(table);
        Optional<RestaurantTable> found = tableRepo.findByIdentity("12");
        Optional<RestaurantTable> notFound = tableRepo.findByIdentity("99");

        assertTrue(found.isPresent());
        assertEquals(TableZone.VIP, found.get().zone());
        assertTrue(notFound.isEmpty());
    }

    @Test
    @DisplayName("Should remove existing entity and return false when removing non-existent entity")
    void shouldRemoveExistingAndHandleMissing() {
        RestaurantTable table1 = new RestaurantTable(1, 2, TableZone.BAR);
        RestaurantTable table2 = new RestaurantTable(2, 4, TableZone.TERRACE);
        tableRepo.add(table1);
        tableRepo.add(table2);

        assertTrue(tableRepo.removeByIdentity("1"));
        assertFalse(tableRepo.removeByIdentity("1"));
        assertFalse(tableRepo.removeByIdentity("999"));
        assertEquals(1, tableRepo.size());

        assertTrue(tableRepo.remove(table2));
        assertEquals(0, tableRepo.size());
    }

    @Test
    @DisplayName("Should return unmodifiable collection from findAll()")
    void shouldReturnUnmodifiableList() {
        Customer customer = Customer.of("+380671112233", "Olena", "olena@test.com");
        customerRepo.add(customer);

        List<Customer> all = customerRepo.findAll();

        assertThrows(UnsupportedOperationException.class, () -> all.add(customer));
    }

    @Test
    @DisplayName("Should work properly with RestaurantOrder entities")
    void shouldWorkWithRestaurantOrders() {
        Customer customer = Customer.of("+380671112233", "Olena", "olena@test.com");
        RestaurantTable table = new RestaurantTable(3, 4, TableZone.MAIN_HALL);
        Reservation reservation = new Reservation(
                customer, table, LocalDate.now(), LocalTime.of(14, 0), LocalTime.of(16, 0), 2, ReservationStatus.CONFIRMED
        );
        RestaurantOrder order = RestaurantOrder.of("ORD-100", reservation, 500.0, 50.0);

        GenericRepository<RestaurantOrder> orderRepo =
                new GenericRepository<>("RestaurantOrder", RestaurantOrder::getOrderId);

        orderRepo.add(order);
        assertTrue(orderRepo.findByIdentity("ORD-100").isPresent());
    }
}