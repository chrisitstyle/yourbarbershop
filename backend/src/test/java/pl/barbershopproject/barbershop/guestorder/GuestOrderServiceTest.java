package pl.barbershopproject.barbershop.guestorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.exception.IdempotencyConflictException;
import pl.barbershopproject.barbershop.exception.MissingPaymentException;
import pl.barbershopproject.barbershop.exception.OrderOfferChangeNotAllowedException;
import pl.barbershopproject.barbershop.exception.OrderStatusChangeNotAllowedException;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationResponseDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderUpdateRequestDTO;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestHasher;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestManager;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.payment.*;
import pl.barbershopproject.barbershop.utils.OrderModificationPolicy;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static pl.barbershopproject.barbershop.utils.testentities.GuestOrderTestEntities.*;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createBookedOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;

@ExtendWith(MockitoExtension.class)
class GuestOrderServiceTest {

    private static final Long IDEMPOTENCY_REQUEST_ID = 100L;
    private static final String IDEMPOTENCY_KEY = "guest-order-service-test-key";
    private static final String REQUEST_HASH = "a".repeat(64);
    private static final String STRIPE_CHECKOUT_IDEMPOTENCY_KEY = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private GuestOrderRepository guestOrderRepository;
    @Mock
    private OfferQuery offerQuery;
    @Mock
    private AppointmentReservation appointmentReservation;
    @Mock
    private PaymentOfferUpdater paymentOfferUpdater;
    @Mock
    private GuestOrderEvents guestOrderEvents;
    @Mock
    private GuestOrderCreationTransaction guestOrderCreationTransaction;
    @Mock
    private PaymentCheckout paymentCheckout;

    @Mock
    private IdempotencyRequestHasher idempotencyRequestHasher;
    @Mock
    private IdempotencyRequestManager idempotencyRequestManager;
    @Mock
    private OrderModificationPolicy orderModificationPolicy;

    @InjectMocks
    private GuestOrderService guestOrderService;

    private GuestOrder guestOrder;
    private Offer offer;
    private Payment defaultPayment;

    @BeforeEach
    void setUp() {
        offer = createOffer();

        defaultPayment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(offer.getCost())
                .build();

        guestOrder = guestOrderBuilder()
                .idGuestOrder(1L)
                .offer(offer)
                .bookedOffer(createBookedOffer(offer))
                .orderStatus(OrderStatus.NOWE)
                .payment(defaultPayment)
                .build();
    }

    @Test
    void addGuestOrder_ShouldReturnResponseWithoutCheckoutUrl_ForOfflinePayment() {
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

        PaymentCheckoutRequest checkoutRequest = new PaymentCheckoutRequest(
                10L,
                PaymentMethod.GOTOWKA,
                PaymentStatus.NIE_WYMAGANA,
                null,
                offer.getCost(),
                "PLN",
                offer.getKind()
        );

        GuestOrderCreationTransactionResult transactionResult = GuestOrderCreationTransactionResult.resourceCreated(
                IDEMPOTENCY_REQUEST_ID,
                1L,
                checkoutRequest
        );

        givenRequestHash(guestOrderCreationDTO);

        when(guestOrderCreationTransaction.create(
                guestOrderCreationDTO,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(transactionResult);

        when(paymentCheckout.createCheckoutIfRequired(checkoutRequest))
                .thenReturn(null);

        GuestOrderCreationResponseDTO result =
                guestOrderService.addGuestOrder(
                        guestOrderCreationDTO,
                        IDEMPOTENCY_KEY
                );

        assertNotNull(result);
        assertEquals(1L, result.guestOrderId());
        assertEquals(PaymentMethod.GOTOWKA, result.paymentMethod());
        assertEquals(PaymentStatus.NIE_WYMAGANA, result.paymentStatus());
        assertNull(result.checkoutUrl());

        verify(guestOrderCreationTransaction).create(
                guestOrderCreationDTO,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        );

        verify(paymentCheckout)
                .createCheckoutIfRequired(checkoutRequest);

        verify(idempotencyRequestManager).markCompleted(IDEMPOTENCY_REQUEST_ID, null);
    }

    @Test
    void addGuestOrder_ShouldReturnCheckoutUrl_ForOnlinePayment() {
        GuestOrderCreationDTO guestOrderCreationDTO =
                createGuestOrderCreationDTO();

        PaymentCheckoutRequest checkoutRequest = new PaymentCheckoutRequest(
                10L,
                PaymentMethod.KARTA_ONLINE,
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                STRIPE_CHECKOUT_IDEMPOTENCY_KEY,
                offer.getCost(),
                "PLN",
                offer.getKind()
        );

        GuestOrderCreationTransactionResult transactionResult = GuestOrderCreationTransactionResult.resourceCreated(
                IDEMPOTENCY_REQUEST_ID,
                1L,
                checkoutRequest
        );

        String checkoutUrl = "https://checkout.stripe.com/c/pay/cs_test_123";

        givenRequestHash(guestOrderCreationDTO);

        when(guestOrderCreationTransaction.create(
                guestOrderCreationDTO,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(transactionResult);

        when(paymentCheckout.createCheckoutIfRequired(checkoutRequest))
                .thenReturn(checkoutUrl);

        GuestOrderCreationResponseDTO result = guestOrderService.addGuestOrder(
                        guestOrderCreationDTO,
                        IDEMPOTENCY_KEY
                );

        assertNotNull(result);
        assertEquals(1L, result.guestOrderId());
        assertEquals(
                PaymentMethod.KARTA_ONLINE,
                result.paymentMethod()
        );
        assertEquals(
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                result.paymentStatus()
        );
        assertEquals(checkoutUrl, result.checkoutUrl());

        verify(guestOrderCreationTransaction).create(
                guestOrderCreationDTO,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        );

        verify(paymentCheckout).createCheckoutIfRequired(checkoutRequest);

        verify(idempotencyRequestManager)
                .markCompleted(
                        IDEMPOTENCY_REQUEST_ID,
                        checkoutUrl);
    }

    @Test
    void addGuestOrder_ShouldNotCreateCheckout_WhenTransactionFails() {
        GuestOrderCreationDTO guestOrderCreationDTO =
                createGuestOrderCreationDTO();

        givenRequestHash(guestOrderCreationDTO);

        when(guestOrderCreationTransaction.create(
                guestOrderCreationDTO,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenThrow(new NoSuchElementException(
                "Oferta o ID: "
                        + guestOrderCreationDTO.idOffer()
                        + " nie istnieje"
        ));

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> guestOrderService.addGuestOrder(
                        guestOrderCreationDTO,
                        IDEMPOTENCY_KEY
                )
        );

        assertEquals("Oferta o ID: "
                        + guestOrderCreationDTO.idOffer()
                        + " nie istnieje",
                exception.getMessage());

        verify(guestOrderCreationTransaction).create(
                guestOrderCreationDTO,
                IDEMPOTENCY_KEY,
                REQUEST_HASH);

        verifyNoInteractions(
                paymentCheckout,
                idempotencyRequestManager);
    }

    @Test
    void addGuestOrder_ShouldReturnStoredResponse_WhenRequestIsCompleted() {
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

        PaymentCheckoutRequest checkoutRequest = new PaymentCheckoutRequest(
                10L,
                PaymentMethod.KARTA_ONLINE,
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                STRIPE_CHECKOUT_IDEMPOTENCY_KEY,
                offer.getCost(),
                "PLN",
                offer.getKind());

        String checkoutUrl = "https://checkout.stripe.com/c/pay/cs_test_123";

        GuestOrderCreationTransactionResult transactionResult = GuestOrderCreationTransactionResult.completed(
                IDEMPOTENCY_REQUEST_ID,
                1L,
                checkoutRequest,
                checkoutUrl);

        givenRequestHash(guestOrderCreationDTO);

        when(guestOrderCreationTransaction.create(
                guestOrderCreationDTO,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(transactionResult);

        GuestOrderCreationResponseDTO result = guestOrderService.addGuestOrder(
                guestOrderCreationDTO,
                IDEMPOTENCY_KEY);

        assertEquals(1L, result.guestOrderId());
        assertEquals(
                PaymentMethod.KARTA_ONLINE,
                result.paymentMethod());
        assertEquals(
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                result.paymentStatus());
        assertEquals(checkoutUrl, result.checkoutUrl());

        verifyNoInteractions(
                paymentCheckout,
                idempotencyRequestManager);
    }

    @Test
    void addGuestOrder_ShouldRejectRequest_WhenSameKeyIsStillProcessing() {
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

        GuestOrderCreationTransactionResult transactionResult = GuestOrderCreationTransactionResult.inProgress(
                IDEMPOTENCY_REQUEST_ID);

        givenRequestHash(guestOrderCreationDTO);

        when(guestOrderCreationTransaction.create(
                guestOrderCreationDTO,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(transactionResult);

        IdempotencyConflictException exception = assertThrows(
                IdempotencyConflictException.class,
                () -> guestOrderService.addGuestOrder(
                        guestOrderCreationDTO,
                        IDEMPOTENCY_KEY
                ));

        assertEquals(
                "Żądanie z tym Idempotency-Key jest nadal przetwarzane",
                exception.getMessage());

        verifyNoInteractions(
                paymentCheckout,
                idempotencyRequestManager);
    }

    @Test
    void getAllGuestOrders_ShouldReturnListOfGuestOrderDTOs() {
        when(guestOrderRepository.findAll()).thenReturn(List.of(guestOrder));

        List<GuestOrderDTO> result = guestOrderService.getAllGuestOrders();

        assertEquals(1, result.size());
        assertEquals(guestOrder.getIdGuestOrder(), result.getFirst().idGuestOrder());
        assertEquals(guestOrder.getBookedOffer().getName(), result.getFirst().offer().kind());
        assertEquals(guestOrder.getBookedOffer().getPrice(), result.getFirst().offer().cost());

        verify(guestOrderRepository).findAll();
    }

    @Test
    void getGuestOrder_ShouldReturnGuestOrderDTO_WhenOrderExists() {
        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        GuestOrderDTO result = guestOrderService.getGuestOrder(1L);

        assertNotNull(result);
        assertEquals(guestOrder.getIdGuestOrder(), result.idGuestOrder());
        assertEquals(guestOrder.getBookedOffer().getName(), result.offer().kind());
        assertEquals(guestOrder.getBookedOffer().getPrice(), result.offer().cost());

        verify(guestOrderRepository).findById(1L);
    }

    @Test
    void getGuestOrder_ShouldThrowException_WhenOrderDoesNotExist() {
        when(guestOrderRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> guestOrderService.getGuestOrder(1L));

        assertEquals("Nie znaleziono zamówienia gościa o ID: 1", exception.getMessage());

        verify(guestOrderRepository).findById(1L);
    }

    @Test
    void getGuestOrdersByStatus_ShouldReturnGuestOrderDTOs() {
        when(guestOrderRepository.findGuestOrdersByStatus(OrderStatus.NOWE)).thenReturn(List.of(guestOrder));

        List<GuestOrderDTO> result = guestOrderService.getGuestOrdersByStatus(OrderStatus.NOWE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(OrderStatus.NOWE, result.getFirst().orderStatus());

        verify(guestOrderRepository).findGuestOrdersByStatus(OrderStatus.NOWE);
    }

    @Test
    void updateGuestOrder_ShouldUpdateExistingOrderWithoutChangingOffer() {
        GuestOrderUpdateRequestDTO request = createGuestOrderUpdateRequestDTO();

        LocalDateTime currentVisitDate = guestOrder.getVisitDate();

        OrderStatus currentOrderStatus = guestOrder.getOrderStatus();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));
        when(offerQuery.getRequiredOffer(request.idOffer())).thenReturn(offer);
        when(guestOrderRepository.save(guestOrder)).thenReturn(guestOrder);

        GuestOrderDTO result = guestOrderService.updateGuestOrder(request, 1L);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(request.firstname(), result.firstname()),
                () -> assertEquals(request.lastname(), result.lastname()),
                () -> assertEquals(request.phonenumber(), result.phonenumber()),
                () -> assertEquals(request.email(), result.email()),
                () -> assertEquals(offer.getIdOffer(), result.offer().idOffer()),
                () -> assertEquals(guestOrder.getBookedOffer().getName(), result.offer().kind()),
                () -> assertEquals(guestOrder.getBookedOffer().getPrice(), result.offer().cost()),
                () -> assertEquals(request.visitDate(), result.visitDate()),
                () -> assertEquals(request.orderStatus(), result.orderStatus()));

        verify(offerQuery).getRequiredOffer(request.idOffer());
        verifyNoInteractions(paymentOfferUpdater);
        verify(appointmentReservation).updateSlotReservation(
                currentVisitDate, currentOrderStatus, request.visitDate(), request.orderStatus());

        verify(guestOrderRepository).save(guestOrder);
        verify(guestOrderEvents).updated(guestOrder, currentOrderStatus);
        verify(orderModificationPolicy).validateUpdate(
                currentOrderStatus,
                request.orderStatus(),
                defaultPayment);
    }

    @Test
    void updateGuestOrder_ShouldUpdateBookedOffer_WhenAssignedOfferChanges() {
        Offer targetOffer = createOffer(2L, "Strzyżenie i broda", new BigDecimal("180.00"));

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(offer.getCost()).build();

        guestOrder.setPayment(payment);

        LocalDateTime currentVisitDate = guestOrder.getVisitDate();

        OrderStatus currentOrderStatus = guestOrder.getOrderStatus();

        GuestOrderUpdateRequestDTO request = new GuestOrderUpdateRequestDTO(
                "UpdatedJohn",
                "UpdatedDoe",
                "987654321",
                "updated@example.com",
                targetOffer.getIdOffer(),
                LocalDateTime.of(2026, 11, 10, 12, 0), OrderStatus.NOWE);

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        when(offerQuery.getRequiredOffer(targetOffer.getIdOffer())).thenReturn(targetOffer);

        when(guestOrderRepository.save(guestOrder)).thenReturn(guestOrder);

        GuestOrderDTO result = guestOrderService.updateGuestOrder(request, 1L);

        verify(paymentOfferUpdater).updateAfterOfferChange(payment, targetOffer);

        verify(appointmentReservation).updateSlotReservation(currentVisitDate,
                currentOrderStatus,
                request.visitDate(), request.orderStatus());

        verify(orderModificationPolicy).validateUpdate(
                currentOrderStatus,
                request.orderStatus(),
                payment);

        assertSame(targetOffer, guestOrder.getOffer());
        assertEquals(targetOffer.getKind(), guestOrder.getBookedOffer().getName());
        assertEquals(0, targetOffer.getCost().compareTo(guestOrder.getBookedOffer().getPrice()));
        assertEquals(targetOffer.getIdOffer(), result.offer().idOffer());
        assertEquals(targetOffer.getKind(), result.offer().kind());
        assertEquals(0, targetOffer.getCost().compareTo(result.offer().cost()));
    }

    @Test
    void updateGuestOrder_ShouldPreserveBookedOffer_WhenCatalogOfferDataChanged() {
        Offer changedCatalogOffer = createOffer(
                offer.getIdOffer(),
                "Nowa nazwa katalogowa",
                new BigDecimal("999.00"));

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(guestOrder.getBookedOffer().getPrice()).build();

        guestOrder.setPayment(payment);

        OrderStatus currentOrderStatus = guestOrder.getOrderStatus();

        String bookedName = guestOrder.getBookedOffer().getName();

        BigDecimal bookedPrice = guestOrder.getBookedOffer().getPrice();

        GuestOrderUpdateRequestDTO request = new GuestOrderUpdateRequestDTO(
                "UpdatedJohn",
                "UpdatedDoe",
                "987654321",
                "updated@example.com",
                changedCatalogOffer.getIdOffer(),
                LocalDateTime.of(2026, 11, 10, 12, 0), OrderStatus.NOWE);

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        when(offerQuery.getRequiredOffer(changedCatalogOffer.getIdOffer())).thenReturn(changedCatalogOffer);

        when(guestOrderRepository.save(guestOrder)).thenReturn(guestOrder);

        GuestOrderDTO result = guestOrderService.updateGuestOrder(request, 1L);

        verifyNoInteractions(paymentOfferUpdater);

        verify(orderModificationPolicy).validateUpdate(
                currentOrderStatus,
                request.orderStatus(),
                payment);

        assertSame(offer, guestOrder.getOffer());
        assertEquals(bookedName, guestOrder.getBookedOffer().getName());
        assertEquals(0, bookedPrice.compareTo(guestOrder.getBookedOffer().getPrice()));
        assertEquals(0, bookedPrice.compareTo(payment.getAmount()));
        assertEquals(bookedName, result.offer().kind());
        assertEquals(0, bookedPrice.compareTo(result.offer().cost()));
    }

    @Test
    void updateGuestOrder_ShouldNotUpdateOrder_WhenPaymentRejectsOfferChange() {
        Offer targetOffer = createOffer(
                2L,
                "Strzyżenie i broda",
                new BigDecimal("180.00"));

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OPLACONA)
                .amount(offer.getCost()).build();

        guestOrder.setPayment(payment);

        OrderStatus currentOrderStatus = guestOrder.getOrderStatus();
        String currentFirstname = guestOrder.getFirstname();
        LocalDateTime currentVisitDate = guestOrder.getVisitDate();

        GuestOrderUpdateRequestDTO request = new GuestOrderUpdateRequestDTO(
                "UpdatedJohn",
                "UpdatedDoe",
                "987654321",
                "updated@example.com",
                targetOffer.getIdOffer(),
                LocalDateTime.of(2026, 11, 10, 12, 0), OrderStatus.NOWE);

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        when(offerQuery.getRequiredOffer(targetOffer.getIdOffer())).thenReturn(targetOffer);

        doThrow(new OrderOfferChangeNotAllowedException("Nie można zmienić oferty w opłaconym lub zwróconym zamówieniu"))
                .when(paymentOfferUpdater).updateAfterOfferChange(payment, targetOffer);

        OrderOfferChangeNotAllowedException exception = assertThrows(OrderOfferChangeNotAllowedException.class,
                () -> guestOrderService.updateGuestOrder(request, 1L));

        assertEquals("Nie można zmienić oferty w opłaconym lub zwróconym zamówieniu", exception.getMessage());

        verify(paymentOfferUpdater).updateAfterOfferChange(payment, targetOffer);

        verify(guestOrderRepository, never()).save(any(GuestOrder.class));

        verifyNoInteractions(appointmentReservation, guestOrderEvents);

        verify(orderModificationPolicy).validateUpdate(
                currentOrderStatus,
                request.orderStatus(),
                payment);

        assertSame(offer, guestOrder.getOffer());
        assertEquals(offer.getKind(), guestOrder.getBookedOffer().getName());
        assertEquals(0, offer.getCost().compareTo(guestOrder.getBookedOffer().getPrice()));
        assertEquals(currentFirstname, guestOrder.getFirstname());
        assertEquals(currentVisitDate, guestOrder.getVisitDate());
    }

    @Test
    void updateGuestOrder_ShouldUseCurrentStatus_WhenUpdatedStatusIsNull() {
        GuestOrderUpdateRequestDTO request = createGuestOrderUpdateRequestDTOWithNullStatus();

        LocalDateTime currentVisitDate = guestOrder.getVisitDate();

        OrderStatus currentOrderStatus = guestOrder.getOrderStatus();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        when(offerQuery.getRequiredOffer(request.idOffer())).thenReturn(offer);

        when(guestOrderRepository.save(guestOrder)).thenReturn(guestOrder);

        GuestOrderDTO result = guestOrderService.updateGuestOrder(request, 1L);

        assertNotNull(result);
        assertEquals(request.visitDate(), result.visitDate());
        assertEquals(currentOrderStatus, result.orderStatus());

        verifyNoInteractions(paymentOfferUpdater);

        verify(appointmentReservation).updateSlotReservation(currentVisitDate,
                currentOrderStatus, request.visitDate(), currentOrderStatus);

        verify(guestOrderRepository).save(guestOrder);
        verify(guestOrderEvents).updated(guestOrder, currentOrderStatus);

        verify(orderModificationPolicy).validateUpdate(
                currentOrderStatus,
                currentOrderStatus,
                defaultPayment);
    }

    @Test
    void updateGuestOrder_ShouldThrowException_WhenOrderDoesNotExist() {
        GuestOrderUpdateRequestDTO request = createGuestOrderUpdateRequestDTO();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> guestOrderService.updateGuestOrder(request, 1L));

        assertEquals("Nie znaleziono zamówienia gościa o ID: 1", exception.getMessage());

        verify(guestOrderRepository).findById(1L);

        verifyNoInteractions(orderModificationPolicy, offerQuery, appointmentReservation, paymentOfferUpdater,
                guestOrderEvents);

        verify(guestOrderRepository, never()).save(any(GuestOrder.class));
    }

    @Test
    void updateGuestOrder_ShouldThrowException_WhenOfferDoesNotExist() {
        GuestOrderUpdateRequestDTO request = createGuestOrderUpdateRequestDTO();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        when(offerQuery.getRequiredOffer(request.idOffer()))
                .thenThrow(new NoSuchElementException("Oferta o ID: " + request.idOffer() + " nie istnieje"));

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> guestOrderService.updateGuestOrder(request, 1L));

        assertEquals("Oferta o ID: " + request.idOffer() + " nie istnieje", exception.getMessage());

        verify(guestOrderRepository).findById(1L);
        verify(offerQuery).getRequiredOffer(request.idOffer());

        verifyNoInteractions(appointmentReservation, paymentOfferUpdater, guestOrderEvents);

        verify(guestOrderRepository, never()).save(any(GuestOrder.class));

        verify(orderModificationPolicy).validateUpdate(
                guestOrder.getOrderStatus(),
                request.orderStatus(),
                defaultPayment);
    }

    @Test
    void deleteGuestOrderById_ShouldDeleteGuestOrder() {
        LocalDateTime visitDate = guestOrder.getVisitDate();
        OrderStatus orderStatus = guestOrder.getOrderStatus();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        guestOrderService.deleteGuestOrderById(1L);

        verify(guestOrderRepository).findById(1L);
        verify(appointmentReservation).releaseIfReserved(visitDate, orderStatus);
        verify(guestOrderRepository).delete(guestOrder);
        verify(guestOrderEvents).deleted(1L);
    }

    @Test
    void updateGuestOrder_ShouldNotContinueWhenModificationPolicyRejectsUpdate() {
        GuestOrderUpdateRequestDTO request =
                new GuestOrderUpdateRequestDTO(
                        "UpdatedJohn",
                        "UpdatedDoe",
                        "987654321",
                        "updated@example.com",
                        offer.getIdOffer(),
                        LocalDateTime.of(2026, 11, 10, 12, 0),
                        OrderStatus.ZREALIZOWANE
                );

        OrderStatus currentOrderStatus =
                guestOrder.getOrderStatus();

        doThrow(new OrderStatusChangeNotAllowedException(
                "Nie można zrealizować zamówienia przed rozliczeniem płatności"
        ))
                .when(orderModificationPolicy)
                .validateUpdate(
                        currentOrderStatus,
                        request.orderStatus(),
                        defaultPayment
                );

        when(guestOrderRepository.findById(1L))
                .thenReturn(Optional.of(guestOrder));

        OrderStatusChangeNotAllowedException exception = assertThrows(
                OrderStatusChangeNotAllowedException.class,
                () -> guestOrderService.updateGuestOrder(
                        request,
                        1L
                )
        );

        assertEquals(
                "Nie można zrealizować zamówienia przed rozliczeniem płatności",
                exception.getMessage());

        verify(orderModificationPolicy).validateUpdate(
                currentOrderStatus,
                request.orderStatus(),
                defaultPayment);

        verifyNoInteractions(
                offerQuery,
                paymentOfferUpdater,
                appointmentReservation,
                guestOrderEvents);

        verify(guestOrderRepository,
                never()
        ).save(any(GuestOrder.class));
    }

    @Test
    void updateGuestOrder_ShouldThrowExceptionWhenPaymentIsMissing() {
        GuestOrderUpdateRequestDTO request = createGuestOrderUpdateRequestDTO();

        guestOrder.setPayment(null);

        when(guestOrderRepository.findById(1L))
                .thenReturn(Optional.of(guestOrder));

        MissingPaymentException exception = assertThrows(
                MissingPaymentException.class,
                () -> guestOrderService.updateGuestOrder(
                        request,
                        1L
                )
        );

        assertEquals("Zamówienie gościa o ID 1 nie ma powiązanej płatności",
                exception.getMessage());

        verifyNoInteractions(
                orderModificationPolicy,
                offerQuery,
                paymentOfferUpdater,
                appointmentReservation,
                guestOrderEvents
        );

        verify(guestOrderRepository, never()).save(any(GuestOrder.class));

        verify(guestOrderRepository).findById(1L);
    }

    @Test
    void deleteGuestOrderById_ShouldThrowException_WhenOrderDoesNotExist() {
        when(guestOrderRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> guestOrderService.deleteGuestOrderById(1L));

        assertEquals("Nie znaleziono zamówienia gościa o ID: 1", exception.getMessage());

        verify(guestOrderRepository).findById(1L);

        verifyNoInteractions(appointmentReservation, guestOrderEvents);

        verify(guestOrderRepository, never()).delete(any(GuestOrder.class));
    }

    private void givenRequestHash(
            GuestOrderCreationDTO guestOrderCreationDTO
    ) {
        when(idempotencyRequestHasher.hash(
                "guest-order-creation-v1",
                "firstname",
                guestOrderCreationDTO.firstname(),
                "lastname",
                guestOrderCreationDTO.lastname(),
                "phonenumber",
                guestOrderCreationDTO.phonenumber(),
                "email",
                guestOrderCreationDTO.email(),
                "idOffer",
                guestOrderCreationDTO.idOffer(),
                "visitDate",
                guestOrderCreationDTO.visitDate(),
                "paymentMethod",
                guestOrderCreationDTO.paymentMethod().name()
        )).thenReturn(REQUEST_HASH);
    }
}