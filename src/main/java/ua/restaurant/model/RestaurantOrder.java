package ua.restaurant.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ua.common.BaseEntity;
import ua.restaurant.util.RestaurantUtils;
import ua.restaurant.validation.ValidDiscount;

import java.util.Objects;

@ValidDiscount
public class RestaurantOrder extends BaseEntity implements Comparable<RestaurantOrder> {

    @NotBlank(message = "Ідентифікатор замовлення не може бути порожнім")
    @Pattern(regexp = "^ORD-\\d+$", message = "Ідентифікатор замовлення має відповідати формату ORD-XXX")
    private final String orderId;

    @NotNull(message = "Бронювання для замовлення є обов'язковим")
    @Valid
    private final Reservation reservation;

    @Positive(message = "Загальна сума замовлення має бути більшою за 0")
    private final double totalAmount;

    @PositiveOrZero(message = "Сума знижки не може бути від'ємною")
    private double discountAmount;

    @JsonCreator
    private RestaurantOrder(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("reservation") Reservation reservation,
            @JsonProperty("totalAmount") double totalAmount,
            @JsonProperty("discountAmount") double discountAmount
    ) {
        super();
        this.orderId = orderId == null ? null : RestaurantUtils.normalizeUpperCase(orderId);
        this.reservation = reservation;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
    }

    public static RestaurantOrder of(
            String orderId,
            Reservation reservation,
            double totalAmount,
            double discountAmount
    ) {
        return new RestaurantOrder(orderId, reservation, totalAmount, discountAmount);
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

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    @Override
    public int compareTo(RestaurantOrder other) {
        int byAmount = Double.compare(this.totalAmount, other.totalAmount);
        if (byAmount != 0) {
            return byAmount;
        }
        return Objects.compare(this.orderId, other.orderId, ComparatorNullSafe.INSTANCE);
    }

    private enum ComparatorNullSafe implements java.util.Comparator<String> {
        INSTANCE;

        @Override
        public int compare(String a, String b) {
            if (a == b) return 0;
            if (a == null) return -1;
            if (b == null) return 1;
            return a.compareTo(b);
        }
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
        return Objects.equals(orderId, that.orderId);
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