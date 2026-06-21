package pl.barbershopproject.barbershop.utils.testentities;

import pl.barbershopproject.barbershop.offer.Offer;

import java.math.BigDecimal;

/**
 * Utility class providing factory methods for offer-related test objects.
 * <p>
 * This class centralizes creation of {@link Offer} entities used in tests.
 * It provides both ready-to-use default offers and builder-based creation
 * for scenarios where only selected fields need to be overridden.
 * </p>
 */
public final class OfferTestEntities {

    /**
     * Private constructor to block instantiation of utility class.
     */
    private OfferTestEntities() {
    }

    /**
     * Returns an OfferBuilder pre-filled with default test data.
     *
     * @return OfferBuilder with default values set
     */
    public static Offer.OfferBuilder offerBuilder() {
        return Offer.builder()
                .idOffer(1L)
                .kind("test_kind")
                .cost(BigDecimal.valueOf(120));
    }

    /**
     * Creates an Offer instance with specified kind and cost.
     *
     * @param kind the type/kind of the offer
     * @param cost the cost of the offer
     * @return new Offer instance
     */
    public static Offer createOffer(String kind, BigDecimal cost) {

        return Offer.builder()
                .kind(kind)
                .cost(cost)
                .build();
    }

    /**
     * Creates an Offer instance with specified ID, kind, and cost.
     *
     * @param idOffer the ID of the offer
     * @param kind    the type/kind of the offer
     * @param cost    the cost of the offer
     * @return new Offer instance
     */
    public static Offer createOffer(Long idOffer, String kind, BigDecimal cost) {

        return Offer.builder()
                .idOffer(idOffer)
                .kind(kind)
                .cost(cost)
                .build();
    }

    /**
     * Creates a default Offer instance.
     */
    public static Offer createOffer() {
        return offerBuilder().build();
    }

    /**
     * Creates an Offer instance specifically for JPA testing (NO ID set).
     * Hibernate requires ID to be null for a successful INSERT.
     */
    public static Offer createUnsavedOffer() {
        return Offer.builder()
                .kind("test_kind")
                .cost(BigDecimal.valueOf(150))
                .build();
    }
}
