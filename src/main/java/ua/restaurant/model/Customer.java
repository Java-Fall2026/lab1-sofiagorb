package ua.restaurant.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ua.restaurant.util.RestaurantUtils;

import java.util.Comparator;

public record Customer(
        @NotBlank(message = "Номер телефону не може бути порожнім")
        @Pattern(regexp = "^\\+380\\d{9}$", message = "Телефон має відповідати формату +380XXXXXXXXX")
        String phone,

        @NotBlank(message = "Ім'я клієнта не може бути порожнім")
        @Size(min = 2, max = 60, message = "Ім'я клієнта має містити від 2 до 60 символів")
        String name,

        @NotBlank(message = "Електронна пошта не може бути порожньою")
        @Email(message = "Некоректний формат електронної пошти")
        String email
) implements Comparable<Customer> {

    private static final Comparator<Customer> NATURAL_ORDER = Comparator
            .comparing(Customer::phone)
            .thenComparing(Customer::name)
            .thenComparing(Customer::email);

    public Customer {
        phone = phone == null ? null : RestaurantUtils.normalize(phone);
        name = name == null ? null : RestaurantUtils.capitalize(name);
        email = email == null ? null : RestaurantUtils.normalize(email);
    }

    public static Customer of(String phone, String name, String email) {
        return new Customer(phone, name, email);
    }

    @Override
    public int compareTo(Customer other) {
        return NATURAL_ORDER.compare(this, other);
    }
}