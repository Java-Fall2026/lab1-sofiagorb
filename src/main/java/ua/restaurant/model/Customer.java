package ua.restaurant.model;

import ua.restaurant.util.RestaurantUtils;

public record Customer(
        String phone,
        String name,
        String email
) {

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
}