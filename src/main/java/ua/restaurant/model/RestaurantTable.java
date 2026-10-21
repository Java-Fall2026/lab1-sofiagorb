package ua.restaurant.model;

import ua.restaurant.util.RestaurantUtils;

public record RestaurantTable(
        int tableNumber,
        int capacity,
        TableZone zone
) {

    static final int MIN_TABLE_NUMBER = 1;
    static final int MAX_TABLE_NUMBER = 200;
    static final int MIN_CAPACITY = 1;
    static final int MAX_CAPACITY = 20;

    public RestaurantTable {
        tableNumber = RestaurantUtils.requireInRange(
                tableNumber,
                MIN_TABLE_NUMBER,
                MAX_TABLE_NUMBER,
                "Table number"
        );

        capacity = RestaurantUtils.requireInRange(
                capacity,
                MIN_CAPACITY,
                MAX_CAPACITY,
                "Capacity"
        );

        zone = RestaurantUtils.requireNotNull(zone, "Zone");
    }
}