package ua.restaurant.util;

import java.util.Locale;

final class FormatHelper {

    private FormatHelper() {
    }

    static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    static String normalizeUpperCase(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        return Character.toUpperCase(normalized.charAt(0))
                + normalized.substring(1);
    }

    static String formatMoney(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}