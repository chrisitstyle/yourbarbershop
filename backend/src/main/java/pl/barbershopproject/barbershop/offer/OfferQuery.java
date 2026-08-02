package pl.barbershopproject.barbershop.offer;

/**
 * Provides read access to offers required by other application modules.
 *
 * <p>This interface prevents services from depending directly on
 * {@link OfferRepository} and exposes only the operation required
 * to retrieve an existing offer.</p>
 */
public interface OfferQuery {

    /**
     * Returns the offer with the specified identifier.
     *
     * @param idOffer identifier of the offer
     * @return offer matching the provided identifier
     * @throws java.util.NoSuchElementException if the offer does not exist
     */
    Offer getRequiredOffer(Long idOffer);
}