package ua.restaurant.util;

import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.TableZone;

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

    public static String zoneDescription(TableZone zone) {
        return switch (zone) {
            case MAIN_HALL -> "Main hall";
            case TERRACE -> "Terrace";
            case VIP -> "VIP zone";
            case BAR -> "Bar";
        };
    }

    public static String availableAction(ReservationStatus status) {
        return switch (status) {
            case PENDING -> "Confirm reservation";
            case CONFIRMED -> "Seat customer";
            case SEATED -> "Complete reservation";
            case COMPLETED -> "No action";
            case CANCELLED -> "No action";
        };
    }
}