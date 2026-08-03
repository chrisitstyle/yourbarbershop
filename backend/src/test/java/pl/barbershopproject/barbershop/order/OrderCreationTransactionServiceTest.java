package pl.barbershopproject.barbershop.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.exception.AppointmentSlotTakenException;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.event.OrderEvents;
import pl.barbershopproject.barbershop.payment.*;
import pl.barbershopproject.barbershop.user.User;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities.createOrderCreationDTO;
import static pl.barbershopproject.barbershop.utils.testentities.UserTestEntities.createUser;

@ExtendWith(MockitoExtension.class)
class OrderCreationTransactionServiceTest {

    private static final Long ORDER_ID = 1L;
    private static final Long PAYMENT_ID = 10L;
    private static final ZoneId TEST_ZONE = ZoneId.of("Europe/Warsaw");
    private static final Instant TEST_INSTANT = Instant.parse("2026-01-16T12:00:00Z");

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OfferQuery offerQuery;
    @Mock
    private AppointmentReservation appointmentReservation;
    @Mock
    private PaymentCreator paymentCreator;
    @Mock
    private OrderEvents orderEvents;

    private OrderCreationTransactionService orderCreationTransactionService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TEST_INSTANT, TEST_ZONE);

        orderCreationTransactionService = new OrderCreationTransactionService(
                orderRepository,
                offerQuery,
                appointmentReservation,
                paymentCreator,
                orderEvents,
                clock
        );
    }

    @Test
    void create_ShouldPersistOrderCreatePaymentAndReturnTransactionResult() {
        OrderCreationDTO orderCreationDTO = createOrderCreationDTO();
        Offer offer = createOffer();
        User user = createUser();

        Payment payment = Payment.builder()
                .idPayment(PAYMENT_ID)
                .paymentMethod(orderCreationDTO.paymentMethod())
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(offer.getCost())
                .currency("PLN")
                .build();

        PaymentCheckoutRequest checkoutRequest = new PaymentCheckoutRequest(
                PAYMENT_ID,
                orderCreationDTO.paymentMethod(),
                PaymentStatus.NIE_WYMAGANA,
                offer.getCost(),
                "PLN",
                offer.getKind()
        );

        PaymentCreationResult paymentCreationResult = new PaymentCreationResult(payment, checkoutRequest);

        when(offerQuery.getRequiredOffer(orderCreationDTO.idOffer()))
                .thenReturn(offer);

        givenRepositorySavesOrderWithId();

        when(paymentCreator.createForOrder(
                any(Order.class),
                eq(orderCreationDTO.paymentMethod())
        )).thenReturn(paymentCreationResult);

        OrderCreationTransactionResult result = orderCreationTransactionService.create(orderCreationDTO, user);

        assertThat(result.orderId()).isEqualTo(ORDER_ID);
        assertThat(result.checkoutRequest()).isSameAs(checkoutRequest);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertThat(savedOrder.getIdOrder()).isEqualTo(ORDER_ID);
        assertThat(savedOrder.getUser()).isSameAs(user);
        assertThat(savedOrder.getOffer()).isSameAs(offer);
        assertThat(savedOrder.getVisitDate())
                .isEqualTo(orderCreationDTO.visitDate());

        assertThat(savedOrder.getBookedOffer()).isNotNull();
        assertThat(savedOrder.getBookedOffer().getName())
                .isEqualTo(offer.getKind());
        assertThat(savedOrder.getBookedOffer().getPrice())
                .isEqualByComparingTo(offer.getCost());

        verify(offerQuery)
                .getRequiredOffer(orderCreationDTO.idOffer());

        verify(appointmentReservation)
                .reserveSlot(orderCreationDTO.visitDate());

        verify(paymentCreator).createForOrder(
                savedOrder,
                orderCreationDTO.paymentMethod()
        );

        verify(orderEvents).created(savedOrder, payment);
    }

    @Test
    void create_ShouldNotReserveSlot_WhenOfferDoesNotExist() {
        OrderCreationDTO orderCreationDTO = createOrderCreationDTO();
        User user = createUser();

        when(offerQuery.getRequiredOffer(orderCreationDTO.idOffer()))
                .thenThrow(new NoSuchElementException(
                        "Oferta o ID: "
                                + orderCreationDTO.idOffer()
                                + " nie istnieje"
                ));

        assertThatThrownBy(() ->
                orderCreationTransactionService.create(orderCreationDTO, user)
        )
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage(
                        "Oferta o ID: "
                                + orderCreationDTO.idOffer()
                                + " nie istnieje"
                );

        verify(offerQuery)
                .getRequiredOffer(orderCreationDTO.idOffer());

        verifyNoInteractions(
                appointmentReservation,
                orderRepository,
                paymentCreator,
                orderEvents
        );
    }

    @Test
    void create_ShouldNotPersistOrder_WhenAppointmentSlotIsTaken() {
        OrderCreationDTO orderCreationDTO = createOrderCreationDTO();
        Offer offer = createOffer();
        User user = createUser();

        when(offerQuery.getRequiredOffer(orderCreationDTO.idOffer()))
                .thenReturn(offer);

        doThrow(new AppointmentSlotTakenException(
                orderCreationDTO.visitDate()
        ))
                .when(appointmentReservation)
                .reserveSlot(orderCreationDTO.visitDate());

        assertThatThrownBy(() ->
                orderCreationTransactionService.create(
                        orderCreationDTO,
                        user
                )
        ).isInstanceOf(AppointmentSlotTakenException.class);

        verify(offerQuery)
                .getRequiredOffer(orderCreationDTO.idOffer());

        verify(appointmentReservation)
                .reserveSlot(orderCreationDTO.visitDate());

        verifyNoInteractions(
                orderRepository,
                paymentCreator,
                orderEvents
        );
    }

    @Test
    void create_ShouldNotPublishEvent_WhenPaymentCreationFails() {
        OrderCreationDTO orderCreationDTO = createOrderCreationDTO();
        Offer offer = createOffer();
        User user = createUser();

        when(offerQuery.getRequiredOffer(orderCreationDTO.idOffer()))
                .thenReturn(offer);

        givenRepositorySavesOrderWithId();

        when(paymentCreator.createForOrder(
                any(Order.class),
                eq(orderCreationDTO.paymentMethod())
        )).thenThrow(new IllegalStateException(
                "Nie udało się utworzyć płatności"
        ));

        assertThatThrownBy(() ->
                orderCreationTransactionService.create(
                        orderCreationDTO,
                        user
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Nie udało się utworzyć płatności");

        verify(appointmentReservation)
                .reserveSlot(orderCreationDTO.visitDate());

        verify(orderRepository).save(any(Order.class));

        verify(paymentCreator).createForOrder(
                any(Order.class),
                eq(orderCreationDTO.paymentMethod())
        );

        verify(orderEvents, never()).created(
                any(Order.class),
                any(Payment.class)
        );
    }

    private void givenRepositorySavesOrderWithId() {
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order savedOrder = invocation.getArgument(0);
                    savedOrder.setIdOrder(ORDER_ID);
                    return savedOrder;
                });
    }
}
