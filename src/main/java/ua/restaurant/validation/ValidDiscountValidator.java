package ua.restaurant.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ua.restaurant.model.RestaurantOrder;

public class ValidDiscountValidator implements ConstraintValidator<ValidDiscount, RestaurantOrder> {

    @Override
    public boolean isValid(RestaurantOrder order, ConstraintValidatorContext context) {
        if (order == null) {
            return true;
        }
        return order.getDiscountAmount() <= order.getTotalAmount();
    }
}