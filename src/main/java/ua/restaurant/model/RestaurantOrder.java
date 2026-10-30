package ua.restaurant.model;

import ua.common.BaseEntity;
import ua.restaurant.util.RestaurantUtils;

import java.util.Objects;

public class RestaurantOrder extends BaseEntity {

    private final String orderId;
    private final Reservation reservation;
    private final double totalAmount;
    private double discountAmount;

    private RestaurantOrder(
            String orderId,
            Reservation reservation,
            double totalAmount,
            double discountAmount
    ) {
        super();

        this.orderId = RestaurantUtils.normalizeUpperCase(
                RestaurantUtils.requireNotBlank(orderId, "Order ID")
        );

        this.reservation = RestaurantUtils.requireNotNull(
                reservation, "Reservation"
        );

        if (totalAmount < 0) {
            throw new IllegalArgumentException(
                    "Total amount must be greater than or equal to 0, got: "
                            + totalAmount
            );
        }

        this.totalAmount = totalAmount;

        setDiscountAmount(discountAmount);
    }

    public static RestaurantOrder of(
            String orderId,
            Reservation reservation,
            double totalAmount,
            double discountAmount
    ) {
        return new RestaurantOrder(
                orderId,
                reservation,
                totalAmount,
                discountAmount
        );
    }

    public String getOrderId() {
        return orderId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public final void setDiscountAmount(double discountAmount) {
        if (discountAmount < 0 || discountAmount > totalAmount) {
            throw new IllegalArgumentException(
                    "Discount amount must be between 0 and "
                            + totalAmount + ", got: " + discountAmount
            );
        }

        this.discountAmount = discountAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RestaurantOrder that = (RestaurantOrder) o;
        return orderId.equals(that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return "RestaurantOrder{" +
                "orderId='" + orderId + '\'' +
                ", reservation=" + reservation +
                ", totalAmount=" + totalAmount +
                ", discountAmount=" + discountAmount +
                ", createdAt=" + createdAt +
                '}';
    }
}