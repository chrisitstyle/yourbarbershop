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
import pl.barbershopproject.barbershop.exception.OrderOfferChangeNotAllowedException;
import pl.barbershopproject.barbershop.exception.OrderStatusChangeNotAllowedException;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestHasher;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestManager;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.dto.OrderCreationResponseDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.dto.OrderUpdatedRequestDTO;
import pl.barbershopproject.barbershop.order.event.OrderEvents;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentCheckout;
import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentOfferUpdater;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.security.AuthenticatedUser;
import pl.barbershopproject.barbershop.security.CurrentUserProvider;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import pl.barbershopproject.barbershop.utils.OrderModificationPolicy;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createBookedOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities.createOrderCreationDTO;
import static pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities.createOrderUpdatedRequestDTO;
import static pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities.orderBuilder;
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
    private OfferQuery offerQuery;
    @Mock
    private AppointmentReservation appointmentReservation;
    @Mock
    private PaymentOfferUpdater paymentOfferUpdater;
    @Mock
    private OrderEvents orderEvents;
    @Mock
    private OrderCreationTransaction orderCreationTransaction;
    @Mock
    private PaymentCheckout paymentCheckout;
    @Mock
    private OrderModificationPolicy orderModificationPolicy;

    @Mock
    private IdempotencyRequestHasher idempotencyRequestHasher;
    @Mock
    private IdempotencyRequestManager idempotencyRequestManager;

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

        when(paymentCheckout.createCheckoutIfRequired(checkoutRequest))
                .thenReturn(null);

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

        verify(paymentCheckout)
                .createCheckoutIfRequired(checkoutRequest);

        verify(idempotencyRequestManager)
                .markCompleted(IDEMPOTENCY_REQUEST_ID, null);
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

        OrderCreationTransactionResult transactionResult =
                OrderCreationTransactionResult.resourceCreated(
                        IDEMPOTENCY_REQUEST_ID,
                        1L,
                        checkoutRequest
                );

        String checkoutUrl =
                "https://checkout.stripe.com/c/pay/cs_test_123";

        givenRequestHash(orderCreationDTO);

        when(orderCreationTransaction.create(
                orderCreationDTO,
                user,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(transactionResult);

        when(paymentCheckout.createCheckoutIfRequired(checkoutRequest))
                .thenReturn(checkoutUrl);

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

        verify(paymentCheckout)
                .createCheckoutIfRequired(checkoutRequest);

        verify(idempotencyRequestManager)
                .markCompleted(
                        IDEMPOTENCY_REQUEST_ID,
                        checkoutUrl
                );
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

        verifyNoInteractions(
                paymentCheckout,
                idempotencyRequestManager
        );
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

        OrderCreationTransactionResult transactionResult =
                OrderCreationTransactionResult.completed(
                        IDEMPOTENCY_REQUEST_ID,
                        1L,
                        checkoutRequest,
                        checkoutUrl
                );

        givenRequestHash(orderCreationDTO);

        when(orderCreationTransaction.create(
                orderCreationDTO,
                user,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(transactionResult);

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

        verifyNoInteractions(
                paymentCheckout,
                idempotencyRequestManager
        );
    }

    @Test
    void addOrder_ShouldRejectRequest_WhenSameKeyIsStillProcessing() {
        OrderCreationDTO orderCreationDTO = createOrderCreationDTO();
        givenAuthenticatedUser();
        givenRequestHash(orderCreationDTO);
        OrderCreationTransactionResult transactionResult =
                OrderCreationTransactionResult.inProgress(
                        IDEMPOTENCY_REQUEST_ID
                );

        givenRequestHash(orderCreationDTO);

        when(orderCreationTransaction.create(
                orderCreationDTO,
                user,
                IDEMPOTENCY_KEY,
                REQUEST_HASH
        )).thenReturn(transactionResult);

        IdempotencyConflictException exception = assertThrows(
                IdempotencyConflictException.class,
                () -> orderService.addOrder(
                        orderCreationDTO,
                        IDEMPOTENCY_KEY
                )
        );

        assertEquals(
                "Żądanie z tym Idempotency-Key jest nadal przetwarzane",
                exception.getMessage()
        );

        verifyNoInteractions(
                paymentCheckout,
                idempotencyRequestManager
        );
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
    void updateOrder_ShouldUpdateExistingOrderWithoutChangingOffer() {
        OrderUpdatedRequestDTO request = createOrderUpdatedRequestDTO();

        LocalDateTime currentVisitDate = order.getVisitDate();
        OrderStatus currentOrderStatus = order.getOrderStatus();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(offerQuery.getRequiredOffer(request.idOffer())).thenReturn(offer);

        when(orderRepository.save(order)).thenReturn(order);

        OrderDTO result = orderService.updateOrder(request, 1L);

        assertNotNull(result);

        assertAll(() -> assertEquals(order.getIdOrder(), result.idOrder()),
                () -> assertEquals(user.getIdUser(), result.user().idUser()),
                () -> assertEquals(user.getFirstname(), result.user().firstname()),
                () -> assertEquals(user.getLastname(), result.user().lastname()),
                () -> assertEquals(user.getEmail(), result.user().email()),
                () -> assertEquals(offer.getIdOffer(), result.offer().idOffer()),
                () -> assertEquals(order.getBookedOffer().getName(), result.offer().kind()),
                () -> assertEquals(order.getBookedOffer().getPrice(), result.offer().cost()),
                () -> assertEquals(request.visitDate(), result.visitDate()),
                () -> assertEquals(request.orderStatus(), result.orderStatus()));

        verify(offerQuery).getRequiredOffer(request.idOffer());

        verifyNoInteractions(paymentOfferUpdater);

        verify(appointmentReservation).updateSlotReservation(currentVisitDate,
                currentOrderStatus, request.visitDate(), request.orderStatus());

        verify(orderRepository).save(order);
        verify(orderEvents).updated(order, currentOrderStatus);

        verify(orderModificationPolicy).validateUpdate(
                currentOrderStatus,
                request.orderStatus(),
                defaultPayment);
    }

    @Test
    void updateOrder_ShouldUpdateBookedOffer_WhenAssignedOfferChanges() {
        Offer targetOffer = createOffer(2L, "Strzyżenie i broda", new BigDecimal("180.00"));

        Payment payment = Payment
                .builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA).amount(offer.getCost()).build();

        order.setPayment(payment);

        LocalDateTime currentVisitDate = order.getVisitDate();
        OrderStatus currentOrderStatus = order.getOrderStatus();

        OrderUpdatedRequestDTO request = new OrderUpdatedRequestDTO(
                targetOffer.getIdOffer(),
                LocalDateTime.of(2026, Month.NOVEMBER, 10, 12, 0), OrderStatus.NOWE);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(offerQuery.getRequiredOffer(targetOffer.getIdOffer())).thenReturn(targetOffer);

        when(orderRepository.save(order)).thenReturn(order);

        OrderDTO result = orderService.updateOrder(request, 1L);

        verify(paymentOfferUpdater).updateAfterOfferChange(payment, targetOffer);

        verify(appointmentReservation).updateSlotReservation(currentVisitDate,
                currentOrderStatus, request.visitDate(), request.orderStatus());

        verify(orderModificationPolicy).validateUpdate(
                currentOrderStatus,
                request.orderStatus(),
                payment);

        assertSame(targetOffer, order.getOffer());
        assertEquals(targetOffer.getKind(), order.getBookedOffer().getName());
        assertEquals(0, targetOffer.getCost().compareTo(order.getBookedOffer().getPrice()));
        assertEquals(targetOffer.getIdOffer(), result.offer().idOffer());
        assertEquals(targetOffer.getKind(), result.offer().kind());
        assertEquals(0, targetOffer.getCost().compareTo(result.offer().cost()));
    }

    @Test
    void updateOrder_ShouldPreserveBookedOffer_WhenCatalogOfferDataChanged() {
        Offer changedCatalogOffer = createOffer(offer.getIdOffer(), "Nowa nazwa katalogowa",
                new BigDecimal("999.00"));

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(order.getBookedOffer().getPrice()).build();

        order.setPayment(payment);

        String bookedName = order.getBookedOffer().getName();
        BigDecimal bookedPrice = order.getBookedOffer().getPrice();

        OrderUpdatedRequestDTO request = new OrderUpdatedRequestDTO(
                changedCatalogOffer.getIdOffer(),
                LocalDateTime.of(2026, Month.NOVEMBER, 10, 12, 0), OrderStatus.NOWE);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(offerQuery.getRequiredOffer(changedCatalogOffer.getIdOffer())).thenReturn(changedCatalogOffer);

        when(orderRepository.save(order)).thenReturn(order);

        OrderDTO result = orderService.updateOrder(request, 1L);

        verifyNoInteractions(paymentOfferUpdater);

        assertSame(offer, order.getOffer());
        assertEquals(bookedName, order.getBookedOffer().getName());
        assertEquals(0, bookedPrice.compareTo(order.getBookedOffer().getPrice()));
        assertEquals(0, bookedPrice.compareTo(payment.getAmount()));
        assertEquals(bookedName, result.offer().kind());
        assertEquals(0, bookedPrice.compareTo(result.offer().cost()));
    }

    @Test
    void updateOrder_ShouldNotUpdateOrder_WhenPaymentRejectsOfferChange() {
        Offer targetOffer = createOffer(2L, "Strzyżenie i broda", new BigDecimal("180.00"));

        Payment payment = Payment
                .builder()
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OPLACONA)
                .amount(offer.getCost())
                .build();

        order.setPayment(payment);

        OrderUpdatedRequestDTO request = new OrderUpdatedRequestDTO(
                targetOffer.getIdOffer(),
                LocalDateTime.of(2026, Month.NOVEMBER, 10, 12, 0), OrderStatus.NOWE);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(offerQuery.getRequiredOffer(targetOffer.getIdOffer())).thenReturn(targetOffer);

        doThrow(new OrderOfferChangeNotAllowedException("Nie można zmienić oferty w opłaconym zamówieniu"))
                .when(paymentOfferUpdater).updateAfterOfferChange(payment, targetOffer);

        OrderOfferChangeNotAllowedException exception = assertThrows(OrderOfferChangeNotAllowedException.class,
                () -> orderService.updateOrder(request, 1L));

        assertEquals("Nie można zmienić oferty w opłaconym zamówieniu", exception.getMessage());

        verify(orderRepository, never()).save(any(Order.class));

        verifyNoInteractions(appointmentReservation, orderEvents);

        assertSame(offer, order.getOffer());
        assertEquals(offer.getKind(), order.getBookedOffer().getName());
        assertEquals(0, offer.getCost().compareTo(order.getBookedOffer().getPrice()));
    }

    @Test
    void updateOrder_ShouldUseCurrentStatus_WhenUpdatedStatusIsNull() {
        LocalDateTime targetVisitDate = LocalDateTime.parse("2025-03-26T10:00:00");

        OrderUpdatedRequestDTO request = new OrderUpdatedRequestDTO(offer.getIdOffer(), targetVisitDate, null);

        LocalDateTime currentVisitDate = order.getVisitDate();
        OrderStatus currentOrderStatus = order.getOrderStatus();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(offerQuery.getRequiredOffer(request.idOffer())).thenReturn(offer);

        when(orderRepository.save(order)).thenReturn(order);

        OrderDTO result = orderService.updateOrder(request, 1L);

        assertNotNull(result);
        assertEquals(targetVisitDate, result.visitDate());
        assertEquals(currentOrderStatus, result.orderStatus());

        verifyNoInteractions(paymentOfferUpdater);

        verify(appointmentReservation).updateSlotReservation(
                currentVisitDate, currentOrderStatus, targetVisitDate, currentOrderStatus);

        verify(orderRepository).save(order);
        verify(orderEvents).updated(order, currentOrderStatus);

        verify(orderModificationPolicy).validateUpdate(
                currentOrderStatus,
                currentOrderStatus,
                defaultPayment);
    }

    @Test
    void updateOrder_ShouldThrowException_WhenOrderNotFound() {
        OrderUpdatedRequestDTO request = createOrderUpdatedRequestDTO();

        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> orderService.updateOrder(request, 2L));

        assertEquals("Zamówienie o ID: 2 nie istnieje", exception.getMessage());

        verify(orderRepository).findById(2L);

        verifyNoInteractions(orderModificationPolicy, offerQuery, appointmentReservation, paymentOfferUpdater,
                orderEvents);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrder_ShouldThrowException_WhenOfferNotFound() {
        OrderUpdatedRequestDTO request = createOrderUpdatedRequestDTO();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(offerQuery.getRequiredOffer(request.idOffer()))
                .thenThrow(new NoSuchElementException("Oferta o ID: " + request.idOffer() + " nie istnieje"));

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> orderService.updateOrder(request, 1L));

        assertEquals("Oferta o ID: " + request.idOffer() + " nie istnieje", exception.getMessage());

        verify(orderRepository).findById(1L);
        verify(offerQuery).getRequiredOffer(request.idOffer());

        verifyNoInteractions(appointmentReservation, paymentOfferUpdater, orderEvents);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrder_ShouldNotContinueWhenModificationPolicyRejectsUpdate() {
        OrderUpdatedRequestDTO request = new OrderUpdatedRequestDTO(
                        offer.getIdOffer(),
                        LocalDateTime.of(2026, Month.NOVEMBER, 10, 12, 0),
                        OrderStatus.ZREALIZOWANE
                );

        OrderStatus currentOrderStatus = order.getOrderStatus();

        doThrow(new OrderStatusChangeNotAllowedException(
                "Nie można zrealizować zamówienia przed rozliczeniem płatności"
        ))
                .when(orderModificationPolicy)
                .validateUpdate(
                        currentOrderStatus,
                        request.orderStatus(),
                        defaultPayment
                );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderStatusChangeNotAllowedException exception = assertThrows(
                        OrderStatusChangeNotAllowedException.class,
                        () -> orderService.updateOrder(
                                request,
                                1L
                        )
                );

        assertEquals(
                "Nie można zrealizować zamówienia przed rozliczeniem płatności",
                exception.getMessage()
        );

        verify(orderModificationPolicy).validateUpdate(
                currentOrderStatus,
                request.orderStatus(),
                defaultPayment
        );

        verifyNoInteractions(
                offerQuery,
                paymentOfferUpdater,
                appointmentReservation,
                orderEvents
        );

        verify(
                orderRepository,
                never()
        ).save(any(Order.class));
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
                orderModificationPolicy,
                offerQuery,
                paymentOfferUpdater,
                appointmentReservation,
                orderEvents
        );

        verify(
                orderRepository,
                never()
        ).save(any(Order.class));
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
                paymentCheckout,
                idempotencyRequestManager
        );
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