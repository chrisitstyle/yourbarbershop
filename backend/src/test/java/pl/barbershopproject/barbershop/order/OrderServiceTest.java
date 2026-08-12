package pl.barbershopproject.barbershop.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.exception.IdempotencyConflictException;
import pl.barbershopproject.barbershop.exception.MissingPaymentException;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestHasher;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.dto.OrderCreationResponseDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.dto.OrderUpdatedRequestDTO;
import pl.barbershopproject.barbershop.order.event.OrderEvents;
import pl.barbershopproject.barbershop.ordercreation.OrderCreationCompletionHandler;
import pl.barbershopproject.barbershop.orderupdate.OrderUpdateCoordinator;
import pl.barbershopproject.barbershop.orderupdate.OrderUpdateResult;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.security.AuthenticatedUser;
import pl.barbershopproject.barbershop.security.CurrentUserProvider;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createBookedOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities.*;
import static pl.barbershopproject.barbershop.utils.testentities.UserTestEntities.createAuthenticatedUser;
import static pl.barbershopproject.barbershop.utils.testentities.UserTestEntities.createUser;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Long IDEMPOTENCY_REQUEST_ID = 100L;
    private static final String IDEMPOTENCY_KEY = "order-service-test-key";
    private static final String REQUEST_HASH = "a".repeat(64);
    private static final String STRIPE_CHECKOUT_IDEMPOTENCY_KEY = "550e8400-e29b-41d4-a716-446655440000";

    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AppointmentReservation appointmentReservation;
    @Mock
    private OrderEvents orderEvents;
    @Mock
    private OrderCreationTransaction orderCreationTransaction;
    @Mock
    private IdempotencyRequestHasher idempotencyRequestHasher;
    @Mock
    private OrderUpdateCoordinator orderUpdateCoordinator;
    @Mock
    private OrderCreationCompletionHandler orderCreationCompletionHandler;
    @InjectMocks
    private OrderService orderService;

    private Order order;
    private User user;
    private Offer offer;
    private Payment defaultPayment;

    @BeforeEach
    void setUp() {
        offer = createOffer();
        user = createUser();

        defaultPayment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(offer.getCost())
                .build();

        order = orderBuilder()
                .idOrder(1L)
                .user(user)
                .offer(offer)
                .bookedOffer(createBookedOffer(offer))
                .orderDate(LocalDateTime.parse("2025-03-23T10:00:00"))
                .visitDate(LocalDateTime.parse("2025-03-24T12:00:00"))
                .orderStatus(OrderStatus.NOWE)
                .payment(defaultPayment)
                .build();
    }

    @Test
    void addOrder_ShouldReturnResponseWithoutCheckoutUrl_ForOfflinePayment() {
        OrderCreationDTO orderCreationDTO = createOrderCreationDTO();
        givenAuthenticatedUser();
        PaymentCheckoutRequest checkoutRequest = new PaymentCheckoutRequest(
                10L,
                PaymentMethod.GOTOWKA,
                PaymentStatus.NIE_WYMAGANA,
                null,
                offer.getCost(),
                "PLN",
                offer.getKind()
        );

        OrderCreationTransactionResult transactionResult = OrderCreationTransactionResult
                .resourceCreated(
                        IDEMPOTENCY_REQUEST_ID,
                        1L,
                        checkoutRequest);

        givenRequestHash(orderCreationDTO);

        when(orderCreationTransaction.create(
                orderCreationDTO,
                user,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(transactionResult);

        when(orderCreationCompletionHandler.complete(
                transactionResult)).thenReturn(null);

        OrderCreationResponseDTO result = orderService.addOrder(
                orderCreationDTO,
                IDEMPOTENCY_KEY);

        assertNotNull(result);
        assertEquals(1L, result.orderId());
        assertEquals(PaymentMethod.GOTOWKA, result.paymentMethod());
        assertEquals(PaymentStatus.NIE_WYMAGANA, result.paymentStatus());
        assertNull(result.checkoutUrl());

        verify(orderCreationTransaction).create(
                orderCreationDTO,
                user,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        );

        verify(orderCreationCompletionHandler).complete(
                transactionResult);
    }

    @Test
    void addOrder_ShouldReturnCheckoutUrl_ForOnlinePayment() {
        OrderCreationDTO orderCreationDTO = createOrderCreationDTO();
        givenAuthenticatedUser();
        PaymentCheckoutRequest checkoutRequest = new PaymentCheckoutRequest(
                10L,
                PaymentMethod.KARTA_ONLINE,
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                STRIPE_CHECKOUT_IDEMPOTENCY_KEY,
                offer.getCost(),
                "PLN",
                offer.getKind()
        );

        OrderCreationTransactionResult transactionResult = OrderCreationTransactionResult
                .resourceCreated(
                        IDEMPOTENCY_REQUEST_ID,
                        1L,
                        checkoutRequest);

        String checkoutUrl = "https://checkout.stripe.com/c/pay/cs_test_123";

        givenRequestHash(orderCreationDTO);

        when(orderCreationTransaction.create(
                orderCreationDTO,
                user,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(transactionResult);

        when(orderCreationCompletionHandler.complete(
                transactionResult)).thenReturn(checkoutUrl);

        OrderCreationResponseDTO result = orderService.addOrder(
                orderCreationDTO,
                IDEMPOTENCY_KEY);

        assertNotNull(result);
        assertEquals(1L, result.orderId());
        assertEquals(PaymentMethod.KARTA_ONLINE, result.paymentMethod());
        assertEquals(
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                result.paymentStatus()
        );
        assertEquals(checkoutUrl, result.checkoutUrl());

        verify(orderCreationTransaction).create(
                orderCreationDTO,
                user,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        );

        verify(orderCreationCompletionHandler).complete(
                transactionResult);
    }

    @Test
    void addOrder_ShouldNotCreateCheckout_WhenTransactionFails() {
        OrderCreationDTO orderCreationDTO = createOrderCreationDTO();
        givenAuthenticatedUser();
        givenRequestHash(orderCreationDTO);

        when(orderCreationTransaction.create(
                orderCreationDTO,
                user,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenThrow(new NoSuchElementException(
                "Oferta o ID: "
                        + orderCreationDTO.idOffer()
                        + " nie istnieje"
        ));

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.addOrder(
                        orderCreationDTO,
                        IDEMPOTENCY_KEY
                )
        );

        assertEquals(
                "Oferta o ID: "
                        + orderCreationDTO.idOffer()
                        + " nie istnieje",
                exception.getMessage()
        );

        verify(orderCreationTransaction).create(
                orderCreationDTO,
                user,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        );

        verifyNoInteractions(orderCreationCompletionHandler);
    }

    @Test
    void addOrder_ShouldReturnStoredResponse_WhenRequestIsCompleted() {
        OrderCreationDTO orderCreationDTO = createOrderCreationDTO();
        givenAuthenticatedUser();
        givenRequestHash(orderCreationDTO);
        PaymentCheckoutRequest checkoutRequest = new PaymentCheckoutRequest(
                10L,
                PaymentMethod.KARTA_ONLINE,
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                STRIPE_CHECKOUT_IDEMPOTENCY_KEY,
                offer.getCost(),
                "PLN",
                offer.getKind()
        );

        String checkoutUrl = "https://checkout.stripe.com/c/pay/cs_test_123";

        OrderCreationTransactionResult transactionResult = OrderCreationTransactionResult.completed(
                IDEMPOTENCY_REQUEST_ID,
                1L,
                checkoutRequest,
                checkoutUrl);

        when(orderCreationTransaction.create(
                orderCreationDTO,
                user,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(transactionResult);

        when(orderCreationCompletionHandler.complete(
                transactionResult)).thenReturn(checkoutUrl);

        OrderCreationResponseDTO result = orderService.addOrder(
                orderCreationDTO,
                IDEMPOTENCY_KEY
        );

        assertEquals(1L, result.orderId());
        assertEquals(PaymentMethod.KARTA_ONLINE, result.paymentMethod());
        assertEquals(
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                result.paymentStatus()
        );
        assertEquals(checkoutUrl, result.checkoutUrl());

        verify(orderCreationCompletionHandler).complete(transactionResult);
    }
    @Test
    void addOrder_ShouldRejectRequest_WhenSameKeyIsStillProcessing() {
        OrderCreationDTO orderCreationDTO = createOrderCreationDTO();

        givenAuthenticatedUser();
        givenRequestHash(orderCreationDTO);

        OrderCreationTransactionResult transactionResult = OrderCreationTransactionResult
                .inProgress(IDEMPOTENCY_REQUEST_ID);

        when(orderCreationTransaction.create(
                orderCreationDTO,
                user,
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
                        () -> orderService.addOrder(
                                orderCreationDTO,
                                IDEMPOTENCY_KEY));

        assertEquals(
                "Żądanie z tym Idempotency-Key jest nadal przetwarzane",
                exception.getMessage());

        verify(orderCreationTransaction).create(
                orderCreationDTO,
                user,
                IDEMPOTENCY_KEY,
                REQUEST_HASH);

        verify(orderCreationCompletionHandler).complete(
                transactionResult);
    }

    @Test
    void getAllOrders_ShouldReturnListOfOrderDTOs() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getAllOrders();

        assertEquals(1, result.size());
        assertEquals(order.getIdOrder(), result.getFirst().idOrder());
        assertEquals(order.getBookedOffer().getName(), result.getFirst().offer().kind());
        assertEquals(order.getBookedOffer().getPrice(), result.getFirst().offer().cost());

        verify(orderRepository).findAll();
    }

    @Test
    void getSingleOrder_ShouldReturnOrderDTO_WhenOrderExists() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDTO result = orderService.getSingleOrder(1L);

        assertNotNull(result);
        assertEquals(order.getIdOrder(), result.idOrder());
        assertEquals(order.getBookedOffer().getName(), result.offer().kind());
        assertEquals(order.getBookedOffer().getPrice(), result.offer().cost());

        verify(orderRepository).findById(1L);
    }

    @Test
    void getSingleOrder_ShouldThrowException_WhenOrderNotFound() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> orderService.getSingleOrder(2L));

        assertEquals("Zamówienie o ID: 2 nie istnieje", exception.getMessage());

        verify(orderRepository).findById(2L);
    }

    @Test
    void getOrdersByStatus_ShouldReturnFilteredOrders() {
        when(orderRepository.findOrdersByStatus(OrderStatus.NOWE)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrdersByStatus("NOWE");

        assertEquals(1, result.size());
        assertEquals(OrderStatus.NOWE, result.getFirst().orderStatus());

        verify(orderRepository).findOrdersByStatus(OrderStatus.NOWE);
    }

    @Test
    void getOrdersByStatus_ShouldReturnFilteredOrders_WhenStatusHasLowerCaseLetters() {
        when(orderRepository.findOrdersByStatus(OrderStatus.NOWE)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrdersByStatus("nowe");

        assertEquals(1, result.size());
        assertEquals(OrderStatus.NOWE, result.getFirst().orderStatus());

        verify(orderRepository).findOrdersByStatus(OrderStatus.NOWE);
    }

    @Test
    void getOrdersByStatus_ShouldThrowException_ForInvalidStatus() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.getOrdersByStatus("invalid"));

        assertTrue(exception.getMessage().contains("Dostępne statusy"));

        verify(orderRepository, never()).findOrdersByStatus(any());
    }

    @Test
    void updateOrder_ShouldUpdateExistingOrder() {
        OrderUpdatedRequestDTO request = createOrderUpdatedRequestDTO();

        OrderStatus currentOrderStatus = order.getOrderStatus();

        OrderUpdateResult updateResult = new OrderUpdateResult(
                currentOrderStatus,
                request.orderStatus());

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderUpdateCoordinator.prepareUpdate(
                order,
                defaultPayment,
                request.idOffer(),
                request.visitDate(),
                request.orderStatus()
        )).thenReturn(updateResult);

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderDTO result = orderService.updateOrder(request, 1L);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        order.getIdOrder(),
                        result.idOrder()),
                () -> assertEquals(
                        user.getIdUser(),
                        result.user().idUser()),
                () -> assertEquals(
                        request.visitDate(),
                        result.visitDate()),
                () -> assertEquals(
                        request.orderStatus(),
                        result.orderStatus()));

        verify(orderUpdateCoordinator).prepareUpdate(
                order,
                defaultPayment,
                request.idOffer(),
                request.visitDate(),
                request.orderStatus());

        verify(orderRepository).save(order);

        verify(orderEvents).updated(
                order,
                currentOrderStatus);
    }

    @Test
    void updateOrder_ShouldUseTargetStatusReturnedByCoordinator() {
        LocalDateTime targetVisitDate = LocalDateTime.parse("2025-03-26T10:00:00");

        OrderUpdatedRequestDTO request = new OrderUpdatedRequestDTO(
                offer.getIdOffer(),
                targetVisitDate,
                null);

        OrderStatus currentOrderStatus = order.getOrderStatus();

        OrderUpdateResult updateResult = new OrderUpdateResult(
                currentOrderStatus,
                currentOrderStatus);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(orderUpdateCoordinator.prepareUpdate(
                order,
                defaultPayment,
                request.idOffer(),
                request.visitDate(),
                null
        )).thenReturn(updateResult);

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderDTO result = orderService.updateOrder(
                request,
                1L);

        assertNotNull(result);
        assertEquals(targetVisitDate, result.visitDate());
        assertEquals(
                currentOrderStatus,
                result.orderStatus()
        );

        verify(orderUpdateCoordinator).prepareUpdate(
                order,
                defaultPayment,
                request.idOffer(),
                request.visitDate(),
                null
        );

        verify(orderRepository).save(order);

        verify(orderEvents).updated(
                order,
                currentOrderStatus
        );
    }

    @Test
    void updateOrder_ShouldThrowException_WhenOrderNotFound() {
        OrderUpdatedRequestDTO request = createOrderUpdatedRequestDTO();

        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> orderService.updateOrder(request, 2L));

        assertEquals("Zamówienie o ID: 2 nie istnieje", exception.getMessage());

        verify(orderRepository).findById(2L);

        verifyNoInteractions(
                orderUpdateCoordinator,
                appointmentReservation,
                orderEvents);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrder_ShouldThrowExceptionWhenPaymentIsMissing() {
        OrderUpdatedRequestDTO request = createOrderUpdatedRequestDTO();

        order.setPayment(null);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        MissingPaymentException exception = assertThrows(
                MissingPaymentException.class,
                () -> orderService.updateOrder(
                        request,
                        1L
                )
        );

        assertEquals(
                "Zamówienie o ID 1 nie ma powiązanej płatności",
                exception.getMessage()
        );

        verifyNoInteractions(
                orderUpdateCoordinator,
                appointmentReservation,
                orderEvents);

        verify(orderRepository,
                never()).save(any(Order.class));
    }

    @Test
    void deleteOrderById_ShouldDeleteExistingOrder() {
        LocalDateTime visitDate = order.getVisitDate();
        OrderStatus orderStatus = order.getOrderStatus();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrderById(1L);

        verify(orderRepository).findById(1L);

        verify(appointmentReservation).releaseIfReserved(visitDate, orderStatus);

        verify(orderRepository).delete(order);
        verify(orderEvents).deleted(1L);
    }

    @Test
    void deleteOrderById_ShouldThrowException_WhenOrderNotExists() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> orderService.deleteOrderById(2L));

        assertEquals("Zamówienie o ID: 2 nie istnieje", exception.getMessage());

        verify(orderRepository).findById(2L);

        verifyNoInteractions(appointmentReservation, orderEvents);

        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void addOrder_ShouldThrowException_WhenAuthenticatedUserDoesNotExist() {
        // given
        OrderCreationDTO orderCreationDTO = createOrderCreationDTO();
        AuthenticatedUser authenticatedUser = createAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(userRepository.findById(authenticatedUser.userId()))
                .thenReturn(Optional.empty());

        // when, then
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.addOrder(
                        orderCreationDTO,
                        IDEMPOTENCY_KEY
                )
        );

        assertEquals("Użytkownik o podanym ID nie istnieje",
                exception.getMessage());

        verify(currentUserProvider).getCurrentUser();
        verify(userRepository).findById(authenticatedUser.userId());

        verifyNoInteractions(
                orderCreationTransaction,
                orderCreationCompletionHandler);
    }

    private void givenRequestHash(
            OrderCreationDTO orderCreationDTO
    ) {
        when(idempotencyRequestHasher.hash(
                "order-creation-v1",
                "idOffer",
                orderCreationDTO.idOffer(),
                "visitDate",
                orderCreationDTO.visitDate(),
                "paymentMethod",
                orderCreationDTO.paymentMethod().name()
        )).thenReturn(REQUEST_HASH);
    }

    private void givenAuthenticatedUser() {
        AuthenticatedUser authenticatedUser = createAuthenticatedUser();

        when(currentUserProvider.getCurrentUser())
                .thenReturn(authenticatedUser);

        when(userRepository.findById(authenticatedUser.userId()))
                .thenReturn(Optional.of(user));
    }
}