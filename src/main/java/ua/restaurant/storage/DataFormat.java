package ua.restaurant.storage;

import java.nio.file.Path;
import java.util.Locale;

public enum DataFormat {
    JSON,
    YAML;

    public static DataFormat fromPath(Path file) {
        if (file == null || file.getFileName() == null) {
            throw new IllegalArgumentException("File path must not be null");
        }
        String fileName = file.getFileName().toString().toLowerCase(Locale.ROOT);
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            throw new IllegalArgumentException("File has no extension: " + file);
        }
        String ext = fileName.substring(dotIndex + 1);
        return switch (ext) {
            case "json" -> JSON;
            case "yaml", "yml" -> YAML;
            default -> throw new IllegalArgumentException("Unsupported file extension '." + ext + "' for file: " + file);
        };
    }
}