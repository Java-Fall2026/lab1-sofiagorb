package ua.restaurant.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.restaurant.model.Customer;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CustomerRepository extends GenericRepository<Customer> {

    private static final Logger log = LoggerFactory.getLogger(CustomerRepository.class);

    public CustomerRepository() {
        super("customers", Customer::phone);
    }

    public List<Customer> sortByNameAsc() {
        List<Customer> result = findAllSorted(Comparator.comparing(Customer::name));
        log.info("Sorted {} customers by name ascending", result.size());
        return result;
    }

    public List<Customer> sortByNameThenEmailDesc() {
        Comparator<Customer> comparator = Comparator
                .comparing(Customer::name)
                .thenComparing(Comparator.comparing(Customer::email).reversed());
        List<Customer> result = findAllSorted(comparator);
        log.info("Sorted {} customers by name ascending then email descending", result.size());
        return result;
    }

    public List<Customer> findByNameContaining(String namePart) {
        if (namePart == null || namePart.isBlank()) {
            log.error("Name search query must not be null or blank in repository '{}'", getName());
            throw new IllegalArgumentException("Name query must not be null or blank");
        }
        String normalized = namePart.trim().toLowerCase();
        List<Customer> result = stream()
                .filter(c -> c.name().toLowerCase().contains(normalized))
                .sorted(Comparator.comparing(Customer::name))
                .toList();
        logSearchResult("name containing '" + namePart + "'", result.size());
        return result;
    }

    public List<Customer> findByPhoneRange(String minPhone, String maxPhone) {
        if (minPhone == null || maxPhone == null || minPhone.compareTo(maxPhone) > 0) {
            log.error("Invalid phone range [{}, {}] in repository '{}'", minPhone, maxPhone, getName());
            throw new IllegalArgumentException("Invalid phone range");
        }
        List<Customer> result = stream()
                .filter(c -> c.phone().compareTo(minPhone) >= 0 && c.phone().compareTo(maxPhone) <= 0)
                .sorted()
                .toList();
        logSearchResult("phone range [" + minPhone + ", " + maxPhone + "]", result.size());
        return result;
    }

    public List<Customer> findByNameAndEmailDomain(String namePart, String domain) {
        if (namePart == null || namePart.isBlank() || domain == null || domain.isBlank()) {
            log.error("Search parameters must not be null or blank in repository '{}'", getName());
            throw new IllegalArgumentException("Name part and domain must not be null or blank");
        }
        String normalizedName = namePart.trim().toLowerCase();
        String normalizedDomain = domain.trim().toLowerCase();
        List<Customer> result = stream()
                .filter(c -> c.name().toLowerCase().contains(normalizedName)
                        && c.email().toLowerCase().endsWith(normalizedDomain))
                .toList();
        logSearchResult("name '" + namePart + "' and domain '" + domain + "'", result.size());
        return result;
    }

    public Optional<Customer> findFirstByEmailDomain(String domain) {
        if (domain == null || domain.isBlank()) {
            log.error("Domain must not be null or blank in repository '{}'", getName());
            throw new IllegalArgumentException("Domain must not be null or blank");
        }
        String normalizedDomain = domain.trim().toLowerCase();
        Optional<Customer> found = stream()
                .filter(c -> c.email().toLowerCase().endsWith(normalizedDomain))
                .findFirst();
        logSearchResult("first customer with domain '" + domain + "'", found.isPresent() ? 1 : 0);
        return found;
    }
    public List<String> findTopUniqueEmailDomains(int limit) {
        if (limit <= 0) {
            log.error("Limit must be positive, got {} in repository '{}'", limit, getName());
            throw new IllegalArgumentException("Limit must be positive");
        }
        List<String> domains = stream()
                .map(Customer::email)
                .map(email -> email.substring(email.indexOf('@') + 1))
                .distinct()
                .sorted()
                .limit(limit)
                .toList();
        logSearchResult("top " + limit + " unique email domains", domains.size());
        return domains;
    }

    private void logSearchResult(String criteria, int count) {
        if (count == 0) {
            log.warn("No customers found matching criteria: {}", criteria);
        } else {
            log.info("Found {} customer(s) matching criteria: {}", count, criteria);
        }
    }
}