package ua.restaurant.loader;

import java.util.List;

public record LoadResult(
        String source,
        int totalRead,
        int addedCount,
        List<RejectedRecord> rejected
) {
    public LoadResult {
        rejected = rejected == null ? List.of() : List.copyOf(rejected);
    }

    public int rejectedCount() {
        return rejected.size();
    }

    public record RejectedRecord(
            int recordNumber,
            String field,
            String message
    ) {
        @Override
        public String toString() {
            return String.format("Запис #%d | поле '%s' -> %s", recordNumber, field, message);
        }
    }
}