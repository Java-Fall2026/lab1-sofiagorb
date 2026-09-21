package ua.restaurant.model;

import ua.common.BaseEntity;
import ua.restaurant.util.RestaurantUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Reservation extends BaseEntity {

    static final int MIN_GUEST_COUNT = 1;

    static final String PENDING = "PENDING";
    static final String CONFIRMED = "CONFIRMED";
    static final String SEATED = "SEATED";
    static final String COMPLETED = "COMPLETED";
    static final String CANCELLED = "CANCELLED";

    private final Customer customer;
    private final RestaurantTable table;
    private final LocalDate reservationDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int guestCount;
    private String status;

    public Reservation(
            Customer customer,
            RestaurantTable table,
            LocalDate reservationDate,
            LocalTime startTime,
            LocalTime endTime,
            int guestCount,
            String status
    ) {
        super();

        this.customer = RestaurantUtils.requireNotNull(
                customer, "Customer"
        );

        this.table = RestaurantUtils.requireNotNull(
                table, "Table"
        );

        this.reservationDate = RestaurantUtils.requireNotNull(
                reservationDate, "Reservation date"
        );

        this.startTime = RestaurantUtils.requireNotNull(
                startTime, "Start time"
        );

        this.endTime = RestaurantUtils.requireNotNull(
                endTime, "End time"
        );

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(
                    "End time must be later than start time, got: "
                            + endTime
            );
        }

        if (guestCount < MIN_GUEST_COUNT
                || guestCount > table.getCapacity()) {

            throw new IllegalArgumentException(
                    "Guest count must be between "
                            + MIN_GUEST_COUNT
                            + " and "
                            + table.getCapacity()
                            + ", got: "
                            + guestCount
            );
        }

        this.guestCount = guestCount;

        setStatus(status);
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

    public String getStatus() {
        return status;
    }

    public final void setStatus(String status) {
        String normalizedStatus = RestaurantUtils.normalizeUpperCase(
                RestaurantUtils.requireNotBlank(status, "Status")
        );

        if (!normalizedStatus.equals(PENDING)
                && !normalizedStatus.equals(CONFIRMED)
                && !normalizedStatus.equals(SEATED)
                && !normalizedStatus.equals(COMPLETED)
                && !normalizedStatus.equals(CANCELLED)) {

            throw new IllegalArgumentException(
                    "Status must be one of PENDING, CONFIRMED, "
                            + "SEATED, COMPLETED, CANCELLED, got: "
                            + status
            );
        }

        this.status = normalizedStatus;
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

        return customer.equals(that.customer)
                && table.equals(that.table)
                && reservationDate.equals(that.reservationDate)
                && startTime.equals(that.startTime);
    }
    @Override
    public int hashCode() {
        return Objects.hash(
                customer,
                table,
                reservationDate,
                startTime
        );
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
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}