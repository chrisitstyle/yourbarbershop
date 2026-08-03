package pl.barbershopproject.barbershop.utils.testentities;

import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;

import java.math.BigDecimal;

/**
 * Utility class providing factory methods for offer-related test objects.
 *
 * <p>This class centralizes creation of {@link Offer} and {@link BookedOffer}
 * objects used in tests. It provides ready-to-use default instances and
 * builder-based creation for scenarios where selected fields need to be
 * overridden.</p>
 */
public final class OfferTestEntities {

    private static final Long DEFAULT_OFFER_ID = 1L;
    private static final String DEFAULT_OFFER_KIND = "test_kind";
    private static final BigDecimal DEFAULT_OFFER_COST =
            BigDecimal.valueOf(120);

    /**
     * Private constructor to block instantiation of utility class.
     */
    private OfferTestEntities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns an {@link Offer.OfferBuilder} pre-filled with default test data.
     *
     * @return offer builder with default values
     */
    public static Offer.OfferBuilder offerBuilder() {
        return Offer.builder()
                .idOffer(DEFAULT_OFFER_ID)
                .kind(DEFAULT_OFFER_KIND)
                .cost(DEFAULT_OFFER_COST);
    }

    /**
     * Creates an offer with the specified kind and cost.
     *
     * @param kind offer name
     * @param cost offer price
     * @return offer without an assigned identifier
     */
    public static Offer createOffer(
            String kind,
            BigDecimal cost
    ) {
        return Offer.builder()
                .kind(kind)
                .cost(cost)
                .build();
    }

    /**
     * Creates an offer with the specified identifier, kind and cost.
     *
     * @param idOffer offer identifier
     * @param kind offer name
     * @param cost offer price
     * @return offer containing the specified values
     */
    public static Offer createOffer(
            Long idOffer,
            String kind,
            BigDecimal cost
    ) {
        return Offer.builder()
                .idOffer(idOffer)
                .kind(kind)
                .cost(cost)
                .build();
    }

    /**
     * Creates an offer containing default test data.
     *
     * @return default offer
     */
    public static Offer createOffer() {
        return offerBuilder().build();
    }

    /**
     * Creates an offer without an identifier for JPA persistence tests.
     *
     * @return unsaved offer
     */
    public static Offer createUnsavedOffer() {
        return Offer.builder()
                .kind(DEFAULT_OFFER_KIND)
                .cost(BigDecimal.valueOf(150))
                .build();
    }

    /**
     * Creates a booked offer snapshot containing default test data.
     *
     * @return default booked offer snapshot
     */
    public static BookedOffer createBookedOffer() {
        return BookedOffer.from(createOffer());
    }

    /**
     * Creates a booked offer snapshot from the provided offer.
     *
     * @param offer offer to snapshot
     * @return snapshot containing the offer name and price
     */
    public static BookedOffer createBookedOffer(Offer offer) {
        return BookedOffer.from(offer);
    }

    /**
     * Creates a booked offer snapshot with the specified name and price.
     *
     * @param name historical offer name
     * @param price historical offer price
     * @return booked offer snapshot
     */
    public static BookedOffer createBookedOffer(
            String name,
            BigDecimal price
    ) {
        Offer offer = createOffer(name, price);

        return BookedOffer.from(offer);
    }
}