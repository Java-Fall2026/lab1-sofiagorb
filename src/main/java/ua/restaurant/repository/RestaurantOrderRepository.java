package ua.restaurant.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RestaurantOrderRepository extends GenericRepository<RestaurantOrder> {

    private static final Logger log = LoggerFactory.getLogger(RestaurantOrderRepository.class);

    public RestaurantOrderRepository() {
        super("orders", RestaurantOrder::getOrderId);
    }

    public List<RestaurantOrder> sortByTotalAmountDesc() {
        Comparator<RestaurantOrder> comparator = Comparator
                .comparingDouble(RestaurantOrder::getTotalAmount)
                .reversed();
        List<RestaurantOrder> result = findAllSorted(comparator);
        log.info("Sorted {} orders by total amount descending", result.size());
        return result;
    }

    public List<RestaurantOrder> sortByDateDescThenAmountAsc() {
        Comparator<RestaurantOrder> comparator = Comparator
                .comparing((RestaurantOrder order) -> order.getReservation().getReservationDate())
                .reversed()
                .thenComparingDouble(RestaurantOrder::getTotalAmount);
        List<RestaurantOrder> result = findAllSorted(comparator);
        log.info("Sorted {} orders by reservation date descending then amount ascending", result.size());
        return result;
    }

    public List<RestaurantOrder> findByCustomerNameContaining(String namePart) {
        if (namePart == null || namePart.isBlank()) {
            log.error("Customer name query must not be null or blank in repository '{}'", getName());
            throw new IllegalArgumentException("Customer name query must not be null or blank");
        }
        String normalized = namePart.trim().toLowerCase();
        List<RestaurantOrder> result = stream()
                .filter(order -> order.getReservation().getCustomer().name().toLowerCase().contains(normalized))
                .toList();
        logSearchResult("customer name containing '" + namePart + "'", result.size());
        return result;
    }

    public List<RestaurantOrder> findByTotalAmountRange(double minAmount, double maxAmount) {
        if (minAmount > maxAmount) {
            log.error("Invalid amount range [{}, {}] in repository '{}'", minAmount, maxAmount, getName());
            throw new IllegalArgumentException("minAmount must not exceed maxAmount");
        }
        List<RestaurantOrder> result = stream()
                .filter(order -> order.getTotalAmount() >= minAmount && order.getTotalAmount() <= maxAmount)
                .sorted()
                .toList();
        logSearchResult("total amount range [" + minAmount + ", " + maxAmount + "]", result.size());
        return result;
    }

    public List<RestaurantOrder> findByStatusAndMinAmount(ReservationStatus status, double minAmount) {
        if (status == null) {
            log.error("ReservationStatus must not be null in repository '{}'", getName());
            throw new IllegalArgumentException("Status must not be null");
        }
        List<RestaurantOrder> result = stream()
                .filter(order -> order.getReservation().getStatus() == status
                        && order.getTotalAmount() >= minAmount)
                .toList();
        logSearchResult("status " + status + " and min amount " + minAmount, result.size());
        return result;
    }

    public Optional<RestaurantOrder> findFirstByCustomerPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            log.error("Customer phone must not be null or blank in repository '{}'", getName());
            throw new IllegalArgumentException("Phone must not be null or blank");
            }
        String normalized = phone.trim();
        Optional<RestaurantOrder> found = stream()
                .filter(order -> order.getReservation().getCustomer().phone().equals(normalized))
                .findFirst();
        logSearchResult("first order for phone '" + phone + "'", found.isPresent() ? 1 : 0);
        return found;
    }

    public List<String> findTopCustomerPhonesByMinAmount(double minAmount, int limit) {
        if (limit <= 0) {
            log.error("Limit must be positive, got {} in repository '{}'", limit, getName());
            throw new IllegalArgumentException("Limit must be positive");
        }
        List<String> phones = stream()
                .filter(order -> order.getTotalAmount() >= minAmount)
                .sorted(Comparator.comparingDouble(RestaurantOrder::getTotalAmount).reversed())
                .map(order -> order.getReservation().getCustomer().phone())
                .distinct()
                .limit(limit)
                .toList();
        logSearchResult("top " + limit + " customer phones with order >= " + minAmount, phones.size());
        return phones;
    }

    public Map<ReservationStatus, List<RestaurantOrder>> groupByReservationStatus() {
        Map<ReservationStatus, List<RestaurantOrder>> grouped = stream()
                .collect(Collectors.groupingBy(order -> order.getReservation().getStatus()));
        log.info("Grouped orders into {} status categories", grouped.size());
        return grouped;
    }

    public Map<ReservationStatus, Long> countByReservationStatus() {
        Map<ReservationStatus, Long> counts = stream()
                .collect(Collectors.groupingBy(
                        order -> order.getReservation().getStatus(),
                        Collectors.counting()
                ));
        log.info("Counted orders across {} status categories", counts.size());
        return counts;
    }

    public double calculateTotalRevenue() {
        double total = stream()
                .mapToDouble(RestaurantOrder::getTotalAmount)
                .sum();
        log.info("Calculated total revenue across {} orders: {}", size(), total);
        return total;
    }

    private void logSearchResult(String criteria, int count) {
        if (count == 0) {
            log.warn("No orders found matching criteria: {}", criteria);
        } else {
            log.info("Found {} order(s) matching criteria: {}", count, criteria);
        }
    }
}