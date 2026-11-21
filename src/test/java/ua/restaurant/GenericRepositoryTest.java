package ua.restaurant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    private GenericRepository<Customer, String> customerRepo;
    private GenericRepository<RestaurantTable, Integer> tableRepo;

    @BeforeEach
    void setUp() {
        customerRepo = new GenericRepository<>(Customer::phone, "Customer");
        tableRepo = new GenericRepository<>(RestaurantTable::tableNumber, "RestaurantTable");
    }

    @Test
    @DisplayName("Should add and retrieve entities of different types")
    void shouldAddAndRetrieveEntities() {
        Customer customer = Customer.of("+380671112233", "Olena", "olena@test.com");
        RestaurantTable table = new RestaurantTable(5, 4, TableZone.MAIN_HALL);

        assertTrue(customerRepo.add(customer));
        assertTrue(tableRepo.add(table));

        assertEquals(1, customerRepo.size());
        assertEquals(1, tableRepo.size());
        assertEquals(customer, customerRepo.getAll().get(0));
        assertEquals(table, tableRepo.getAll().get(0));
    }

    @Test
    @DisplayName("Should reject duplicate entities with the same identity")
    void shouldRejectDuplicates() {
        Customer first = Customer.of("+380671112233", "Olena", "olena@test.com");
        Customer duplicate = Customer.of("+380671112233", "Iryna", "iryna@test.com");

        assertTrue(customerRepo.add(first));
        assertFalse(customerRepo.add(duplicate));
        assertEquals(1, customerRepo.size());
        assertEquals("Olena", customerRepo.findByIdentity("+380671112233").orElseThrow().name());
    }

    @Test
    @DisplayName("Should find entity by identity and return empty Optional when missing")
    void shouldFindByIdentity() {
        RestaurantTable table = new RestaurantTable(12, 6, TableZone.VIP);
        tableRepo.add(table);

        Optional<RestaurantTable> found = tableRepo.findByIdentity(12);
        Optional<RestaurantTable> notFound = tableRepo.findByIdentity(99);

        assertTrue(found.isPresent());
        assertEquals(TableZone.VIP, found.get().zone());
        assertTrue(notFound.isEmpty());
    }

    @Test
    @DisplayName("Should remove entity by identity or object reference")
    void shouldRemoveEntity() {
        RestaurantTable table1 = new RestaurantTable(1, 2, TableZone.BAR);
        RestaurantTable table2 = new RestaurantTable(2, 4, TableZone.TERRACE);
        tableRepo.add(table1);
        tableRepo.add(table2);

        assertTrue(tableRepo.removeByIdentity(1));
        assertFalse(tableRepo.removeByIdentity(1));
        assertEquals(1, tableRepo.size());

        assertTrue(tableRepo.remove(table2));
        assertEquals(0, tableRepo.size());
    }

    @Test
    @DisplayName("Should return unmodifiable collection from getAll()")
    void shouldReturnUnmodifiableList() {
        Customer customer = Customer.of("+380671112233", "Olena", "olena@test.com");
        customerRepo.add(customer);

        List<Customer> all = customerRepo.getAll();

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

        GenericRepository<RestaurantOrder, String> orderRepo =
                new GenericRepository<>(RestaurantOrder::getOrderId, "RestaurantOrder");

        assertTrue(orderRepo.add(order));
        assertTrue(orderRepo.findByIdentity("ORD-100").isPresent());
    }
}