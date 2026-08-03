package pl.barbershopproject.barbershop.utils.testentities;

import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationResponseDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderUpdateRequestDTO;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.dto.BookedOfferDTO;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.utils.Status;

import java.time.LocalDateTime;
import java.time.Month;

import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createBookedOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;

/**
 * Utility class providing factory methods for guest order-related test objects.
 * <p>
 * This class helps create {@link GuestOrder} entities and related DTOs with
 * consistent default test data. It is useful in unit, integration and repository
 * tests where guest order setup would otherwise be repeated.
 * </p>
 */
public final class GuestOrderTestEntities {

    /**
     * Private constructor to block instantiation of utility class.
     */
    private GuestOrderTestEntities() {
    }

    /**
     * Returns a GuestOrderBuilder pre-filled with default test data.
     */
    public static GuestOrder.GuestOrderBuilder guestOrderBuilder() {
        return GuestOrder.builder()
                .idGuestOrder(1L)
                .firstname("GuestJohn")
                .lastname("GuestDoe")
                .phonenumber("123456789")
                .email("guestjohndoe@example.com")
                .offer(createOffer())
                .bookedOffer(createBookedOffer(createOffer()))
                .orderDate(LocalDateTime.of(2026, Month.JANUARY, 16, 15, 0))
                .visitDate(LocalDateTime.of(2026, Month.OCTOBER, 17, 17, 0))
                .status(Status.NOWE);
    }

    /**
     * Creates a default GuestOrder instance.
     */
    public static GuestOrder createGuestOrder() {
        return guestOrderBuilder().build();
    }

    /**
     * Creates a GuestOrder instance for testing purposes.
     *
     * @param firstname   guest's first name
     * @param lastname    guest's last name
     * @param phonenumber guest's phone number
     * @param email       guest's email address
     * @param offer       offer associated with the guest order
     * @param orderDate   date the guest order was placed
     * @param visitDate   scheduled visit date
     * @param status      guest order status
     * @return new GuestOrder instance
     */
    public static GuestOrder createGuestOrder(String firstname, String lastname, String phonenumber,
                                              String email, Offer offer, LocalDateTime orderDate,
                                              LocalDateTime visitDate, Status status) {

        return GuestOrder.builder()
                .firstname(firstname)
                .lastname(lastname)
                .phonenumber(phonenumber)
                .email(email)
                .offer(offer)
                .orderDate(orderDate)
                .visitDate(visitDate)
                .status(status)
                .build();

    }

    /**
     * Creates a GuestOrder instance specifically for JPA testing (NO ID set).
     * Hibernate requires ID to be null for a successful INSERT.
     */
    public static GuestOrder createUnsavedGuestOrder() {
        Offer offer = createOffer();
        return GuestOrder.builder()
                .firstname("GuestJohn")
                .lastname("GuestDoe")
                .phonenumber("123456789")
                .email("guestjohndoe@example.com")
                .offer(offer)
                .orderDate(LocalDateTime.of(2026, Month.JANUARY, 16, 15, 0))
                .visitDate(LocalDateTime.of(2026, Month.OCTOBER, 17, 17, 0))
                .status(Status.NOWE)
                .build();
    }

    /**
     * Creates a GuestOrderCreationDTO with default test data.
     * <p>
     * Useful for testing guest order creation endpoint and service.
     *
     * @return GuestOrderCreationDTO instance
     */
    public static GuestOrderCreationDTO createGuestOrderCreationDTO() {
        return new GuestOrderCreationDTO(
                "GuestJohn",
                "GuestDoe",
                "123456789",
                "guestjohndoe@example.com",
                1L,
                LocalDateTime.of(2026, Month.OCTOBER, 17, 17, 0),
                PaymentMethod.GOTOWKA
        );
    }

    /**
     * Creates a GuestOrderUpdateRequestDTO with default updated test data.
     * <p>
     * Useful for testing guest order update endpoint and service.
     *
     * @return GuestOrderUpdateRequestDTO instance
     */
    public static GuestOrderUpdateRequestDTO createGuestOrderUpdateRequestDTO() {
        return new GuestOrderUpdateRequestDTO(
                "updated_firstname",
                "GuestDoe",
                "123456789",
                "guestjohn@example.com",
                1L,
                LocalDateTime.of(2026, Month.JANUARY, 16, 12, 0),
                Status.NOWE
        );
    }

    /**
     * Creates a GuestOrderUpdateRequestDTO with null status.
     * <p>
     * Useful for testing update logic that keeps the current guest order status.
     *
     * @return GuestOrderUpdateRequestDTO instance
     */
    public static GuestOrderUpdateRequestDTO createGuestOrderUpdateRequestDTOWithNullStatus() {
        return new GuestOrderUpdateRequestDTO(
                "updated_firstname",
                "GuestDoe",
                "123456789",
                "guestjohn@example.com",
                1L,
                LocalDateTime.of(2026, Month.JANUARY, 16, 12, 0),
                null
        );
    }

    public static GuestOrderCreationResponseDTO createGuestOrderCreationResponseDTO() {
        return new GuestOrderCreationResponseDTO(
                1L,
                PaymentMethod.GOTOWKA,
                PaymentStatus.NIE_WYMAGANA,
                null
        );
    }

    public static GuestOrderDTO createGuestOrderDTO() {
        Offer offer = createOffer();

        BookedOfferDTO bookedOfferDTO = new BookedOfferDTO(
                offer.getIdOffer(),
                offer.getKind(),
                offer.getCost()
        );

        return new GuestOrderDTO(
                1L,
                "GuestJohn",
                "GuestDoe",
                "123456789",
                "guestjohndoe@example.com",
                bookedOfferDTO,
                LocalDateTime.of(2026, Month.JANUARY, 16, 15, 0),
                LocalDateTime.of(2026, Month.OCTOBER, 17, 17, 0),
                Status.NOWE,
                PaymentMethod.GOTOWKA,
                PaymentStatus.NIE_WYMAGANA
        );
    }

}
