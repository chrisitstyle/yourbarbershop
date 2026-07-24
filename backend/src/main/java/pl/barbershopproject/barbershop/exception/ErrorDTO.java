package pl.barbershopproject.barbershop.exception;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO representing a standardized error response.
 * <p>
 * This record is used by the GlobalExceptionHandler to return consistent
 * JSON error structures to the client whenever an exception occurs in the API.
 *
 * @param message   the user-friendly error message describing what went wrong
 * @param status    the string representation of the HTTP status (e.g., "NOT_FOUND")
 * @param timestamp the exact date and time when the error occurred
 */
@Schema(description = "Standardized API error response payload returned by GlobalExceptionHandler")
public record ErrorDTO(
        @Schema(description = "User-friendly error message describing what went wrong", example = "Selected appointment slot is already taken")
        String message,

        @Schema(description = "String representation of the HTTP status code", example = "BAD_REQUEST")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        String status,

        @Schema(description = "Exact timestamp when the error occurred", example = "2026-07-24T20:15:00")
        LocalDateTime timestamp
) {
}
