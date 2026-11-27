package ua.restaurant.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import ua.common.BaseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Reservation extends BaseEntity {

    @NotNull(message = "Клієнт бронювання є обов'язковим")
    @Valid
    private final Customer customer;

    @NotNull(message = "Столик бронювання є обов'язковим")
    @Valid
    private final RestaurantTable table;

    @NotNull(message = "Дата бронювання є обов'язковою")
    private final LocalDate reservationDate;

    @NotNull(message = "Час початку бронювання є обов'язковим")
    private final LocalTime startTime;

    @NotNull(message = "Час завершення бронювання є обов'язковим")
    private final LocalTime endTime;

    @Min(value = 1, message = "Кількість гостей має бути щонайменше 1")
    private final int guestCount;

    @NotNull(message = "Статус бронювання є обов'язковим")
    private ReservationStatus status;

    @JsonCreator
    public Reservation(
            @JsonProperty("customer") Customer customer,
            @JsonProperty("table") RestaurantTable table,
            @JsonProperty("reservationDate") LocalDate reservationDate,
            @JsonProperty("startTime") LocalTime startTime,
            @JsonProperty("endTime") LocalTime endTime,
            @JsonProperty("guestCount") int guestCount,
            @JsonProperty("status") ReservationStatus status
    ) {
        super();
        this.customer = customer;
        this.table = table;
        this.reservationDate = reservationDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.guestCount = guestCount;
        this.status = status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public RestaurantTable getTable() {
        return table;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = Objects.requireNonNull(status, "Status must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(customer, that.customer)
                && Objects.equals(table, that.table)
                && Objects.equals(reservationDate, that.reservationDate)
                && Objects.equals(startTime, that.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, table, reservationDate, startTime);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "customer=" + customer +
                ", table=" + table +
                ", reservationDate=" + reservationDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", guestCount=" + guestCount +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}