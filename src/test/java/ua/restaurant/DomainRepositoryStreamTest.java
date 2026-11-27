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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainRepositoryStreamTest {

    private CustomerRepository customerRepo;
    private RestaurantOrderRepository orderRepo;

    private Customer c1;
    private Customer c2;
    private Customer c3;

    private RestaurantOrder o1;
    private RestaurantOrder o2;
    private RestaurantOrder o3;

    @BeforeEach
    void setUp() {
        customerRepo = new CustomerRepository();
        orderRepo = new RestaurantOrderRepository();

        c1 = Customer.of("+380501112233", "Olena Koval", "olena@gmail.com");
        c2 = Customer.of("+380672223344", "Andriy Melnyk", "andriy@ukr.net");
        c3 = Customer.of("+380933334455", "Olena Koval", "olena.work@gmail.com");

        customerRepo.addAll(List.of(c1, c2, c3));

        RestaurantTable table1 = new RestaurantTable(1, 4, TableZone.MAIN_HALL);
        RestaurantTable table2 = new RestaurantTable(2, 2, TableZone.TERRACE);

        LocalDate date1 = LocalDate.of(2026, 5, 10);
        LocalDate date2 = LocalDate.of(2026, 5, 12);

        Reservation r1 = new Reservation(c1, table1, date1, LocalTime.of(18, 0), LocalTime.of(20, 0), 2, ReservationStatus.CONFIRMED);
        Reservation r2 = new Reservation(c2, table2, date2, LocalTime.of(19, 0), LocalTime.of(21, 0), 2, ReservationStatus.COMPLETED);
        Reservation r3 = new Reservation(c3, table1, date2, LocalTime.of(17, 0), LocalTime.of(19, 0), 4, ReservationStatus.CONFIRMED);

        o1 = RestaurantOrder.of("ORD-102", r1, 850.50, 50.0);
        o2 = RestaurantOrder.of("ORD-101", r2, 1250.00, 100.0);
        o3 = RestaurantOrder.of("ORD-103", r3, 850.25, 0.0);

        orderRepo.addAll(List.of(o1, o2, o3));
    }

    @Test
    @DisplayName("Natural ordering sorts customers by phone ascending")
    void naturalOrderingSortsCustomersByPhone() {
        List<Customer> list = new ArrayList<>(List.of(c3, c1, c2));
        Collections.sort(list);
        assertEquals(List.of(c1, c2, c3), list);
    }

    @Test
    @DisplayName("Fractional prices differing by less than one unit are ordered accurately")
    void fractionalPricesDifferingByLessThanOneAreSortedCorrectly() {
        List<RestaurantOrder> list = new ArrayList<>(List.of(o2, o1, o3));
        Collections.sort(list);
        assertEquals(List.of(o3, o1, o2), list);
    }

    @Test
    @DisplayName("Multi-level comparator sorts equal primary keys by secondary key descending")
    void multiLevelComparatorSortsSecondaryKeyDescending() {
        List<Customer> sorted = customerRepo.sortByNameThenEmailDesc();
        assertEquals(List.of(c2, c1, c3), sorted);
    }

    @Test
    @DisplayName("findAllSortedByIdentity in ASC and DESC return mutually reversed lists")
    void findAllSortedByIdentityAscAndDescAreReversed() {
        List<RestaurantOrder> asc = orderRepo.findAllSortedByIdentity(SortOrder.ASC);
        List<RestaurantOrder> desc = orderRepo.findAllSortedByIdentity(SortOrder.DESC);

        assertEquals(List.of(o2, o1, o3), asc);
        assertEquals(asc.reversed(), desc);
    }
    @Test
    @DisplayName("TreeSet with comparator inconsistent with equals treats equal-key elements as duplicates")
    void treeSetWithInconsistentComparatorDeduplicatesByComparator() {
        Set<Customer> byNameOnly = new TreeSet<>(Comparator.comparing(Customer::name));
        byNameOnly.add(c1);
        byNameOnly.add(c3);

        assertEquals(1, byNameOnly.size());
        assertTrue(byNameOnly.contains(c1));
    }

    @Test
    @DisplayName("Text search finds matching items case-insensitively")
    void textSearchFindsMatchingItems() {
        List<Customer> found = customerRepo.findByNameContaining("olena");
        assertEquals(2, found.size());
        assertTrue(found.containsAll(List.of(c1, c3)));
    }

    @Test
    @DisplayName("Order amount range search includes exact boundary values and excludes outside values")
    void rangeSearchIncludesExactBoundariesAndExcludesOutsideValues() {
        List<RestaurantOrder> exactBoundaries = orderRepo.findByTotalAmountRange(850.25, 850.50);
        assertEquals(List.of(o3, o1), exactBoundaries);
    }

    @Test
    @DisplayName("Customer phone range search includes exact boundaries and excludes outside values")
    void customerPhoneRangeSearchIncludesExactBoundaries() {
        List<Customer> inRange = customerRepo.findByPhoneRange("+380501112233", "+380672223344");
        assertEquals(List.of(c1, c2), inRange);
    }

    @Test
    @DisplayName("Combined condition search excludes items matching only one condition")
    void combinedSearchRequiresBothConditions() {
        List<RestaurantOrder> matched = orderRepo.findByStatusAndMinAmount(ReservationStatus.CONFIRMED, 850.40);
        assertEquals(List.of(o1), matched);
    }

    @Test
    @DisplayName("Customer combined search by name and email domain requires both conditions")
    void customerCombinedSearchByNameAndEmailDomainRequiresBothConditions() {
        List<Customer> matched = customerRepo.findByNameAndEmailDomain("olena", "@gmail.com");
        assertEquals(List.of(c1, c3), matched);

        List<Customer> noMatch = customerRepo.findByNameAndEmailDomain("olena", "@ukr.net");
        assertTrue(noMatch.isEmpty());
    }

    @Test
    @DisplayName("findTopUniqueEmailDomains returns sorted distinct domains limited to N")
    void findTopUniqueEmailDomainsReturnsSortedDistinctDomains() {
        List<String> allUnique = customerRepo.findTopUniqueEmailDomains(5);
        assertEquals(List.of("gmail.com", "ukr.net"), allUnique);

        List<String> topOne = customerRepo.findTopUniqueEmailDomains(1);
        assertEquals(List.of("gmail.com"), topOne);
    }

    @Test
    @DisplayName("Single item search returns Optional.empty when no matches exist")
    void singleItemSearchReturnsEmptyOptionalWhenNotFound() {
        Optional<Customer> result = customerRepo.findFirstByEmailDomain("@unknown.org");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("groupingBy produces expected group count and group sizes")
    void groupingByProducesExpectedGroupsAndCounts() {
        Map<ReservationStatus, List<RestaurantOrder>> grouped = orderRepo.groupByReservationStatus();
        Map<ReservationStatus, Long> counts = orderRepo.countByReservationStatus();

        assertEquals(2, grouped.size());
        assertEquals(2, grouped.get(ReservationStatus.CONFIRMED).size());
        assertEquals(1L, counts.get(ReservationStatus.COMPLETED));
    }

    @Test
    @DisplayName("Aggregation on empty repository returns zero without throwing")
    void aggregationOnEmptyRepositoryReturnsZero() {
        RestaurantOrderRepository emptyRepo = new RestaurantOrderRepository();
        assertEquals(0.0, emptyRepo.calculateTotalRevenue(), 0.0001);
    }

    @Test
    @DisplayName("Reusing an already consumed stream throws IllegalStateException")
    void reusingConsumedStreamThrowsIllegalStateException() {
        Stream<Customer> stream = customerRepo.stream();
        assertEquals(3L, stream.count());
        assertThrows(IllegalStateException.class, stream::count);
    }
}