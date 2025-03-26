package pl.barbershopproject.barbershop.validation;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Set;
import java.util.stream.Collectors;
@RestControllerAdvice
public class ValidationExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?>
    handleConstraintValidationException(ConstraintViolationException e) {
        Set<Violation> violations = e.getConstraintViolations().stream()
                .map(v -> new Violation(v.getPropertyPath().toString(), v.getMessage()))
                .collect(Collectors.toSet());
        return ResponseEntity.badRequest().body(violations);
    }
}
