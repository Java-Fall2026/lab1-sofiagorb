package ua.restaurant.benchmark;

import ua.restaurant.model.Customer;
import ua.restaurant.repository.GenericRepository;

import java.util.ArrayList;
import java.util.List;

public class SearchBenchmark {

    private static final int ELEMENTS_COUNT = 100_000;
    private static final int WARMUP_ITERATIONS = 5;
    private static final int MEASURE_ITERATIONS = 10;

    public static void main(String[] args) {
        GenericRepository<Customer> repository = new GenericRepository<>("BenchmarkCustomers", Customer::phone);
        List<Customer> list = new ArrayList<>(ELEMENTS_COUNT);

        for (int i = 0; i < ELEMENTS_COUNT; i++) {
            String phone = String.format("+380%09d", i);
            Customer customer = Customer.of(phone, "User" + i, "user" + i + "@example.com");
            repository.add(customer);
            list.add(customer);
        }

        String firstId = String.format("+380%09d", 0);
        String lastId = String.format("+380%09d", ELEMENTS_COUNT - 1);
        String missingId = "+380999999999";

        // Прогрів JVM (Warmup)
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            repository.findByIdentity(firstId);
            repository.findByIdentity(lastId);
            repository.findByIdentity(missingId);

            linearSearch(list, firstId);
            linearSearch(list, lastId);
            linearSearch(list, missingId);
        }

        // Заміри часу (у наносекундах)
        long mapFirstTotal = 0, mapLastTotal = 0, mapMissingTotal = 0;
        long listFirstTotal = 0, listLastTotal = 0, listMissingTotal = 0;

        for (int i = 0; i < MEASURE_ITERATIONS; i++) {
            long start = System.nanoTime();
            repository.findByIdentity(firstId);
            mapFirstTotal += System.nanoTime() - start;

            start = System.nanoTime();
            repository.findByIdentity(lastId);
            mapLastTotal += System.nanoTime() - start;

            start = System.nanoTime();
            repository.findByIdentity(missingId);
            mapMissingTotal += System.nanoTime() - start;

            start = System.nanoTime();
            linearSearch(list, firstId);
            listFirstTotal += System.nanoTime() - start;

            start = System.nanoTime();
            linearSearch(list, lastId);
            listLastTotal += System.nanoTime() - start;

            start = System.nanoTime();
            linearSearch(list, missingId);
            listMissingTotal += System.nanoTime() - start;
        }

        System.out.println("=== РЕЗУЛЬТАТИ БЕНЧМАРКУ (середній час за " + MEASURE_ITERATIONS + " замірів, нс) ===");
        System.out.printf("Перший елемент:    Map = %d нс | List = %d нс%n",
                mapFirstTotal / MEASURE_ITERATIONS, listFirstTotal / MEASURE_ITERATIONS);
        System.out.printf("Останній елемент:  Map = %d нс | List = %d нс%n",
                mapLastTotal / MEASURE_ITERATIONS, listLastTotal / MEASURE_ITERATIONS);
        System.out.printf("Відсутній елемент: Map = %d нс | List = %d нс%n",
                mapMissingTotal / MEASURE_ITERATIONS, listMissingTotal / MEASURE_ITERATIONS);
    }

    private static Customer linearSearch(List<Customer> list, String phone) {
        for (Customer c : list) {
            if (c.phone().equals(phone)) {
                return c;
            }
        }
        return null;
    }
}