package ua.restaurant.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.restaurant.exception.DuplicateIdentityException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GenericRepository<T> {

    private static final Logger log = LoggerFactory.getLogger(GenericRepository.class);

    private final String name;
    private final IdentityExtractor<T> extractor;
    private final Map<String, T> storage = new LinkedHashMap<>();

    public GenericRepository(String name, IdentityExtractor<T> extractor) {
        if (name == null || name.isBlank()) {
            log.error("Repository name must not be null or blank");
            throw new IllegalArgumentException("Repository name must not be null or blank");
        }
        if (extractor == null) {
            log.error("IdentityExtractor must not be null for repository '{}'", name);
            throw new IllegalArgumentException("IdentityExtractor must not be null");
        }
        this.name = name;
        this.extractor = extractor;
    }

    public void add(T item) {
        String id = validateAndExtractId(item);
        if (storage.containsKey(id)) {
            String message = String.format("Duplicate identity '%s' in repository '%s'", id, name);
            log.warn(message);
            log.error("Throwing DuplicateIdentityException for identity '{}' in repository '{}'", id, name);
            throw new DuplicateIdentityException(message);
        }
        storage.put(id, item);
        log.info("Added item with identity '{}' to repository '{}'", id, name);
    }

    public boolean addIfAbsent(T item) {
        String id = validateAndExtractId(item);
        if (storage.containsKey(id)) {
            log.warn("Duplicate identity '{}' ignored in repository '{}' (addIfAbsent)", id, name);
            return false;
        }
        storage.put(id, item);
        log.info("Added item with identity '{}' to repository '{}' (addIfAbsent)", id, name);
        return true;
    }

    public void addAll(Collection<? extends T> items) {
        if (items == null) {
            log.error("Cannot add null collection to repository '{}'", name);
            throw new IllegalArgumentException("Collection must not be null");
        }
        for (T item : items) {
            add(item);
        }
    }

    public Optional<T> findByIdentity(String identity) {
        if (identity == null || identity.isBlank()) {
            log.warn("Search called with null or blank identity in repository '{}'", name);
            return Optional.empty();
        }
        T found = storage.get(identity);
        if (found != null) {
            log.info("Found item with identity '{}' in repository '{}'", identity, name);
        } else {
            log.warn("Item with identity '{}' not found in repository '{}'", identity, name);
        }
        return Optional.ofNullable(found);
    }

    public boolean removeByIdentity(String identity) {
        if (identity == null || identity.isBlank()) {
            log.warn("Remove called with null or blank identity in repository '{}'", name);
            return false;
        }
        T removed = storage.remove(identity);
        if (removed != null) {
            log.info("Removed item with identity '{}' from repository '{}'", identity, name);
            return true;
        }
        log.warn("Attempted to remove non-existent identity '{}' from repository '{}'", identity, name);
        return false;
    }

    public boolean remove(T item) {
        String id = validateAndExtractId(item);
        return removeByIdentity(id);
    }

    public List<T> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(storage.values()));
    }

    public int size() {
        return storage.size();
    }
    private String validateAndExtractId(T item) {
        if (item == null) {
            log.error("Null item passed to repository '{}'", name);
            throw new IllegalArgumentException("Item must not be null");
        }
        String id = extractor.identityOf(item);
        if (id == null || id.isBlank()) {
            log.error("Extracted identity is null or blank in repository '{}'", name);
            throw new IllegalArgumentException("Identity must not be null or blank");
        }
        return id;
    }
}