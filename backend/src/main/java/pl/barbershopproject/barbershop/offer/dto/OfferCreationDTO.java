package pl.barbershopproject.barbershop.offer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Payload for creating a new service offer in the barbershop catalog")
public record OfferCreationDTO(
        @Schema(description = "Name/type of the barber service", example = "Strzyżenie męskie + Broda")
        @NotBlank(message = "Rodzaj jest wymagany")
        String kind,

        @Schema(description = "Cost of the service in PLN", example = "120.00")
        @NotNull(message = "Cena jest wymagana")
        @DecimalMin(value = "0.01", message = "Cena musi byc wieksza niz 0")
        BigDecimal cost
) {
}
