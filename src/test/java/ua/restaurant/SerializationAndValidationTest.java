package ua.restaurant;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ua.restaurant.loader.AppConfig;
import ua.restaurant.loader.DataLoader;
import ua.restaurant.loader.LoadResult;
import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.TableZone;
import ua.restaurant.repository.CustomerRepository;
import ua.restaurant.repository.RestaurantOrderRepository;
import ua.restaurant.storage.FileStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerializationAndValidationTest {

    private static Validator validator;

    @TempDir
    Path tempDir;

    @BeforeAll
    static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private RestaurantOrder createSampleOrder(String orderId, LocalDate date, double total, double discount) {
        Customer customer = Customer.of("+380501112233", "Olena Koval", "olena@gmail.com");
        RestaurantTable table = new RestaurantTable(1, 4, TableZone.MAIN_HALL);
        Reservation reservation = new Reservation(
                customer,
                table,
                date,
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                2,
                ReservationStatus.CONFIRMED
        );
        return RestaurantOrder.of(orderId, reservation, total, discount);
    }

    @Test
    @DisplayName("JSON save and load round-trip preserves list equality")
    void jsonSaveAndLoadProducesEqualList() throws IOException {
        FileStorage<Customer> storage = new FileStorage<>(Customer.class);
        Path jsonFile = tempDir.resolve("customers.json");

        List<Customer> expected = List.of(
                Customer.of("+380501112233", "Olena Koval", "olena@gmail.com"),
                Customer.of("+380672223344", "Andriy Melnyk", "andriy@ukr.net")
        );

        storage.save(jsonFile, expected);
        List<Customer> actual = storage.load(jsonFile);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("YAML save and load round-trip preserves list equality")
    void yamlSaveAndLoadProducesEqualList() throws IOException {
        FileStorage<RestaurantOrder> storage = new FileStorage<>(RestaurantOrder.class);
        Path yamlFile = tempDir.resolve("orders.yaml");

        List<RestaurantOrder> expected = List.of(
                createSampleOrder("ORD-101", LocalDate.of(2026, 11, 2), 850.50, 50.00),
                createSampleOrder("ORD-102", LocalDate.of(2026, 11, 3), 1200.00, 100.00)
        );

        storage.save(yamlFile, expected);
        List<RestaurantOrder> actual = storage.load(yamlFile);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Dates in serialized files are written as ISO strings rather than numeric arrays")
    void datesAreSerializedAsIsoStrings() throws IOException {
        FileStorage<RestaurantOrder> storage = new FileStorage<>(RestaurantOrder.class);
        Path jsonFile = tempDir.resolve("orders_date.json");
        RestaurantOrder order = createSampleOrder("ORD-105", LocalDate.of(2026, 11, 2), 500.0, 0.0);
        storage.save(jsonFile, List.of(order));

        String rawContent = Files.readString(jsonFile);
        assertTrue(rawContent.contains("\"2026-11-02\""), "Expected ISO date string in file content");
        assertFalse(rawContent.contains("[2026,11,2]"), "Date must not be serialized as array");
    }

    @Test
    @DisplayName("Unknown file extension throws IllegalArgumentException")
    void unknownFileExtensionThrowsException() {
        FileStorage<Customer> storage = new FileStorage<>(Customer.class);
        Path xmlFile = tempDir.resolve("customers.xml");

        assertThrows(IllegalArgumentException.class, () -> storage.load(xmlFile));
        assertThrows(IllegalArgumentException.class, () -> storage.save(xmlFile, List.of()));
    }

    static Stream<Arguments> invalidCustomersProvider() {
        return Stream.of(
                Arguments.of(Customer.of("+380501112233", "   ", "olena@gmail.com"), "name"),
                Arguments.of(Customer.of("12345", "Olena Koval", "olena@gmail.com"), "phone"),
                Arguments.of(Customer.of("+380501112233", "O", "olena@gmail.com"), "name"),
                Arguments.of(Customer.of("+380501112233", "Olena Koval", "invalid-email"), "email")
        );
    }

    @ParameterizedTest(name = "Standard validation catches invalid field: {1}")
    @MethodSource("invalidCustomersProvider")
    @DisplayName("Standard Jakarta Validation annotations detect invalid customer fields")
    void standardAnnotationsDetectInvalidValues(Customer invalidCustomer, String expectedField) {
        Set<ConstraintViolation<Customer>> violations = validator.validate(invalidCustomer);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals(expectedField)));
    }

    @Test
    @DisplayName("Standard numeric annotations @Positive, @Min, and @Max catch out-of-range values")
    void standardNumericAnnotationsDetectInvalidValues() {
        RestaurantOrder negativeTotal = createSampleOrder("ORD-201", LocalDate.of(2026, 11, 2), -50.0, 0.0);
        Set<ConstraintViolation<RestaurantOrder>> orderViolations = validator.validate(negativeTotal);
        assertTrue(orderViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("totalAmount")));

        RestaurantTable invalidTable = new RestaurantTable(0, 25, TableZone.MAIN_HALL);
        Set<ConstraintViolation<RestaurantTable>> tableViolations = validator.validate(invalidTable);
        assertTrue(tableViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("tableNumber")));
        assertTrue(tableViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("capacity")));
    }

    @Test
    @DisplayName("Custom @ValidDiscount annotation accepts valid discount and rejects discount exceeding total")
    void customValidDiscountAnnotationChecksCrossFieldRule() {
        RestaurantOrder validOrder = createSampleOrder("ORD-301", LocalDate.of(2026, 11, 2), 500.0, 500.0);
        assertTrue(validator.validate(validOrder).isEmpty());

        RestaurantOrder invalidDiscountOrder = createSampleOrder("ORD-302", LocalDate.of(2026, 11, 2), 500.0, 500.01);
        Set<ConstraintViolation<RestaurantOrder>> violations = validator.validate(invalidDiscountOrder);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Знижка не може перевищувати")));
    }

    @Test
    @DisplayName("DataLoader adds only valid records to repository and reports invalid ones in LoadResult")
    void loaderFiltersInvalidRecordsAndReportsViolations() throws IOException {
        FileStorage<Customer> storage = new FileStorage<>(Customer.class);
        DataLoader<Customer> loader = new DataLoader<>(storage, validator);
        CustomerRepository repo = new CustomerRepository();
        Path file = tempDir.resolve("mixed_customers.json");
        List<Customer> records = List.of(
                Customer.of("+380501112233", "Olena Koval", "olena@gmail.com"),
                Customer.of("bad-phone", "Invalid Phone", "bad@gmail.com"),
                Customer.of("+380672223344", "Andriy Melnyk", "not-an-email")
        );
        storage.save(file, records);

        LoadResult result = loader.loadInto(file, repo);

        assertEquals(3, result.totalRead());
        assertEquals(1, result.addedCount());
        assertEquals(2, result.rejectedCount());
        assertEquals(1, repo.size());
    }

    @Test
    @DisplayName("DataLoader captures duplicate identity in rejected list without throwing")
    void duplicateIdentityIsRecordedInRejectedList() throws IOException {
        FileStorage<Customer> storage = new FileStorage<>(Customer.class);
        DataLoader<Customer> loader = new DataLoader<>(storage, validator);
        CustomerRepository repo = new CustomerRepository();

        Path file = tempDir.resolve("duplicates.json");
        List<Customer> records = List.of(
                Customer.of("+380501112233", "Olena Koval", "olena@gmail.com"),
                Customer.of("+380501112233", "Olena Duplicate", "dup@gmail.com")
        );
        storage.save(file, records);

        LoadResult result = loader.loadInto(file, repo);

        assertEquals(2, result.totalRead());
        assertEquals(1, result.addedCount());
        assertEquals(1, result.rejectedCount());
        assertEquals("identity", result.rejected().getFirst().field());
    }

    @Test
    @DisplayName("Syntax-corrupted file and non-existent file do not crash DataLoader")
    void brokenAndMissingFilesAreHandledGracefully() throws IOException {
        FileStorage<Customer> storage = new FileStorage<>(Customer.class);
        DataLoader<Customer> loader = new DataLoader<>(storage, validator);
        CustomerRepository repo = new CustomerRepository();

        Path brokenFile = tempDir.resolve("broken.json");
        Files.writeString(brokenFile, "[ { \"phone\": \"+380501112233\", broken-syntax ");

        LoadResult brokenResult = loader.loadInto(brokenFile, repo);
        assertEquals(0, brokenResult.addedCount());
        assertEquals(1, brokenResult.rejectedCount());

        Path missingFile = tempDir.resolve("does_not_exist.json");
        LoadResult missingResult = loader.loadInto(missingFile, repo);
        assertEquals(0, missingResult.addedCount());
        assertEquals(1, missingResult.rejectedCount());
        assertEquals(0, repo.size());
    }

    @Test
    @DisplayName("Missing key in AppConfig throws IllegalArgumentException containing the key name")
    void missingPropertyKeyThrowsExceptionWithKeyName() {
        Properties props = new Properties();
        props.setProperty("data.customers.file", "data/customers.json");
        AppConfig config = new AppConfig(props);

        String missingKey = "data.orders.file";
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> config.getRequiredPath(missingKey)
        );
        assertTrue(ex.getMessage().contains(missingKey));
    }
}
