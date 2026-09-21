package ua.restaurant.model;

import ua.common.BaseEntity;
import ua.restaurant.util.RestaurantUtils;

import java.util.Objects;

public class RestaurantTable extends BaseEntity {

    static final int MIN_TABLE_NUMBER = 1;
    static final int MAX_TABLE_NUMBER = 200;
    static final int MIN_CAPACITY = 1;
    static final int MAX_CAPACITY = 20;

    static final String MAIN_HALL = "MAIN_HALL";
    static final String TERRACE = "TERRACE";
    static final String VIP = "VIP";
    static final String BAR = "BAR";

    private final int tableNumber;
    private final int capacity;
    private final String zone;

    public RestaurantTable(int tableNumber, int capacity, String zone) {
        super();

        this.tableNumber = RestaurantUtils.requireInRange(
                tableNumber,
                MIN_TABLE_NUMBER,
                MAX_TABLE_NUMBER,
                "Table number"
        );

        this.capacity = RestaurantUtils.requireInRange(
                capacity,
                MIN_CAPACITY,
                MAX_CAPACITY,
                "Capacity"
        );

        this.zone = RestaurantUtils.normalizeUpperCase(
                RestaurantUtils.requireNotBlank(zone, "Zone")
        );

        if (!this.zone.equals(MAIN_HALL)
                && !this.zone.equals(TERRACE)
                && !this.zone.equals(VIP)
                && !this.zone.equals(BAR)) {

            throw new IllegalArgumentException(
                    "Zone must be one of MAIN_HALL, TERRACE, VIP, BAR, got: "
                            + zone
            );
        }
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getZone() {
        return zone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RestaurantTable that = (RestaurantTable) o;
        return tableNumber == that.tableNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableNumber);
    }

    @Override
    public String toString() {
        return "RestaurantTable{" +
                "tableNumber=" + tableNumber +
                ", capacity=" + capacity +
                ", zone='" + zone + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}