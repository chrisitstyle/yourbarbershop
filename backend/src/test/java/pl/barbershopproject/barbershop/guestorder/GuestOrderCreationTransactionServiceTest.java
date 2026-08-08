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
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestManager;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestResult;
import pl.barbershopproject.barbershop.idempotency.IdempotencyResolution;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;
import pl.barbershopproject.barbershop.payment.PaymentCreationResult;
import pl.barbershopproject.barbershop.payment.PaymentCreator;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static pl.barbershopproject.barbershop.utils.testentities.GuestOrderTestEntities.createGuestOrderCreationDTO;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;

@ExtendWith(MockitoExtension.class)
class GuestOrderCreationTransactionServiceTest {

    private static final Long IDEMPOTENCY_REQUEST_ID = 100L;
    private static final Long GUEST_ORDER_ID = 1L;
    private static final Long PAYMENT_ID = 10L;

    private static final String IDEMPOTENCY_KEY =
            "guest-order-transaction-test-key";

    private static final String REQUEST_HASH =
            "a".repeat(64);

    private static final String CHECKOUT_URL =
            "https://checkout.stripe.com/c/pay/cs_test_123";

    private static final ZoneId TEST_ZONE =
            ZoneId.of("Europe/Warsaw");

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

    @Mock
    private IdempotencyRequestManager idempotencyRequestManager;

    private GuestOrderCreationTransactionService guestOrderCreationTransactionService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TEST_INSTANT, TEST_ZONE);

        guestOrderCreationTransactionService = new GuestOrderCreationTransactionService(
                        guestOrderRepository,
                        offerQuery,
                        appointmentReservation,
                        paymentCreator,
                        guestOrderEvents,
                        idempotencyRequestManager,
                        clock);
    }

    @Test
    void create_ShouldPersistGuestOrderCreatePaymentAndReturnTransactionResult() {
        // given
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
                        null,
                        offer.getCost(),
                        "PLN",
                        offer.getKind());

        PaymentCreationResult paymentCreationResult = new PaymentCreationResult(
                        payment,
                        checkoutRequest);

        givenNewIdempotencyRequest();

        when(offerQuery.getRequiredOffer(
                guestOrderCreationDTO.idOffer()
        )).thenReturn(offer);

        givenRepositorySavesGuestOrderWithId();

        when(paymentCreator.createForGuestOrder(
                any(GuestOrder.class),
                eq(guestOrderCreationDTO.paymentMethod())
        )).thenReturn(paymentCreationResult);

        // when
        GuestOrderCreationTransactionResult result = guestOrderCreationTransactionService.create(
                        guestOrderCreationDTO,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH);

        // then
        assertThat(result.idempotencyRequestId())
                .isEqualTo(IDEMPOTENCY_REQUEST_ID);

        assertThat(result.resolution())
                .isEqualTo(IdempotencyResolution.RESOURCE_CREATED);

        assertThat(result.guestOrderId())
                .isEqualTo(GUEST_ORDER_ID);

        assertThat(result.checkoutRequest())
                .isSameAs(checkoutRequest);

        ArgumentCaptor<GuestOrder> guestOrderCaptor =
                ArgumentCaptor.forClass(GuestOrder.class);

        verify(guestOrderRepository)
                .save(guestOrderCaptor.capture());

        GuestOrder savedGuestOrder = guestOrderCaptor.getValue();

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

        verify(idempotencyRequestManager)
                .startGuestOrderCreation(
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH);

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

        verify(idempotencyRequestManager)
                .markResourceCreated(
                        IDEMPOTENCY_REQUEST_ID,
                        GUEST_ORDER_ID,
                        checkoutRequest);
    }

    @Test
    void create_ShouldReturnStoredResourceWithoutCreatingAnotherGuestOrder() {
        // given
        GuestOrderCreationDTO guestOrderCreationDTO =
                createGuestOrderCreationDTO();

        PaymentCheckoutRequest checkoutRequest =
                createCheckoutRequest(guestOrderCreationDTO);

        when(idempotencyRequestManager.startGuestOrderCreation(
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(new IdempotencyRequestResult(
                IDEMPOTENCY_REQUEST_ID,
                IdempotencyResolution.RESOURCE_CREATED,
                GUEST_ORDER_ID,
                checkoutRequest,
                null));

        // when
        GuestOrderCreationTransactionResult result =
                guestOrderCreationTransactionService.create(
                        guestOrderCreationDTO,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH);

        // then
        assertThat(result.idempotencyRequestId())
                .isEqualTo(IDEMPOTENCY_REQUEST_ID);

        assertThat(result.resolution())
                .isEqualTo(IdempotencyResolution.RESOURCE_CREATED);

        assertThat(result.guestOrderId())
                .isEqualTo(GUEST_ORDER_ID);

        assertThat(result.checkoutRequest())
                .isEqualTo(checkoutRequest);

        verifyNoInteractions(
                offerQuery,
                appointmentReservation,
                guestOrderRepository,
                paymentCreator,
                guestOrderEvents);

        verify(idempotencyRequestManager, never())
                .markResourceCreated(
                        any(), any(), any());
    }

    @Test
    void create_ShouldReturnCompletedResultWithoutCreatingAnotherGuestOrder() {
        // given
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

        PaymentCheckoutRequest checkoutRequest =
                createCheckoutRequest(guestOrderCreationDTO);

        when(idempotencyRequestManager.startGuestOrderCreation(
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(new IdempotencyRequestResult(
                IDEMPOTENCY_REQUEST_ID,
                IdempotencyResolution.COMPLETED,
                GUEST_ORDER_ID,
                checkoutRequest,
                CHECKOUT_URL
        ));

        // when
        GuestOrderCreationTransactionResult result = guestOrderCreationTransactionService.create(
                        guestOrderCreationDTO,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH
                );

        // then
        assertThat(result.idempotencyRequestId())
                .isEqualTo(IDEMPOTENCY_REQUEST_ID);

        assertThat(result.resolution())
                .isEqualTo(IdempotencyResolution.COMPLETED);

        assertThat(result.guestOrderId())
                .isEqualTo(GUEST_ORDER_ID);

        assertThat(result.checkoutRequest())
                .isEqualTo(checkoutRequest);

        assertThat(result.checkoutUrl())
                .isEqualTo(CHECKOUT_URL);

        verifyNoInteractions(
                offerQuery,
                appointmentReservation,
                guestOrderRepository,
                paymentCreator,
                guestOrderEvents
        );

        verify(idempotencyRequestManager, never())
                .markResourceCreated(any(), any(), any());
    }

    @Test
    void create_ShouldReturnInProgressWithoutCreatingGuestOrder() {
        // given
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

        when(idempotencyRequestManager.startGuestOrderCreation(
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(new IdempotencyRequestResult(
                IDEMPOTENCY_REQUEST_ID,
                IdempotencyResolution.IN_PROGRESS,
                null,
                null,
                null));

        // when
        GuestOrderCreationTransactionResult result = guestOrderCreationTransactionService.create(
                        guestOrderCreationDTO,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH);

        // then
        assertThat(result.idempotencyRequestId())
                .isEqualTo(IDEMPOTENCY_REQUEST_ID);

        assertThat(result.resolution())
                .isEqualTo(IdempotencyResolution.IN_PROGRESS);

        assertThat(result.guestOrderId()).isNull();
        assertThat(result.checkoutRequest()).isNull();
        assertThat(result.checkoutUrl()).isNull();

        verifyNoInteractions(
                offerQuery,
                appointmentReservation,
                guestOrderRepository,
                paymentCreator,
                guestOrderEvents);

        verify(idempotencyRequestManager, never())
                .markResourceCreated(any(), any(), any());
    }

    @Test
    void create_ShouldNotReserveSlot_WhenOfferDoesNotExist() {
        // given
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

        givenNewIdempotencyRequest();

        when(offerQuery.getRequiredOffer(
                guestOrderCreationDTO.idOffer()
        )).thenThrow(new NoSuchElementException(
                "Oferta o ID: "
                        + guestOrderCreationDTO.idOffer()
                        + " nie istnieje"
        ));

        // when then
        assertThatThrownBy(() -> guestOrderCreationTransactionService.create(
                        guestOrderCreationDTO,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH
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

        verify(idempotencyRequestManager, never())
                .markResourceCreated(any(), any(), any());
    }

    @Test
    void create_ShouldNotPersistGuestOrder_WhenAppointmentSlotIsTaken() {
        // given
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

        Offer offer = createOffer();

        givenNewIdempotencyRequest();

        when(offerQuery.getRequiredOffer(
                guestOrderCreationDTO.idOffer()
        )).thenReturn(offer);

        doThrow(new AppointmentSlotTakenException(
                guestOrderCreationDTO.visitDate()
        ))
                .when(appointmentReservation)
                .reserveSlot(guestOrderCreationDTO.visitDate());

        // when then
        assertThatThrownBy(() -> guestOrderCreationTransactionService.create(
                        guestOrderCreationDTO,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH
                )
        ).isInstanceOf(AppointmentSlotTakenException.class);

        verify(offerQuery)
                .getRequiredOffer(guestOrderCreationDTO.idOffer());

        verify(appointmentReservation)
                .reserveSlot(guestOrderCreationDTO.visitDate());

        verifyNoInteractions(
                guestOrderRepository,
                paymentCreator,
                guestOrderEvents);

        verify(idempotencyRequestManager, never())
                .markResourceCreated(any(), any(), any());
    }

    @Test
    void create_ShouldNotPublishEvent_WhenPaymentCreationFails() {
        // given
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

        Offer offer = createOffer();

        givenNewIdempotencyRequest();

        when(offerQuery.getRequiredOffer(
                guestOrderCreationDTO.idOffer()
        )).thenReturn(offer);

        givenRepositorySavesGuestOrderWithId();

        when(paymentCreator.createForGuestOrder(
                any(GuestOrder.class),
                eq(guestOrderCreationDTO.paymentMethod())
        )).thenThrow(new IllegalStateException(
                "Nie udało się utworzyć płatności"));

        // when then
        assertThatThrownBy(() -> guestOrderCreationTransactionService.create(
                        guestOrderCreationDTO,
                        IDEMPOTENCY_KEY,
                        REQUEST_HASH
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

        verify(guestOrderEvents, never())
                .created(
                        any(GuestOrder.class),
                        any(Payment.class));

        verify(idempotencyRequestManager, never())
                .markResourceCreated(any(), any(), any());
    }

    private void givenNewIdempotencyRequest() {
        when(idempotencyRequestManager.startGuestOrderCreation(
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(new IdempotencyRequestResult(
                IDEMPOTENCY_REQUEST_ID,
                IdempotencyResolution.NEW,
                null,
                null,
                null
        ));
    }

    private PaymentCheckoutRequest createCheckoutRequest(
            GuestOrderCreationDTO guestOrderCreationDTO
    ) {
        Offer offer = createOffer();

        return new PaymentCheckoutRequest(
                PAYMENT_ID,
                guestOrderCreationDTO.paymentMethod(),
                PaymentStatus.NIE_WYMAGANA,
                null,
                offer.getCost(),
                "PLN",
                offer.getKind()
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