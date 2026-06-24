package pl.barbershopproject.barbershop.utils;

/**
 * Utility class providing static factory methods for creating test entities (User, Offer, Order, GuestOrder) and DTOs.
 * <p>
 * Useful for building entities with minimal boilerplate in JPA/Spring Data test scenarios.
 * It provides both complete object creation and Builders pre-filled with default test data.
 *
 * @deprecated Use dedicated test entity factories instead, such as:
 * {@code UserTestEntities}, {@code OfferTestEntities}, {@code OrderTestEntities}
 * and {@code GuestOrderTestEntities}.
 */
@Deprecated(forRemoval = true)
public class TestEntities {

    /**
     * Private constructor to block instantiation of utility class.
     */
    private TestEntities() {
    }
}