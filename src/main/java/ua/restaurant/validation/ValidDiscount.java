package ua.restaurant.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ValidDiscountValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDiscount {

    String message() default "Знижка не може перевищувати загальну суму замовлення";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}