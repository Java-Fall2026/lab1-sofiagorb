package ua.restaurant.model;

import ua.common.BaseEntity;
import ua.restaurant.util.RestaurantUtils;

import java.util.Objects;

public class Customer extends BaseEntity {

    private final String phone;
    private final String name;
    private final String email;

    private Customer(String phone, String name, String email) {
        super();

        this.phone = RestaurantUtils.normalize(
                RestaurantUtils.requireNotBlank(phone, "Phone")
        );

        this.name = RestaurantUtils.capitalize(
                RestaurantUtils.requireNotBlank(name, "Name")
        );

        this.email = RestaurantUtils.normalize(
                RestaurantUtils.requireEmail(email, "Email")
        );
    }

    public static Customer of(String phone, String name, String email) {
        return new Customer(phone, name, email);
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Customer customer = (Customer) o;
        return phone.equals(customer.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phone);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "phone='" + phone + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}