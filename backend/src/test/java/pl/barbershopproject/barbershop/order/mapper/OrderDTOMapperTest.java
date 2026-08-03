package pl.barbershopproject.barbershop.order.mapper;

import org.junit.jupiter.api.Test;
import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createBookedOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities.orderBuilder;

class OrderDTOMapperTest {

    @Test
    void toDTO_ShouldUseBookedOfferNameAndPrice() {
        Offer currentCatalogOffer = createOffer(1L,"Aktualna nazwa oferty",
                new BigDecimal("200.00")
        );

        BookedOffer bookedOffer = createBookedOffer(
                "Historyczna nazwa oferty",
                new BigDecimal("120.00")
        );

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(new BigDecimal("120.00"))
                .build();

        Order order = orderBuilder()
                .offer(currentCatalogOffer)
                .bookedOffer(bookedOffer)
                .payment(payment)
                .build();

        OrderDTO result = OrderDTOMapper.toDTO(order);

        assertAll(
                () -> assertEquals(
                        currentCatalogOffer.getIdOffer(),
                        result.offer().idOffer()
                ),
                () -> assertEquals(
                        bookedOffer.getName(),
                        result.offer().kind()
                ),
                () -> assertEquals(
                        0,
                        bookedOffer.getPrice().compareTo(
                                result.offer().cost()
                        )
                ),
                () -> assertEquals(
                        PaymentMethod.GOTOWKA,
                        result.paymentMethod()
                ),
                () -> assertEquals(
                        PaymentStatus.NIE_WYMAGANA,
                        result.paymentStatus()
                )
        );
    }

    @Test
    void toDTO_ShouldMapOrderAndUserDetails() {
        Order order = orderBuilder().build();

        OrderDTO result = OrderDTOMapper.toDTO(order);

        assertAll(
                () -> assertEquals(
                        order.getIdOrder(),
                        result.idOrder()
                ),
                () -> assertEquals(
                        order.getUser().getIdUser(),
                        result.user().idUser()
                ),
                () -> assertEquals(
                        order.getUser().getFirstname(),
                        result.user().firstname()
                ),
                () -> assertEquals(
                        order.getUser().getLastname(),
                        result.user().lastname()
                ),
                () -> assertEquals(
                        order.getUser().getEmail(),
                        result.user().email()
                ),
                () -> assertEquals(
                        order.getOrderDate(),
                        result.orderDate()
                ),
                () -> assertEquals(
                        order.getVisitDate(),
                        result.visitDate()
                ),
                () -> assertEquals(
                        order.getStatus(),
                        result.status()
                )
        );
    }

    @Test
    void toDTO_ShouldReturnNullPaymentDetails_WhenOrderHasNoPayment() {
        Order order = orderBuilder()
                .payment(null)
                .build();

        OrderDTO result = OrderDTOMapper.toDTO(order);

        assertNull(result.paymentMethod());
        assertNull(result.paymentStatus());
    }
}
