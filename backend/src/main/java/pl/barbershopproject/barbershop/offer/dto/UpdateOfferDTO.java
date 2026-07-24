package pl.barbershopproject.barbershop.offer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Payload for updating an existing service offer")
public record UpdateOfferDTO(
        @Schema(description = "Updated service name/description", example = "Strzyżenie brody + Combo")
        @NotBlank(message = "Rodzaj jest wymagany")
        String kind,

        @Schema(description = "Updated price in PLN", example = "150.00")
        @NotNull(message = "Cena jest wymagana")
        @DecimalMin(value = "0.01", message = "Cena musi byc wieksza niz 0")
        BigDecimal cost
) {
}
