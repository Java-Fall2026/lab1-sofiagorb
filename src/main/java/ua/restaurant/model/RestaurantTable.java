package ua.restaurant.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RestaurantTable(
        @Min(value = 1, message = "Номер столика має бути не менше 1")
        @Max(value = 200, message = "Номер столика не може перевищувати 200")
        int tableNumber,

        @Min(value = 1, message = "Місткість столика має бути не менше 1")
        @Max(value = 20, message = "Місткість столика не може перевищувати 20")
        int capacity,

        @NotNull(message = "Зона столика є обов'язковою")
        TableZone zone
) {
}