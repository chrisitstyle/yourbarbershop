package pl.barbershopproject.barbershop.offer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OfferCreationDTO(
        @NotBlank(message = "Rodzaj jest wymagany")
        String kind,

        @NotNull(message = "Cena jest wymagana")
        @DecimalMin(value = "0.01", message = "Cena musi byc wieksza niz 0")
        BigDecimal cost
) {
}
