package ua.restaurant.repository;

@FunctionalInterface
public interface IdentityExtractor<T, K> {
    K extract(T entity);
}