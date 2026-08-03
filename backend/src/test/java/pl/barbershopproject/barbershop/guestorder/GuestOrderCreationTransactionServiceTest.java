package pl.barbershopproject.barbershop.guestorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.exception.AppointmentSlotTakenException;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.payment.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static pl.barbershopproject.barbershop.utils.testentities.GuestOrderTestEntities.createGuestOrderCreationDTO;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;

@ExtendWith(MockitoExtension.class)
class GuestOrderCreationTransactionServiceTest {

    private static final Long GUEST_ORDER_ID = 1L;
    private static final Long PAYMENT_ID = 10L;
    private static final ZoneId TEST_ZONE = ZoneId.of("Europe/Warsaw");
    private static final Instant TEST_INSTANT =
            Instant.parse("2026-01-16T12:00:00Z");

    @Mock
    private GuestOrderRepository guestOrderRepository;
    @Mock
    private OfferQuery offerQuery;
    @Mock
    private AppointmentReservation appointmentReservation;
    @Mock
    private PaymentCreator paymentCreator;
    @Mock
    private GuestOrderEvents guestOrderEvents;

    private GuestOrderCreationTransactionService guestOrderCreationTransactionService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TEST_INSTANT, TEST_ZONE);

        guestOrderCreationTransactionService = new GuestOrderCreationTransactionService(guestOrderRepository,
                        offerQuery,
                        appointmentReservation,
                        paymentCreator,
                        guestOrderEvents,
                        clock);
    }

    @Test
    void create_ShouldPersistGuestOrderCreatePaymentAndReturnTransactionResult() {
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

        Offer offer = createOffer();

        Payment payment = Payment.builder()
                .idPayment(PAYMENT_ID)
                .paymentMethod(guestOrderCreationDTO.paymentMethod())
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(offer.getCost())
                .currency("PLN")
                .build();

        PaymentCheckoutRequest checkoutRequest = new PaymentCheckoutRequest(
                        PAYMENT_ID,
                        guestOrderCreationDTO.paymentMethod(),
                        PaymentStatus.NIE_WYMAGANA,
                        offer.getCost(),
                        "PLN",
                        offer.getKind()
                );

        PaymentCreationResult paymentCreationResult = new PaymentCreationResult(
                        payment,
                        checkoutRequest
                );

        when(offerQuery.getRequiredOffer(
                guestOrderCreationDTO.idOffer()
        )).thenReturn(offer);

        givenRepositorySavesGuestOrderWithId();

        when(paymentCreator.createForGuestOrder(
                any(GuestOrder.class),
                eq(guestOrderCreationDTO.paymentMethod())
        )).thenReturn(paymentCreationResult);

        GuestOrderCreationTransactionResult result =
                guestOrderCreationTransactionService.create(
                        guestOrderCreationDTO
                );

        assertThat(result.guestOrderId())
                .isEqualTo(GUEST_ORDER_ID);

        assertThat(result.checkoutRequest())
                .isSameAs(checkoutRequest);

        ArgumentCaptor<GuestOrder> guestOrderCaptor =
                ArgumentCaptor.forClass(GuestOrder.class);

        verify(guestOrderRepository)
                .save(guestOrderCaptor.capture());

        GuestOrder savedGuestOrder =
                guestOrderCaptor.getValue();

        assertThat(savedGuestOrder.getIdGuestOrder())
                .isEqualTo(GUEST_ORDER_ID);

        assertThat(savedGuestOrder.getOffer())
                .isSameAs(offer);

        assertThat(savedGuestOrder.getFirstname())
                .isEqualTo(guestOrderCreationDTO.firstname());

        assertThat(savedGuestOrder.getLastname())
                .isEqualTo(guestOrderCreationDTO.lastname());

        assertThat(savedGuestOrder.getPhonenumber())
                .isEqualTo(guestOrderCreationDTO.phonenumber());

        assertThat(savedGuestOrder.getEmail())
                .isEqualTo(guestOrderCreationDTO.email());

        assertThat(savedGuestOrder.getVisitDate())
                .isEqualTo(guestOrderCreationDTO.visitDate());

        assertThat(savedGuestOrder.getBookedOffer())
                .isNotNull();

        assertThat(savedGuestOrder.getBookedOffer().getName())
                .isEqualTo(offer.getKind());

        assertThat(savedGuestOrder.getBookedOffer().getPrice())
                .isEqualByComparingTo(offer.getCost());

        verify(offerQuery)
                .getRequiredOffer(guestOrderCreationDTO.idOffer());

        verify(appointmentReservation)
                .reserveSlot(guestOrderCreationDTO.visitDate());

        verify(paymentCreator).createForGuestOrder(
                savedGuestOrder,
                guestOrderCreationDTO.paymentMethod()
        );

        verify(guestOrderEvents)
                .created(savedGuestOrder, payment);
    }

    @Test
    void create_ShouldNotReserveSlot_WhenOfferDoesNotExist() {
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

        when(offerQuery.getRequiredOffer(
                guestOrderCreationDTO.idOffer()
        )).thenThrow(new NoSuchElementException(
                "Oferta o ID: "
                        + guestOrderCreationDTO.idOffer()
                        + " nie istnieje"
        ));

        assertThatThrownBy(() ->
                guestOrderCreationTransactionService.create(
                        guestOrderCreationDTO
                )
        )
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage(
                        "Oferta o ID: "
                                + guestOrderCreationDTO.idOffer()
                                + " nie istnieje"
                );

        verify(offerQuery)
                .getRequiredOffer(guestOrderCreationDTO.idOffer());

        verifyNoInteractions(
                appointmentReservation,
                guestOrderRepository,
                paymentCreator,
                guestOrderEvents
        );
    }

    @Test
    void create_ShouldNotPersistGuestOrder_WhenAppointmentSlotIsTaken() {
        GuestOrderCreationDTO guestOrderCreationDTO =
                createGuestOrderCreationDTO();

        Offer offer = createOffer();

        when(offerQuery.getRequiredOffer(
                guestOrderCreationDTO.idOffer()
        )).thenReturn(offer);

        doThrow(new AppointmentSlotTakenException(
                guestOrderCreationDTO.visitDate()
        ))
                .when(appointmentReservation)
                .reserveSlot(guestOrderCreationDTO.visitDate());

        assertThatThrownBy(() ->
                guestOrderCreationTransactionService.create(
                        guestOrderCreationDTO
                )
        ).isInstanceOf(AppointmentSlotTakenException.class);

        verify(offerQuery)
                .getRequiredOffer(guestOrderCreationDTO.idOffer());

        verify(appointmentReservation)
                .reserveSlot(guestOrderCreationDTO.visitDate());

        verifyNoInteractions(
                guestOrderRepository,
                paymentCreator,
                guestOrderEvents
        );
    }

    @Test
    void create_ShouldNotPublishEvent_WhenPaymentCreationFails() {
        GuestOrderCreationDTO guestOrderCreationDTO =
                createGuestOrderCreationDTO();

        Offer offer = createOffer();

        when(offerQuery.getRequiredOffer(
                guestOrderCreationDTO.idOffer()
        )).thenReturn(offer);

        givenRepositorySavesGuestOrderWithId();

        when(paymentCreator.createForGuestOrder(
                any(GuestOrder.class),
                eq(guestOrderCreationDTO.paymentMethod())
        )).thenThrow(new IllegalStateException(
                "Nie udało się utworzyć płatności"
        ));

        assertThatThrownBy(() ->
                guestOrderCreationTransactionService.create(
                        guestOrderCreationDTO
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Nie udało się utworzyć płatności");

        verify(appointmentReservation)
                .reserveSlot(guestOrderCreationDTO.visitDate());

        verify(guestOrderRepository)
                .save(any(GuestOrder.class));

        verify(paymentCreator).createForGuestOrder(
                any(GuestOrder.class),
                eq(guestOrderCreationDTO.paymentMethod())
        );

        verify(guestOrderEvents, never()).created(
                any(GuestOrder.class),
                any(Payment.class)
        );
    }

    private void givenRepositorySavesGuestOrderWithId() {
        when(guestOrderRepository.save(any(GuestOrder.class)))
                .thenAnswer(invocation -> {
                    GuestOrder savedGuestOrder =
                            invocation.getArgument(0);

                    savedGuestOrder.setIdGuestOrder(
                            GUEST_ORDER_ID
                    );

                    return savedGuestOrder;
                });
    }
}
