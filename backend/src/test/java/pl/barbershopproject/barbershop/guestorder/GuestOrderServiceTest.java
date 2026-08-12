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
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationResponseDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderUpdateRequestDTO;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestHasher;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.ordercreation.OrderCreationCompletionHandler;
import pl.barbershopproject.barbershop.orderupdate.OrderUpdateCoordinator;
import pl.barbershopproject.barbershop.orderupdate.OrderUpdateResult;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.utils.OrderStatus;

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
    private AppointmentReservation appointmentReservation;
    @Mock
    private GuestOrderEvents guestOrderEvents;
    @Mock
    private GuestOrderCreationTransaction guestOrderCreationTransaction;

    @Mock
    private OrderUpdateCoordinator orderUpdateCoordinator;

    @Mock
    private OrderCreationCompletionHandler orderCreationCompletionHandler;
    @Mock
    private IdempotencyRequestHasher idempotencyRequestHasher;

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
                offer.getKind());

        GuestOrderCreationTransactionResult transactionResult = GuestOrderCreationTransactionResult
                .resourceCreated(
                        IDEMPOTENCY_REQUEST_ID,
                        1L,
                        checkoutRequest);

        givenRequestHash(guestOrderCreationDTO);

        when(guestOrderCreationTransaction.create(
                guestOrderCreationDTO,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(transactionResult);

        when(orderCreationCompletionHandler.complete(
                transactionResult
        )).thenReturn(null);

        GuestOrderCreationResponseDTO result = guestOrderService.addGuestOrder(
                guestOrderCreationDTO,
                IDEMPOTENCY_KEY);

        assertNotNull(result);
        assertEquals(1L, result.guestOrderId());
        assertEquals(
                PaymentMethod.GOTOWKA,
                result.paymentMethod()
        );
        assertEquals(
                PaymentStatus.NIE_WYMAGANA,
                result.paymentStatus()
        );
        assertNull(result.checkoutUrl());

        verify(guestOrderCreationTransaction).create(
                guestOrderCreationDTO,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        );

        verify(orderCreationCompletionHandler).complete(
                transactionResult
        );
    }

    @Test
    void addGuestOrder_ShouldReturnCheckoutUrl_ForOnlinePayment() {
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

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

        when(orderCreationCompletionHandler.complete(transactionResult)).thenReturn(checkoutUrl);

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
        verify(orderCreationCompletionHandler).complete(transactionResult);
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

        verifyNoInteractions(orderCreationCompletionHandler);
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

        when(orderCreationCompletionHandler.complete(
                transactionResult)).thenReturn(checkoutUrl);

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

        verify(orderCreationCompletionHandler).complete(
                transactionResult);
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

        when(orderCreationCompletionHandler.complete(
                transactionResult
        )).thenThrow(
                new IdempotencyConflictException(
                        "Żądanie z tym Idempotency-Key jest nadal przetwarzane"));

        IdempotencyConflictException exception = assertThrows(
                IdempotencyConflictException.class,
                () -> guestOrderService.addGuestOrder(
                        guestOrderCreationDTO,
                        IDEMPOTENCY_KEY
                ));

        assertEquals(
                "Żądanie z tym Idempotency-Key jest nadal przetwarzane",
                exception.getMessage());

        verify(orderCreationCompletionHandler).complete(
                transactionResult);
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
    void updateGuestOrder_ShouldUpdateExistingOrder() {
        GuestOrderUpdateRequestDTO request = createGuestOrderUpdateRequestDTO();

        OrderStatus currentOrderStatus = guestOrder.getOrderStatus();

        OrderUpdateResult updateResult = new OrderUpdateResult(
                currentOrderStatus,
                request.orderStatus());

        when(guestOrderRepository.findById(1L))
                .thenReturn(Optional.of(guestOrder));

        when(orderUpdateCoordinator.prepareUpdate(
                guestOrder,
                defaultPayment,
                request.idOffer(),
                request.visitDate(),
                request.orderStatus()
        )).thenReturn(updateResult);

        when(guestOrderRepository.save(guestOrder))
                .thenReturn(guestOrder);

        GuestOrderDTO result = guestOrderService.updateGuestOrder(
                request, 1L);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        request.firstname(),
                        result.firstname()),
                () -> assertEquals(
                        request.lastname(),
                        result.lastname()),
                () -> assertEquals(
                        request.phonenumber(),
                        result.phonenumber()),
                () -> assertEquals(
                        request.email(),
                        result.email()),
                () -> assertEquals(
                        request.visitDate(),
                        result.visitDate()),
                () -> assertEquals(
                        request.orderStatus(),
                        result.orderStatus())
        );

        verify(orderUpdateCoordinator).prepareUpdate(
                guestOrder,
                defaultPayment,
                request.idOffer(),
                request.visitDate(),
                request.orderStatus()
        );

        verify(guestOrderRepository).save(guestOrder);

        verify(guestOrderEvents).updated(
                guestOrder,
                currentOrderStatus
        );
    }

    @Test
    void updateGuestOrder_ShouldUseTargetStatusReturnedByCoordinator() {
        GuestOrderUpdateRequestDTO request =
                createGuestOrderUpdateRequestDTOWithNullStatus();

        OrderStatus currentOrderStatus =
                guestOrder.getOrderStatus();

        OrderUpdateResult updateResult =
                new OrderUpdateResult(
                        currentOrderStatus,
                        currentOrderStatus
                );

        when(guestOrderRepository.findById(1L))
                .thenReturn(Optional.of(guestOrder));

        when(orderUpdateCoordinator.prepareUpdate(
                guestOrder,
                defaultPayment,
                request.idOffer(),
                request.visitDate(),
                null
        )).thenReturn(updateResult);

        when(guestOrderRepository.save(guestOrder))
                .thenReturn(guestOrder);

        GuestOrderDTO result =
                guestOrderService.updateGuestOrder(
                        request,
                        1L
                );

        assertNotNull(result);
        assertEquals(
                request.visitDate(),
                result.visitDate()
        );
        assertEquals(
                currentOrderStatus,
                result.orderStatus()
        );

        verify(orderUpdateCoordinator).prepareUpdate(
                guestOrder,
                defaultPayment,
                request.idOffer(),
                request.visitDate(),
                null
        );

        verify(guestOrderRepository).save(guestOrder);

        verify(guestOrderEvents).updated(
                guestOrder,
                currentOrderStatus
        );
    }

    @Test
    void updateGuestOrder_ShouldThrowException_WhenOrderDoesNotExist() {
        GuestOrderUpdateRequestDTO request = createGuestOrderUpdateRequestDTO();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> guestOrderService.updateGuestOrder(request, 1L));

        assertEquals("Nie znaleziono zamówienia gościa o ID: 1", exception.getMessage());

        verify(guestOrderRepository).findById(1L);

        verifyNoInteractions(
                orderUpdateCoordinator,
                appointmentReservation,
                guestOrderEvents);

        verify(guestOrderRepository, never()).save(any(GuestOrder.class));
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
                orderUpdateCoordinator,
                appointmentReservation,
                guestOrderEvents);

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