package pl.barbershopproject.barbershop.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.exception.AppointmentSlotTakenException;
import pl.barbershopproject.barbershop.exception.OrderOfferChangeNotAllowedException;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.dto.OrderCreationResponseDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.dto.OrderUpdatedRequestDTO;
import pl.barbershopproject.barbershop.order.event.OrderEvents;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentCreationResult;
import pl.barbershopproject.barbershop.payment.PaymentCreator;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentOfferUpdater;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.utils.Status;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createBookedOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities.createOrderCreationDTO;
import static pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities.createOrderUpdatedRequestDTO;
import static pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities.orderBuilder;
import static pl.barbershopproject.barbershop.utils.testentities.UserTestEntities.createUser;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final ZoneId TEST_ZONE =
            ZoneId.of("Europe/Warsaw");

    private static final Instant TEST_INSTANT =
            Instant.parse("2026-01-16T12:00:00Z");

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OfferQuery offerQuery;

    @Mock
    private AppointmentReservation appointmentReservation;

    @Mock
    private PaymentCreator paymentCreator;

    @Mock
    private PaymentOfferUpdater paymentOfferUpdater;

    @Mock
    private OrderEvents orderEvents;

    @Mock
    private Clock clock;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private User user;
    private Offer offer;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(TEST_INSTANT);
        when(clock.getZone()).thenReturn(TEST_ZONE);

        offer = createOffer();
        user = createUser();

        order = orderBuilder()
                .idOrder(1L)
                .user(user)
                .offer(offer)
                .bookedOffer(createBookedOffer(offer))
                .orderDate(LocalDateTime.parse(
                        "2025-03-23T10:00:00"
                ))
                .visitDate(LocalDateTime.parse(
                        "2025-03-24T12:00:00"
                ))
                .status(Status.NOWE)
                .build();
    }

    @Test
    void addOrder_ShouldSaveOrder() {
        OrderCreationDTO dto = createOrderCreationDTO();

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(offer.getCost())
                .build();

        when(offerQuery.getRequiredOffer(dto.idOffer()))
                .thenReturn(offer);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentCreator.createForOrder(
                any(Order.class),
                any(Offer.class),
                any(PaymentMethod.class)
        )).thenReturn(new PaymentCreationResult(
                payment,
                null
        ));

        OrderCreationResponseDTO result =
                orderService.addOrder(dto, user);

        assertNotNull(result);
        assertEquals(
                PaymentMethod.GOTOWKA,
                result.paymentMethod()
        );
        assertEquals(
                PaymentStatus.NIE_WYMAGANA,
                result.paymentStatus()
        );
        assertNull(result.checkoutUrl());

        verify(offerQuery).getRequiredOffer(dto.idOffer());
        verify(appointmentReservation)
                .reserveSlot(dto.visitDate());
        verify(orderRepository).save(any(Order.class));

        verify(paymentCreator).createForOrder(
                any(Order.class),
                any(Offer.class),
                any(PaymentMethod.class)
        );

        verify(orderEvents).created(
                any(Order.class),
                any(Payment.class)
        );
    }

    @Test
    void addOrder_ShouldCreateBookedOfferSnapshot() {
        OrderCreationDTO dto = createOrderCreationDTO();

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(offer.getCost())
                .build();

        when(offerQuery.getRequiredOffer(dto.idOffer()))
                .thenReturn(offer);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(paymentCreator.createForOrder(
                any(Order.class),
                any(Offer.class),
                any(PaymentMethod.class)
        )).thenReturn(new PaymentCreationResult(
                payment,
                null
        ));

        orderService.addOrder(dto, user);

        verify(orderRepository).save(
                org.mockito.ArgumentMatchers.argThat(savedOrder ->
                        savedOrder.getBookedOffer() != null
                                && savedOrder.getBookedOffer()
                                .getName()
                                .equals(offer.getKind())
                                && savedOrder.getBookedOffer()
                                .getPrice()
                                .compareTo(offer.getCost()) == 0
                )
        );
    }

    @Test
    void addOrder_ShouldThrowException_WhenOfferDoesNotExist() {
        OrderCreationDTO dto = createOrderCreationDTO();

        when(offerQuery.getRequiredOffer(dto.idOffer()))
                .thenThrow(new NoSuchElementException(
                        "Oferta o ID: "
                                + dto.idOffer()
                                + " nie istnieje"
                ));

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.addOrder(dto, user)
        );

        assertEquals(
                "Oferta o ID: "
                        + dto.idOffer()
                        + " nie istnieje",
                exception.getMessage()
        );

        verify(offerQuery).getRequiredOffer(dto.idOffer());
        verifyNoInteractions(
                appointmentReservation,
                paymentCreator,
                orderEvents
        );
        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void addOrder_ShouldThrowException_WhenAppointmentSlotIsTaken() {
        OrderCreationDTO dto = createOrderCreationDTO();

        when(offerQuery.getRequiredOffer(dto.idOffer()))
                .thenReturn(offer);

        doThrow(new AppointmentSlotTakenException(dto.visitDate()))
                .when(appointmentReservation)
                .reserveSlot(dto.visitDate());

        assertThrows(
                AppointmentSlotTakenException.class,
                () -> orderService.addOrder(dto, user)
        );

        verify(offerQuery).getRequiredOffer(dto.idOffer());
        verify(appointmentReservation)
                .reserveSlot(dto.visitDate());
        verify(orderRepository, never())
                .save(any(Order.class));
        verifyNoInteractions(paymentCreator, orderEvents);
    }

    @Test
    void getAllOrders_ShouldReturnListOfOrderDTOs() {
        when(orderRepository.findAll())
                .thenReturn(List.of(order));

        List<OrderDTO> result =
                orderService.getAllOrders();

        assertEquals(1, result.size());
        assertEquals(
                order.getIdOrder(),
                result.getFirst().idOrder()
        );
        assertEquals(
                order.getBookedOffer().getName(),
                result.getFirst().offer().kind()
        );
        assertEquals(
                order.getBookedOffer().getPrice(),
                result.getFirst().offer().cost()
        );

        verify(orderRepository).findAll();
    }

    @Test
    void getSingleOrder_ShouldReturnOrderDTO_WhenOrderExists() {
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        OrderDTO result =
                orderService.getSingleOrder(1L);

        assertNotNull(result);
        assertEquals(order.getIdOrder(), result.idOrder());
        assertEquals(
                order.getBookedOffer().getName(),
                result.offer().kind()
        );
        assertEquals(
                order.getBookedOffer().getPrice(),
                result.offer().cost()
        );

        verify(orderRepository).findById(1L);
    }

    @Test
    void getSingleOrder_ShouldThrowException_WhenOrderNotFound() {
        when(orderRepository.findById(2L))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.getSingleOrder(2L)
        );

        assertEquals(
                "Zamówienie o ID: 2 nie istnieje",
                exception.getMessage()
        );

        verify(orderRepository).findById(2L);
    }

    @Test
    void getOrdersByStatus_ShouldReturnFilteredOrders() {
        when(orderRepository.findOrdersByStatus(Status.NOWE))
                .thenReturn(List.of(order));

        List<OrderDTO> result =
                orderService.getOrdersByStatus("NOWE");

        assertEquals(1, result.size());
        assertEquals(
                Status.NOWE,
                result.getFirst().status()
        );

        verify(orderRepository)
                .findOrdersByStatus(Status.NOWE);
    }

    @Test
    void getOrdersByStatus_ShouldReturnFilteredOrders_WhenStatusHasLowerCaseLetters() {
        when(orderRepository.findOrdersByStatus(Status.NOWE))
                .thenReturn(List.of(order));

        List<OrderDTO> result =
                orderService.getOrdersByStatus("nowe");

        assertEquals(1, result.size());
        assertEquals(
                Status.NOWE,
                result.getFirst().status()
        );

        verify(orderRepository)
                .findOrdersByStatus(Status.NOWE);
    }

    @Test
    void getOrdersByStatus_ShouldThrowException_ForInvalidStatus() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrdersByStatus("invalid")
        );

        assertTrue(
                exception.getMessage()
                        .contains("Dostępne statusy")
        );

        verify(orderRepository, never())
                .findOrdersByStatus(any());
    }

    @Test
    void updateOrder_ShouldUpdateExistingOrderWithoutChangingOffer() {
        OrderUpdatedRequestDTO request =
                createOrderUpdatedRequestDTO();

        LocalDateTime currentVisitDate =
                order.getVisitDate();

        Status currentStatus =
                order.getStatus();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(offerQuery.getRequiredOffer(request.idOffer()))
                .thenReturn(offer);

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderDTO result =
                orderService.updateOrder(request, 1L);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(
                        order.getIdOrder(),
                        result.idOrder()
                ),
                () -> assertEquals(
                        user.getIdUser(),
                        result.user().idUser()
                ),
                () -> assertEquals(
                        user.getFirstname(),
                        result.user().firstname()
                ),
                () -> assertEquals(
                        user.getLastname(),
                        result.user().lastname()
                ),
                () -> assertEquals(
                        user.getEmail(),
                        result.user().email()
                ),
                () -> assertEquals(
                        offer.getIdOffer(),
                        result.offer().idOffer()
                ),
                () -> assertEquals(
                        order.getBookedOffer().getName(),
                        result.offer().kind()
                ),
                () -> assertEquals(
                        order.getBookedOffer().getPrice(),
                        result.offer().cost()
                ),
                () -> assertEquals(
                        request.visitDate(),
                        result.visitDate()
                ),
                () -> assertEquals(
                        request.status(),
                        result.status()
                )
        );

        verify(offerQuery)
                .getRequiredOffer(request.idOffer());

        verifyNoInteractions(paymentOfferUpdater);

        verify(appointmentReservation)
                .updateSlotReservation(
                        currentVisitDate,
                        currentStatus,
                        request.visitDate(),
                        request.status()
                );

        verify(orderRepository).save(order);
        verify(orderEvents).updated(order, currentStatus);
    }

    @Test
    void updateOrder_ShouldUpdateBookedOffer_WhenAssignedOfferChanges() {
        Offer targetOffer = createOffer(
                2L,
                "Strzyżenie i broda",
                new BigDecimal("180.00")
        );

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(offer.getCost())
                .build();

        order.setPayment(payment);

        LocalDateTime currentVisitDate =
                order.getVisitDate();

        Status currentStatus =
                order.getStatus();

        OrderUpdatedRequestDTO request =
                new OrderUpdatedRequestDTO(
                        targetOffer.getIdOffer(),
                        LocalDateTime.of(
                                2026,
                                11,
                                10,
                                12,
                                0
                        ),
                        Status.NOWE
                );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(offerQuery.getRequiredOffer(
                targetOffer.getIdOffer()
        )).thenReturn(targetOffer);

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderDTO result =
                orderService.updateOrder(request, 1L);

        verify(paymentOfferUpdater)
                .updateAfterOfferChange(
                        payment,
                        targetOffer
                );

        verify(appointmentReservation)
                .updateSlotReservation(
                        currentVisitDate,
                        currentStatus,
                        request.visitDate(),
                        request.status()
                );

        assertSame(targetOffer, order.getOffer());

        assertEquals(
                targetOffer.getKind(),
                order.getBookedOffer().getName()
        );

        assertEquals(
                0,
                targetOffer.getCost().compareTo(
                        order.getBookedOffer().getPrice()
                )
        );

        assertEquals(
                targetOffer.getIdOffer(),
                result.offer().idOffer()
        );

        assertEquals(
                targetOffer.getKind(),
                result.offer().kind()
        );

        assertEquals(
                0,
                targetOffer.getCost().compareTo(
                        result.offer().cost()
                )
        );
    }

    @Test
    void updateOrder_ShouldPreserveBookedOffer_WhenCatalogOfferDataChanged() {
        Offer changedCatalogOffer = createOffer(
                offer.getIdOffer(),
                "Nowa nazwa katalogowa",
                new BigDecimal("999.00")
        );

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(order.getBookedOffer().getPrice())
                .build();

        order.setPayment(payment);

        String bookedName =
                order.getBookedOffer().getName();

        BigDecimal bookedPrice =
                order.getBookedOffer().getPrice();

        OrderUpdatedRequestDTO request =
                new OrderUpdatedRequestDTO(
                        changedCatalogOffer.getIdOffer(),
                        LocalDateTime.of(
                                2026,
                                11,
                                10,
                                12,
                                0
                        ),
                        Status.NOWE
                );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(offerQuery.getRequiredOffer(
                changedCatalogOffer.getIdOffer()
        )).thenReturn(changedCatalogOffer);

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderDTO result =
                orderService.updateOrder(request, 1L);

        verifyNoInteractions(paymentOfferUpdater);

        assertSame(offer, order.getOffer());

        assertEquals(
                bookedName,
                order.getBookedOffer().getName()
        );

        assertEquals(
                0,
                bookedPrice.compareTo(
                        order.getBookedOffer().getPrice()
                )
        );

        assertEquals(
                0,
                bookedPrice.compareTo(payment.getAmount())
        );

        assertEquals(bookedName, result.offer().kind());

        assertEquals(
                0,
                bookedPrice.compareTo(result.offer().cost())
        );
    }

    @Test
    void updateOrder_ShouldNotUpdateOrder_WhenPaymentRejectsOfferChange() {
        Offer targetOffer = createOffer(
                2L,
                "Strzyżenie i broda",
                new BigDecimal("180.00")
        );

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OPLACONA)
                .amount(offer.getCost())
                .build();

        order.setPayment(payment);

        OrderUpdatedRequestDTO request =
                new OrderUpdatedRequestDTO(
                        targetOffer.getIdOffer(),
                        LocalDateTime.of(
                                2026,
                                11,
                                10,
                                12,
                                0
                        ),
                        Status.NOWE
                );

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(offerQuery.getRequiredOffer(
                targetOffer.getIdOffer()
        )).thenReturn(targetOffer);

        doThrow(new OrderOfferChangeNotAllowedException(
                "Nie można zmienić oferty w opłaconym zamówieniu"
        ))
                .when(paymentOfferUpdater)
                .updateAfterOfferChange(
                        payment,
                        targetOffer
                );

        OrderOfferChangeNotAllowedException exception =
                assertThrows(
                        OrderOfferChangeNotAllowedException.class,
                        () -> orderService.updateOrder(
                                request,
                                1L
                        )
                );

        assertEquals(
                "Nie można zmienić oferty w opłaconym zamówieniu",
                exception.getMessage()
        );

        verify(orderRepository, never())
                .save(any(Order.class));

        verifyNoInteractions(
                appointmentReservation,
                orderEvents
        );

        assertSame(offer, order.getOffer());
        assertEquals(
                offer.getKind(),
                order.getBookedOffer().getName()
        );
        assertEquals(
                0,
                offer.getCost().compareTo(
                        order.getBookedOffer().getPrice()
                )
        );
    }

    @Test
    void updateOrder_ShouldUseCurrentStatus_WhenUpdatedStatusIsNull() {
        LocalDateTime targetVisitDate =
                LocalDateTime.parse(
                        "2025-03-26T10:00:00"
                );

        OrderUpdatedRequestDTO request =
                new OrderUpdatedRequestDTO(
                        offer.getIdOffer(),
                        targetVisitDate,
                        null
                );

        LocalDateTime currentVisitDate =
                order.getVisitDate();

        Status currentStatus =
                order.getStatus();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(offerQuery.getRequiredOffer(request.idOffer()))
                .thenReturn(offer);

        when(orderRepository.save(order))
                .thenReturn(order);

        OrderDTO result =
                orderService.updateOrder(request, 1L);

        assertNotNull(result);
        assertEquals(
                targetVisitDate,
                result.visitDate()
        );
        assertEquals(
                currentStatus,
                result.status()
        );

        verifyNoInteractions(paymentOfferUpdater);

        verify(appointmentReservation)
                .updateSlotReservation(
                        currentVisitDate,
                        currentStatus,
                        targetVisitDate,
                        currentStatus
                );

        verify(orderRepository).save(order);
        verify(orderEvents).updated(order, currentStatus);
    }

    @Test
    void updateOrder_ShouldThrowException_WhenOrderNotFound() {
        OrderUpdatedRequestDTO request =
                createOrderUpdatedRequestDTO();

        when(orderRepository.findById(2L))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.updateOrder(request, 2L)
        );

        assertEquals(
                "Zamówienie o ID: 2 nie istnieje",
                exception.getMessage()
        );

        verify(orderRepository).findById(2L);

        verifyNoInteractions(
                offerQuery,
                appointmentReservation,
                paymentOfferUpdater,
                orderEvents
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void updateOrder_ShouldThrowException_WhenOfferNotFound() {
        OrderUpdatedRequestDTO request =
                createOrderUpdatedRequestDTO();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        when(offerQuery.getRequiredOffer(request.idOffer()))
                .thenThrow(new NoSuchElementException(
                        "Oferta o ID: "
                                + request.idOffer()
                                + " nie istnieje"
                ));

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.updateOrder(request, 1L)
        );

        assertEquals("Oferta o ID: "
                        + request.idOffer()
                        + " nie istnieje",
                exception.getMessage()
        );

        verify(orderRepository).findById(1L);
        verify(offerQuery)
                .getRequiredOffer(request.idOffer());

        verifyNoInteractions(
                appointmentReservation,
                paymentOfferUpdater,
                orderEvents
        );

        verify(orderRepository, never())
                .save(any(Order.class));
    }

    @Test
    void deleteOrderById_ShouldDeleteExistingOrder() {
        LocalDateTime visitDate =
                order.getVisitDate();

        Status status =
                order.getStatus();

        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));

        orderService.deleteOrderById(1L);

        verify(orderRepository).findById(1L);

        verify(appointmentReservation)
                .releaseIfReserved(
                        visitDate,
                        status
                );

        verify(orderRepository).delete(order);
        verify(orderEvents).deleted(1L);
    }

    @Test
    void deleteOrderById_ShouldThrowException_WhenOrderNotExists() {
        when(orderRepository.findById(2L))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.deleteOrderById(2L)
        );

        assertEquals(
                "Zamówienie o ID: 2 nie istnieje",
                exception.getMessage()
        );

        verify(orderRepository).findById(2L);

        verifyNoInteractions(
                appointmentReservation,
                orderEvents
        );

        verify(orderRepository, never())
                .delete(any(Order.class));
    }
}