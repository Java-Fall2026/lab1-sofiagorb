package ua.restaurant.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GenericRepository<T, K> {

    private static final Logger log = LoggerFactory.getLogger(GenericRepository.class);

    private final Map<K, T> storage = new LinkedHashMap<>();
    private final IdentityExtractor<T, K> identityExtractor;
    private final String entityName;

    public GenericRepository(IdentityExtractor<T, K> identityExtractor, String entityName) {
        this.identityExtractor = Objects.requireNonNull(identityExtractor, "IdentityExtractor must not be null");
        this.entityName = Objects.requireNonNull(entityName, "Entity name must not be null");
    }

    public boolean add(T entity) {
        Objects.requireNonNull(entity, "Entity must not be null");
        K id = identityExtractor.extract(entity);

        if (storage.containsKey(id)) {
            log.warn("Duplicate {} ignored. Identity already exists: {}", entityName, id);
            return false;
        }

        storage.put(id, entity);
        log.info("Added {} with identity: {}", entityName, id);
        return true;
    }

    public boolean removeByIdentity(K identity) {
        Objects.requireNonNull(identity, "Identity must not be null");
        T removed = storage.remove(identity);

        if (removed != null) {
            log.info("Removed {} with identity: {}", entityName, identity);
            return true;
        }

        log.warn("Attempted to remove non-existent {} with identity: {}", entityName, identity);
        return false;
    }

    public boolean remove(T entity) {
        Objects.requireNonNull(entity, "Entity must not be null");
        return removeByIdentity(identityExtractor.extract(entity));
    }

    public Optional<T> findByIdentity(K identity) {
        if (identity == null) {
            log.warn("Search called with null identity for {}", entityName);
            return Optional.empty();
        }
        T found = storage.get(identity);
        log.info("Search {} by identity '{}': {}", entityName, identity, found != null ? "FOUND" : "NOT FOUND");
        return Optional.ofNullable(found);
    }

    public List<T> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(storage.values()));
    }

    public int size() {
        return storage.size();
    }
}