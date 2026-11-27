package ua.restaurant.loader;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.restaurant.exception.DuplicateIdentityException;
import ua.restaurant.repository.GenericRepository;
import ua.restaurant.storage.FileStorage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DataLoader<T> {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final FileStorage<T> storage;
    private final Validator validator;

    public DataLoader(FileStorage<T> storage) {
        this(storage, createDefaultValidator());
    }

    public DataLoader(FileStorage<T> storage, Validator validator) {
        this.storage = Objects.requireNonNull(storage, "FileStorage must not be null");
        this.validator = Objects.requireNonNull(validator, "Validator must not be null");
    }

    private static Validator createDefaultValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        return factory.getValidator();
    }

    public LoadResult loadInto(Path file, GenericRepository<T> repository) {
        Objects.requireNonNull(file, "File path must not be null");
        Objects.requireNonNull(repository, "Repository must not be null");

        List<T> rawItems;
        try {
            rawItems = storage.load(file);
        } catch (IOException | RuntimeException e) {
            log.error("Failed to read data file '{}': {}", file, e.getMessage());
            LoadResult.RejectedRecord fileError = new LoadResult.RejectedRecord(
                    0,
                    "file",
                    "Помилка читання файлу '" + file + "': " + e.getMessage()
            );
            LoadResult failedResult = new LoadResult(file.toString(), 0, 0, List.of(fileError));
            log.info("File '{}' summary: read={}, added={}, rejected={}", file, 0, 0, 1);
            log.warn("Rejected entry in '{}': {}", file, fileError);
            return failedResult;
        }

        int addedCount = 0;
        List<LoadResult.RejectedRecord> rejected = new ArrayList<>();

        for (int i = 0; i < rawItems.size(); i++) {
            int recordNumber = i + 1;
            T item = rawItems.get(i);

            if (item == null) {
                LoadResult.RejectedRecord nullRejection = new LoadResult.RejectedRecord(
                        recordNumber,
                        "record",
                        "Запис порожній (null)"
                );
                rejected.add(nullRejection);
                continue;
            }

            Set<ConstraintViolation<T>> violations = validator.validate(item);
            if (!violations.isEmpty()) {
                List<ConstraintViolation<T>> sortedViolations = violations.stream()
                        .sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
                        .toList();
                for (ConstraintViolation<T> violation : sortedViolations) {
                    String path = violation.getPropertyPath().toString();
                    String fieldName = path.isEmpty() ? "object" : path;
                    rejected.add(new LoadResult.RejectedRecord(
                            recordNumber,
                            fieldName,
                            violation.getMessage()
                    ));
                }
                continue;
            }

            try {
                repository.add(item);
                addedCount++;
            } catch (DuplicateIdentityException dupEx) {
                rejected.add(new LoadResult.RejectedRecord(
                        recordNumber,
                        "identity",
                        "Дублікат ідентичності: " + dupEx.getMessage()
                ));
            }
        }
        LoadResult result = new LoadResult(file.toString(), rawItems.size(), addedCount, rejected);
        log.info("File '{}' summary: read={}, added={}, rejected={}",
                file, result.totalRead(), result.addedCount(), result.rejectedCount());
        for (LoadResult.RejectedRecord entry : rejected) {
            log.warn("Rejected entry in '{}': {}", file, entry);
        }
        return result;
    }
}
