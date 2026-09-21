package ua.restaurant.util;

import ua.restaurant.model.Reservation;
import ua.restaurant.model.RestaurantOrder;

import java.time.Duration;

public final class RestaurantUtils {

    private RestaurantUtils() {
    }

    public static String requireNotBlank(String value, String fieldName) {
        return ValidationHelper.requireNotBlank(value, fieldName);
    }

    public static String requireEmail(String value, String fieldName) {
        return ValidationHelper.requireEmail(value, fieldName);
    }

    public static int requirePositive(int value, String fieldName) {
        return ValidationHelper.requirePositive(value, fieldName);
    }

    public static int requireInRange(
            int value, int min, int max, String fieldName) {
        return ValidationHelper.requireInRange(value, min, max, fieldName);
    }

    public static <T> T requireNotNull(T value, String fieldName) {
        return ValidationHelper.requireNotNull(value, fieldName);
    }

    public static String normalize(String value) {
        return FormatHelper.normalize(value);
    }

    public static String normalizeUpperCase(String value) {
        return FormatHelper.normalizeUpperCase(value);
    }

    public static String capitalize(String value) {
        return FormatHelper.capitalize(value);
    }

    public static String formatMoney(double value) {
        return FormatHelper.formatMoney(value);
    }

    public static long durationMinutes(Reservation reservation) {
        requireNotNull(reservation, "Reservation");

        return Duration.between(
                reservation.getStartTime(),
                reservation.getEndTime()
        ).toMinutes();
    }

    public static double finalAmount(RestaurantOrder order) {
        requireNotNull(order, "Order");

        return order.getTotalAmount() - order.getDiscountAmount();
    }
}