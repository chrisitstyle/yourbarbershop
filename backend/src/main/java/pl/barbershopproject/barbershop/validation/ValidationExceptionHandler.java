package pl.barbershopproject.barbershop.validation;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Set<Violation>> handleConstraintValidationException(
            ConstraintViolationException exception
    ) {
        Set<Violation> violations = exception.getConstraintViolations()
                .stream()
                .map(violation -> new Violation(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .collect(Collectors.toSet());

        return ResponseEntity.badRequest().body(violations);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Set<Violation>> handleMethodValidationException(
            HandlerMethodValidationException exception
    ) {
        Set<Violation> violations = exception
                .getParameterValidationResults()
                .stream()
                .flatMap(result -> result.getResolvableErrors()
                        .stream()
                        .map(error -> new Violation(
                                resolveFieldName(result),
                                Objects.requireNonNullElse(
                                        error.getDefaultMessage(),
                                        "Nieprawidłowa wartość"
                                )
                        )))
                .collect(Collectors.toSet());

        return ResponseEntity.badRequest().body(violations);
    }

    private String resolveFieldName(ParameterValidationResult result) {
        RequestHeader requestHeader = result
                .getMethodParameter()
                .getParameterAnnotation(RequestHeader.class);

        if (requestHeader != null) {
            String headerName = !requestHeader.name().isBlank()
                    ? requestHeader.name()
                    : requestHeader.value();

            if (!headerName.isBlank()) {
                return headerName;
            }
        }

        return Objects.requireNonNullElse(
                result.getMethodParameter().getParameterName(),
                "request"
        );
    }
}