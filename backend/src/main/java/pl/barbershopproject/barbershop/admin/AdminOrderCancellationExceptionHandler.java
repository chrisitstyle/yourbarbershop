package pl.barbershopproject.barbershop.admin;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.barbershopproject.barbershop.exception.OrderStatusChangeNotAllowedException;

import java.util.NoSuchElementException;

/**
 * Maps administrative cancellation errors to appropriate HTTP responses.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = AdminOrderCancellationController.class)
public class AdminOrderCancellationExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleReservationNotFound(NoSuchElementException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage());
    }

    @ExceptionHandler(OrderStatusChangeNotAllowedException.class)
    public ProblemDetail handleCancellationConflict(
            OrderStatusChangeNotAllowedException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage());
    }
}
