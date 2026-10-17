package ua.restaurant.model;

import ua.restaurant.util.RestaurantUtils;

public record Room(
        int roomNumber,
        RoomType type,
        double pricePerNight
) {

    public Room {
        if (roomNumber <= 0) {
            throw new IllegalArgumentException(
                    "Room number must be greater than 0, got: "
                            + roomNumber
            );
        }

        type = RestaurantUtils.requireNotNull(type, "Room type");

        if (!Double.isFinite(pricePerNight) || pricePerNight <= 0) {
            throw new IllegalArgumentException(
                    "Price per night must be positive and finite, got: "
                            + pricePerNight
            );
        }
    }

    public double totalPrice(int nights) {
        if (nights <= 0) {
            throw new IllegalArgumentException(
                    "Number of nights must be positive, got: " + nights
            );
        }

        return pricePerNight * nights;
    }
}