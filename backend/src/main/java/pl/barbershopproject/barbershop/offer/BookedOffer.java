package pl.barbershopproject.barbershop.offer;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a historical snapshot of an offer assigned to an order.
 *
 * <p>The snapshot preserves the offer name and price from the moment
 * the order was created or its offer was changed before payment.</p>
 */
@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookedOffer implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(
            name = "booked_offer_name",
            nullable = false,
            length = 255
    )
    private String name;

    @Column(
            name = "booked_offer_price",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal price;

    /**
     * Creates a historical snapshot from the provided offer.
     *
     * @param offer offer to snapshot
     * @return snapshot containing the offer name and price
     * @throws NullPointerException if the offer, its name or price is null
     */
    public static BookedOffer from(Offer offer) {
        Objects.requireNonNull(offer, "Offer nie może być null");

        String offerName = Objects.requireNonNull(
                offer.getKind(),
                "Nazwa oferty nie może być null"
        );

        BigDecimal offerPrice = Objects.requireNonNull(
                offer.getCost(),
                "Cena oferty nie może być null"
        );

        return new BookedOffer(offerName, offerPrice);
    }
}
