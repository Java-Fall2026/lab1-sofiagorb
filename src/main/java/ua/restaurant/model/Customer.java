package ua.restaurant.model;

import ua.restaurant.util.RestaurantUtils;

import java.util.Comparator;

public record Customer(
        String phone,
        String name,
        String email
) implements Comparable<Customer> {

    private static final Comparator<Customer> NATURAL_ORDER = Comparator
            .comparing(Customer::phone)
            .thenComparing(Customer::name)
            .thenComparing(Customer::email);

    public Customer {
        phone = RestaurantUtils.normalize(
                RestaurantUtils.requireNotBlank(phone, "Phone")
        );

        name = RestaurantUtils.capitalize(
                RestaurantUtils.requireNotBlank(name, "Name")
        );

        email = RestaurantUtils.normalize(
                RestaurantUtils.requireEmail(email, "Email")
        );
    }

    public static Customer of(String phone, String name, String email) {
        return new Customer(phone, name, email);
    }

    @Override
    public int compareTo(Customer other) {
        return NATURAL_ORDER.compare(this, other);
    }
}