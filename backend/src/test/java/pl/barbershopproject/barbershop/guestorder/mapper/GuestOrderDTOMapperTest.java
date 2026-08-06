package pl.barbershopproject.barbershop.guestorder.mapper;

import org.junit.jupiter.api.Test;
import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderDTO;
import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static pl.barbershopproject.barbershop.utils.testentities.GuestOrderTestEntities.guestOrderBuilder;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createBookedOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;

class GuestOrderDTOMapperTest {

    @Test
    void toDTO_ShouldUseBookedOfferNameAndPrice() {
        Offer currentCatalogOffer = createOffer(
                1L,
                "Aktualna nazwa oferty",
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

        GuestOrder guestOrder = guestOrderBuilder()
                .offer(currentCatalogOffer)
                .bookedOffer(bookedOffer)
                .payment(payment)
                .build();

        GuestOrderDTO result =
                GuestOrderDTOMapper.toDTO(guestOrder);

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
    void toDTO_ShouldMapGuestOrderDetails() {
        GuestOrder guestOrder =
                guestOrderBuilder().build();

        GuestOrderDTO result =
                GuestOrderDTOMapper.toDTO(guestOrder);

        assertAll(
                () -> assertEquals(
                        guestOrder.getIdGuestOrder(),
                        result.idGuestOrder()
                ),
                () -> assertEquals(
                        guestOrder.getFirstname(),
                        result.firstname()
                ),
                () -> assertEquals(
                        guestOrder.getLastname(),
                        result.lastname()
                ),
                () -> assertEquals(
                        guestOrder.getPhonenumber(),
                        result.phonenumber()
                ),
                () -> assertEquals(
                        guestOrder.getEmail(),
                        result.email()
                ),
                () -> assertEquals(
                        guestOrder.getOffer().getIdOffer(),
                        result.offer().idOffer()
                ),
                () -> assertEquals(
                        guestOrder.getBookedOffer().getName(),
                        result.offer().kind()
                ),
                () -> assertEquals(
                        0,
                        guestOrder.getBookedOffer()
                                .getPrice()
                                .compareTo(result.offer().cost())
                ),
                () -> assertEquals(
                        guestOrder.getOrderDate(),
                        result.orderDate()
                ),
                () -> assertEquals(
                        guestOrder.getVisitDate(),
                        result.visitDate()
                ),
                () -> assertEquals(
                        guestOrder.getOrderStatus(),
                        result.orderStatus()
                )
        );
    }

    @Test
    void toDTO_ShouldReturnNullPaymentDetails_WhenGuestOrderHasNoPayment() {
        GuestOrder guestOrder = guestOrderBuilder()
                .payment(null)
                .build();

        GuestOrderDTO result =
                GuestOrderDTOMapper.toDTO(guestOrder);

        assertNull(result.paymentMethod());
        assertNull(result.paymentStatus());
    }
}
