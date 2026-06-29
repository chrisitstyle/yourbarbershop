package pl.barbershopproject.barbershop.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pl.barbershopproject.barbershop.appointment.AppointmentAvailabilityService;
import pl.barbershopproject.barbershop.exception.AppointmentSlotTakenException;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.dto.OrderCreationResponseDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.dto.OrderUpdatedRequestDTO;
import pl.barbershopproject.barbershop.order.event.OrderCreatedEvent;
import pl.barbershopproject.barbershop.payment.*;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.util.Status;
import pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities;
import pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities;
import pl.barbershopproject.barbershop.utils.testentities.UserTestEntities;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final ZoneId TEST_ZONE = ZoneId.of("Europe/Warsaw");
    private static final Instant TEST_INSTANT = Instant.parse("2026-01-16T12:00:00Z");

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private AppointmentAvailabilityService appointmentAvailabilityService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private StripeCheckoutService stripeCheckoutService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private User user;
    private Offer offer;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(TEST_INSTANT);
        lenient().when(clock.getZone()).thenReturn(TEST_ZONE);

        offer = OfferTestEntities.createOffer();
        user = UserTestEntities.createUser();

        order = new Order();
        order.setIdOrder(1L);
        order.setUser(user);
        order.setOffer(offer);
        order.setOrderDate(LocalDateTime.parse("2025-03-23T10:00:00"));
        order.setVisitDate(LocalDateTime.parse("2025-03-24T12:00:00"));
        order.setStatus(Status.NOWE);
    }

    @Test
    void addOrder_ShouldSaveOrder() {
        OrderCreationDTO dto = OrderTestEntities.createOrderCreationDTO();

        when(offerRepository.findById(dto.idOffer())).thenReturn(Optional.of(offer));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderCreationResponseDTO result = orderService.addOrder(dto, user);

        assertNotNull(result);
        assertEquals(PaymentMethod.GOTOWKA, result.paymentMethod());
        assertEquals(PaymentStatus.OCZEKUJE_NA_PLATNOSC, result.paymentStatus());
        assertNull(result.checkoutUrl());

        verify(offerRepository, times(1)).findById(dto.idOffer());
        verify(appointmentAvailabilityService, times(1)).reserveSlot(dto.visitDate());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(eventPublisher, times(1)).publishEvent(any(OrderCreatedEvent.class));
        verifyNoInteractions(stripeCheckoutService);
    }

    @Test
    void addOrder_ShouldThrowException_WhenOfferDoesNotExist() {
        OrderCreationDTO dto = OrderTestEntities.createOrderCreationDTO();

        when(offerRepository.findById(dto.idOffer())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.addOrder(dto, user)
        );

        assertEquals("Oferta o ID: " + dto.idOffer() + " nie istnieje", exception.getMessage());

        verify(offerRepository, times(1)).findById(dto.idOffer());
        verify(appointmentAvailabilityService, never()).reserveSlot(any());
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publishEvent(any());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void addOrder_ShouldThrowException_WhenAppointmentSlotIsTaken() {
        OrderCreationDTO orderCreationDTO = OrderTestEntities.createOrderCreationDTO();

        when(offerRepository.findById(orderCreationDTO.idOffer())).thenReturn(Optional.of(offer));
        doThrow(new AppointmentSlotTakenException(orderCreationDTO.visitDate()))
                .when(appointmentAvailabilityService)
                .reserveSlot(orderCreationDTO.visitDate());

        assertThrows(
                AppointmentSlotTakenException.class,
                () -> orderService.addOrder(orderCreationDTO, user)
        );

        verify(offerRepository, times(1)).findById(orderCreationDTO.idOffer());
        verify(appointmentAvailabilityService, times(1)).reserveSlot(orderCreationDTO.visitDate());
        verify(orderRepository, never()).save(any(Order.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void getAllOrders_ShouldReturnListOfOrderDTOs() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getAllOrders();

        assertEquals(1, result.size());
        assertEquals(order.getIdOrder(), result.getFirst().idOrder());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getSingleOrder_ShouldReturnOrderDTO_WhenOrderExists() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDTO result = orderService.getSingleOrder(1L);

        assertNotNull(result);
        assertEquals(order.getIdOrder(), result.idOrder());

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getSingleOrder_ShouldThrowException_WhenOrderNotFound() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.getSingleOrder(2L)
        );

        assertEquals("Zamówienie o ID: 2 nie istnieje", exception.getMessage());

        verify(orderRepository, times(1)).findById(2L);
    }

    @Test
    void getOrdersByStatus_ShouldReturnFilteredOrders() {
        when(orderRepository.findOrdersByStatus(Status.NOWE))
                .thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrdersByStatus("NOWE");

        assertEquals(1, result.size());
        assertEquals(Status.NOWE, result.getFirst().status());

        verify(orderRepository, times(1)).findOrdersByStatus(Status.NOWE);
    }

    @Test
    void getOrdersByStatus_ShouldReturnFilteredOrders_WhenStatusHasLowerCaseLetters() {
        when(orderRepository.findOrdersByStatus(Status.NOWE))
                .thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getOrdersByStatus("nowe");

        assertEquals(1, result.size());
        assertEquals(Status.NOWE, result.getFirst().status());

        verify(orderRepository, times(1)).findOrdersByStatus(Status.NOWE);
    }

    @Test
    void getOrdersByStatus_ShouldThrowException_ForInvalidStatus() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.getOrdersByStatus("invalid")
        );

        assertTrue(exception.getMessage().contains("Dostępne statusy"));

        verify(orderRepository, never()).findOrdersByStatus(any());
    }

    @Test
    void updateOrder_ShouldUpdateExistingOrder() {
        OrderUpdatedRequestDTO updatedOrder = OrderTestEntities.createOrderUpdatedRequestDTO();

        LocalDateTime currentVisitDate = order.getVisitDate();
        Status currentStatus = order.getStatus();

        LocalDateTime targetVisitDate = updatedOrder.visitDate();
        Status targetStatus = updatedOrder.status();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(offerRepository.findById(updatedOrder.idOffer())).thenReturn(Optional.of(offer));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO updatedOrderResult = orderService.updateOrder(updatedOrder, 1L);

        assertNotNull(updatedOrderResult);
        assertAll(
                () -> assertEquals(order.getIdOrder(), updatedOrderResult.idOrder()),
                () -> assertEquals(user.getIdUser(), updatedOrderResult.user().idUser()),
                () -> assertEquals(user.getFirstname(), updatedOrderResult.user().firstname()),
                () -> assertEquals(user.getLastname(), updatedOrderResult.user().lastname()),
                () -> assertEquals(user.getEmail(), updatedOrderResult.user().email()),
                () -> assertEquals(offer, updatedOrderResult.offer()),
                () -> assertEquals(order.getOrderDate(), updatedOrderResult.orderDate()),
                () -> assertEquals(targetVisitDate, updatedOrderResult.visitDate()),
                () -> assertEquals(targetStatus, updatedOrderResult.status())
        );

        verify(orderRepository, times(1)).findById(1L);
        verify(offerRepository, times(1)).findById(updatedOrder.idOffer());
        verify(appointmentAvailabilityService, times(1)).updateSlotReservation(
                currentVisitDate,
                currentStatus,
                targetVisitDate,
                targetStatus
        );
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateOrder_ShouldUseCurrentStatus_WhenUpdatedStatusIsNull() {
        LocalDateTime targetVisitDate = LocalDateTime.parse("2025-03-26T10:00:00");

        OrderUpdatedRequestDTO updatedOrder = new OrderUpdatedRequestDTO(
                1L,
                targetVisitDate,
                null
        );

        LocalDateTime currentVisitDate = order.getVisitDate();
        Status currentStatus = order.getStatus();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(offerRepository.findById(updatedOrder.idOffer())).thenReturn(Optional.of(offer));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO updatedOrderResult = orderService.updateOrder(updatedOrder, 1L);

        assertNotNull(updatedOrderResult);
        assertEquals(targetVisitDate, updatedOrderResult.visitDate());
        assertEquals(currentStatus, updatedOrderResult.status());

        verify(orderRepository, times(1)).findById(1L);
        verify(offerRepository, times(1)).findById(updatedOrder.idOffer());
        verify(appointmentAvailabilityService, times(1)).updateSlotReservation(
                currentVisitDate,
                currentStatus,
                targetVisitDate,
                currentStatus
        );
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateOrder_ShouldThrowException_WhenOrderNotFound() {
        OrderUpdatedRequestDTO updatedOrder = OrderTestEntities.createOrderUpdatedRequestDTO();

        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.updateOrder(updatedOrder, 2L)
        );

        assertEquals("Zamówienie o ID: 2 nie istnieje", exception.getMessage());

        verify(orderRepository, times(1)).findById(2L);
        verify(offerRepository, never()).findById(any());
        verify(appointmentAvailabilityService, never()).updateSlotReservation(any(), any(), any(), any());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrder_ShouldThrowException_WhenOfferNotFound() {
        OrderUpdatedRequestDTO updatedOrder = OrderTestEntities.createOrderUpdatedRequestDTO();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(offerRepository.findById(updatedOrder.idOffer())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.updateOrder(updatedOrder, 1L)
        );

        assertEquals("Oferta o ID: " + updatedOrder.idOffer() + " nie istnieje", exception.getMessage());

        verify(orderRepository, times(1)).findById(1L);
        verify(offerRepository, times(1)).findById(updatedOrder.idOffer());
        verify(appointmentAvailabilityService, never()).updateSlotReservation(any(), any(), any(), any());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void deleteOrderById_ShouldDeleteExistingOrder() {
        LocalDateTime visitDate = order.getVisitDate();
        Status status = order.getStatus();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrderById(1L);

        verify(orderRepository, times(1)).findById(1L);
        verify(appointmentAvailabilityService, times(1)).releaseIfReserved(visitDate, status);
        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    void deleteOrderById_ShouldThrowException_WhenOrderNotExists() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderService.deleteOrderById(2L)
        );

        assertEquals("Zamówienie o ID: 2 nie istnieje", exception.getMessage());

        verify(orderRepository, times(1)).findById(2L);
        verify(appointmentAvailabilityService, never()).releaseIfReserved(any(), any());
        verify(orderRepository, never()).delete(any(Order.class));
    }
}