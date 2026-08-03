package pl.barbershopproject.barbershop.offer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Represents offer details displayed as part of an existing order.
 *
 * <p>The identifier refers to the offer currently assigned to the order,
 * while the name and price come from the historical snapshot captured
 * when the offer was booked.</p>
 *
 * @param idOffer identifier of the offer assigned to the order
 * @param kind historical name of the booked offer
 * @param cost historical price of the booked offer
 */
@Schema(description = "Historical details of the offer booked for an order")
public record BookedOfferDTO(

        @Schema(description = "Offer identifier", example = "1")
        Long idOffer,

        @Schema(description = "Booked offer name", example = "Strzyżenie")
        String kind,

        @Schema(description = "Booked offer price", example = "80.00")
        BigDecimal cost

) implements Serializable {
}
