package pl.barbershopproject.barbershop.idempotency;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates a client-generated idempotency key used to identify
 * an order creation operation.
 */
@Documented
@NotBlank(message = "Idempotency-Key nie może być pusty")
@Size(
        max = 255,
        message = "Idempotency-Key nie może przekraczać 255 znaków"
)
@Constraint(validatedBy = {})
@Target({
        ElementType.PARAMETER,
        ElementType.FIELD,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIdempotencyKey {

    String message() default "Nieprawidłowy Idempotency-Key";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
