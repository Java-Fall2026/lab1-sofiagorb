package ua.restaurant.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    public static final String KEY_CUSTOMERS_FILE = "data.customers.file";
    public static final String KEY_ORDERS_FILE = "data.orders.file";
    public static final String KEY_BROKEN_FILE = "data.broken.file";
    public static final String KEY_OUTPUT_ORDERS_FILE = "data.output.orders.file";

    private final Properties properties;

    public AppConfig(Properties properties) {
        this.properties = Objects.requireNonNull(properties, "Properties must not be null");
    }

    public static AppConfig loadFromClasspath(String resourceName) {
        Properties props = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                log.error("Configuration resource '{}' not found on classpath", resourceName);
                throw new IllegalStateException("Configuration resource not found: " + resourceName);
            }
            props.load(in);
            log.info("Loaded configuration from '{}'", resourceName);
            return new AppConfig(props);
        } catch (IOException e) {
            log.error("Failed to read configuration resource '{}'", resourceName, e);
            throw new IllegalStateException("Failed to read configuration resource: " + resourceName, e);
        }
    }

    public Path getRequiredPath(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            log.error("Missing required configuration key '{}'", key);
            throw new IllegalArgumentException("Missing required property key: " + key);
        }
        return Path.of(value.trim());
    }
}