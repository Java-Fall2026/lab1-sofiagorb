package ua.restaurant;

import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.util.RestaurantUtils;

import java.time.LocalDate;
import java.time.LocalTime;

public class Main {

    public static void main(String[] args) {

        // 1. Creating objects using a factory method and a constructor

        Customer customer = Customer.of(
                " +380671234567 ",
                "sofia",
                "sofia@EXAMPLE.COM"
        );

        RestaurantTable table = new RestaurantTable(
                15,
                4,
                " terrace "
        );

        Reservation reservation = new Reservation(
                customer,
                table,
                LocalDate.of(2026, 9, 25),
                LocalTime.of(18, 0),
                LocalTime.of(20, 30),
                3,
                " pending "
        );

        RestaurantOrder order = RestaurantOrder.of(
                "ORD-501",
                reservation,
                1200.0,
                150.0
        );

        // 2. Normalization

        System.out.println("=== NORMALIZATION ===");
        System.out.println("Customer name: " + customer.getName());
        System.out.println("Customer email: " + customer.getEmail());
        System.out.println("Table zone: " + table.getZone());
        System.out.println("Reservation status: " + reservation.getStatus());
        System.out.println();

        // 3. Invalid cases

        System.out.println("=== INVALID CASES ===");

        try {
            Customer.of("", "Test", "test@example.com");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid phone: " + e.getMessage());
        }

        try {
            new RestaurantTable(0, 4, "VIP");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid table: " + e.getMessage());
        }

        try {
            new Reservation(
                    customer,
                    table,
                    LocalDate.of(2026, 9, 25),
                    LocalTime.of(20, 0),
                    LocalTime.of(19, 0),
                    2,
                    "CONFIRMED"
            );
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid time: " + e.getMessage());
        }

        // 4. Setter validation

        System.out.println();
        System.out.println("=== SETTER VALIDATION ===");

        reservation.setStatus(" confirmed ");

        System.out.println(
                "Status after valid setter: "
                        + reservation.getStatus()
        );

        try {
            reservation.setStatus("UNKNOWN");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status: " + e.getMessage());
        }

        try {
            order.setDiscountAmount(2000.0);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid discount: " + e.getMessage());
        }

        // 5. == versus equals()

        System.out.println();
        System.out.println("=== == VS EQUALS ===");

        Customer sameCustomer = Customer.of(
                " +380671234567 ",
                "sofia",
                "Sofia@EXAMPLE.COM"
        );

        Customer differentCustomer = Customer.of(
                " +380991112233 ",
                "Different name",
                "different@example.com"
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
                "customer.equals(differentCustomer): "
                        + customer.equals(differentCustomer)
        );
        System.out.println(
                "customer.hashCode(): "
                        + customer.hashCode()
        );

        System.out.println(
                "sameCustomer.hashCode(): "
                        + sameCustomer.hashCode()
        );

        // 6. Computed methods

        System.out.println();
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

        // 7. toString() for all entities

        System.out.println();
        System.out.println("=== TOSTRING ===");

        System.out.println(customer);
        System.out.println(table);
        System.out.println(reservation);
        System.out.println(order);

        // 8. Access restrictions.
        // ValidationHelper is package-private and cannot be accessed here.
        // customer.phone = "123"; // phone is private and cannot be accessed here.
    }
}