package pl.barbershopproject.barbershop.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import pl.barbershopproject.barbershop.exception.OrderOfferChangeNotAllowedException;
import pl.barbershopproject.barbershop.offer.Offer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.offerBuilder;

class PaymentOfferUpdateServiceTest {

    private PaymentOfferUpdateService paymentOfferUpdateService;

    @BeforeEach
    void setUp() {
        paymentOfferUpdateService = new PaymentOfferUpdateService();
    }

    @ParameterizedTest
    @EnumSource(
            value = PaymentMethod.class,
            names = {
                    "GOTOWKA",
                    "KARTA_NA_MIEJSCU"
            }
    )
    void shouldUpdatePaymentAmountForOfflinePayment(
            PaymentMethod paymentMethod
    ) {
        Payment payment = Payment.builder()
                .paymentMethod(paymentMethod)
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

        assertThat(payment.getAmount())
                .isEqualByComparingTo("80.00");
    }

    @Test
    void shouldRejectOfferChangeForPaidPayment() {
        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OPLACONA)
                .amount(new BigDecimal("50.00"))
                .build();

        Offer newOffer = offerBuilder()
                .cost(new BigDecimal("80.00"))
                .build();

        assertThatThrownBy(() ->
                paymentOfferUpdateService.updateAfterOfferChange(
                        payment,
                        newOffer
                )
        )
                .isInstanceOf(OrderOfferChangeNotAllowedException.class)
                .hasMessage(
                        "Nie można zmienić oferty w opłaconym zamówieniu"
                );

        assertThat(payment.getAmount())
                .isEqualByComparingTo("50.00");
    }

    @Test
    void shouldRejectOfferChangeForRefundedPayment() {
        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.ZWROCONA)
                .amount(new BigDecimal("50.00"))
                .build();

        Offer newOffer = offerBuilder()
                .cost(new BigDecimal("80.00"))
                .build();

        assertThatThrownBy(() ->
                paymentOfferUpdateService.updateAfterOfferChange(
                        payment,
                        newOffer
                )
        )
                .isInstanceOf(OrderOfferChangeNotAllowedException.class)
                .hasMessage(
                        "Nie można zmienić oferty w opłaconym zamówieniu"
                );

        assertThat(payment.getAmount())
                .isEqualByComparingTo("50.00");
    }

    @Test
    void shouldRejectOfferChangeForPendingOnlinePayment() {
        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OCZEKUJE_NA_PLATNOSC)
                .amount(new BigDecimal("50.00"))
                .stripeCheckoutSessionId("cs_test_123")
                .build();

        Offer newOffer = offerBuilder()
                .cost(new BigDecimal("80.00"))
                .build();

        assertThatThrownBy(() ->
                paymentOfferUpdateService.updateAfterOfferChange(
                        payment,
                        newOffer
                )
        )
                .isInstanceOf(OrderOfferChangeNotAllowedException.class)
                .hasMessage(
                        "Nie można zmienić oferty, gdy płatność online oczekuje na zakończenie"
                );

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
                .hasMessage("Płatność zamówienia nie może być null");
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
                .hasMessage("Nowa oferta nie może być null");

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
                .hasMessage("Cena nowej oferty nie może być null");

        assertThat(payment.getAmount())
                .isEqualByComparingTo("50.00");
    }
}
