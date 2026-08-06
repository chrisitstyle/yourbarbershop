package pl.barbershopproject.barbershop.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.barbershopproject.barbershop.exception.OrderOfferChangeNotAllowedException;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.utils.OrderModificationPolicy;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.offerBuilder;

class PaymentOfferUpdateServiceTest {

    private OrderModificationPolicy orderModificationPolicy;
    private PaymentOfferUpdateService paymentOfferUpdateService;

    @BeforeEach
    void setUp() {
        orderModificationPolicy = mock(OrderModificationPolicy.class);

        paymentOfferUpdateService =
                new PaymentOfferUpdateService(
                        orderModificationPolicy
                );
    }

    @Test
    void shouldUpdatePaymentAmountWhenPolicyAllowsOfferChange() {
        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(new BigDecimal("50.00"))
                .build();

        Offer newOffer = offerBuilder()
                .cost(new BigDecimal("80.00"))
                .build();

        paymentOfferUpdateService.updateAfterOfferChange(
                payment,
                newOffer
        );

        verify(orderModificationPolicy).validateOfferChange(payment);

        assertThat(payment.getAmount())
                .isEqualByComparingTo("80.00");
    }

    @Test
    void shouldNotUpdatePaymentAmountWhenPolicyRejectsOfferChange() {
        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OPLACONA)
                .amount(new BigDecimal("50.00"))
                .build();

        Offer newOffer = offerBuilder()
                .cost(new BigDecimal("80.00"))
                .build();

        doThrow(new OrderOfferChangeNotAllowedException(
                "Nie można zmienić oferty w opłaconym lub zwróconym zamówieniu"
        ))
                .when(orderModificationPolicy)
                .validateOfferChange(payment);

        assertThatThrownBy(() ->
                paymentOfferUpdateService.updateAfterOfferChange(
                        payment,
                        newOffer
                )
        )
                .isInstanceOf(
                        OrderOfferChangeNotAllowedException.class
                )
                .hasMessage(
                        "Nie można zmienić oferty w opłaconym lub zwróconym zamówieniu"
                );

        verify(orderModificationPolicy)
                .validateOfferChange(payment);

        assertThat(payment.getAmount())
                .isEqualByComparingTo("50.00");
    }

    @Test
    void shouldThrowExceptionWhenPaymentIsNull() {
        Offer newOffer = offerBuilder()
                .cost(new BigDecimal("80.00"))
                .build();

        assertThatThrownBy(() ->
                paymentOfferUpdateService.updateAfterOfferChange(
                        null,
                        newOffer
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Płatność zamówienia nie może być null"
                );

        verifyNoInteractions(orderModificationPolicy);
    }

    @Test
    void shouldThrowExceptionWhenNewOfferIsNull() {
        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(new BigDecimal("50.00"))
                .build();

        assertThatThrownBy(() ->
                paymentOfferUpdateService.updateAfterOfferChange(
                        payment,
                        null
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Nowa oferta nie może być null"
                );

        verifyNoInteractions(orderModificationPolicy);

        assertThat(payment.getAmount())
                .isEqualByComparingTo("50.00");
    }

    @Test
    void shouldThrowExceptionWhenNewOfferPriceIsNull() {
        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(new BigDecimal("50.00"))
                .build();

        Offer newOffer = offerBuilder()
                .cost(null)
                .build();

        assertThatThrownBy(() ->
                paymentOfferUpdateService.updateAfterOfferChange(
                        payment,
                        newOffer
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Cena nowej oferty nie może być null"
                );

        verifyNoInteractions(orderModificationPolicy);

        assertThat(payment.getAmount())
                .isEqualByComparingTo("50.00");
    }
}