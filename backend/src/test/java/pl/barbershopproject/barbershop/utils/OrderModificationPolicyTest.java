package pl.barbershopproject.barbershop.utils;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import pl.barbershopproject.barbershop.exception.OrderModificationNotAllowedException;
import pl.barbershopproject.barbershop.exception.OrderOfferChangeNotAllowedException;
import pl.barbershopproject.barbershop.exception.OrderStatusChangeNotAllowedException;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderModificationPolicyTest {

    private final OrderModificationPolicy orderModificationPolicy =
            new OrderModificationPolicy();

    @Nested
    class UpdateValidation {

        @Test
        void shouldAllowUpdatingNewOrderWithoutChangingStatus() {
            Payment payment = createPayment(
                    PaymentMethod.KARTA_ONLINE,
                    PaymentStatus.OPLACONA
            );

            assertThatCode(() -> orderModificationPolicy.validateUpdate(
                            OrderStatus.NOWE,
                            OrderStatus.NOWE,
                            payment)
            ).doesNotThrowAnyException();
        }

        @ParameterizedTest
        @EnumSource(
                value = OrderStatus.class,
                names = {
                        "ZREALIZOWANE",
                        "ANULOWANE"
                }
        )
        void shouldRejectUpdatingOrderWithTerminalStatus(
                OrderStatus currentOrderStatus
        ) {
            Payment payment = createPayment(
                    PaymentMethod.GOTOWKA,
                    PaymentStatus.NIE_WYMAGANA
            );

            assertThatThrownBy(() ->
                    orderModificationPolicy.validateUpdate(
                            currentOrderStatus,
                            currentOrderStatus,
                            payment
                    )
            )
                    .isInstanceOf(
                            OrderModificationNotAllowedException.class
                    )
                    .hasMessageContaining(
                            currentOrderStatus.name()
                    );
        }

        @ParameterizedTest
        @CsvSource({
                "KARTA_ONLINE, OPLACONA",
                "GOTOWKA, NIE_WYMAGANA",
                "KARTA_NA_MIEJSCU, NIE_WYMAGANA"
        })
        void shouldAllowCompletingOrderWhenPaymentIsSettled(
                PaymentMethod paymentMethod,
                PaymentStatus paymentStatus
        ) {
            Payment payment = createPayment(
                    paymentMethod,
                    paymentStatus
            );

            assertThatCode(() -> orderModificationPolicy.validateUpdate(
                            OrderStatus.NOWE,
                            OrderStatus.ZREALIZOWANE,
                            payment
                    )
            ).doesNotThrowAnyException();
        }

        @ParameterizedTest
        @CsvSource({
                "KARTA_ONLINE, OCZEKUJE_NA_PLATNOSC",
                "KARTA_ONLINE, NIEUDANA",
                "KARTA_ONLINE, WYGASLA",
                "KARTA_ONLINE, ZWROCONA",
                "GOTOWKA, NIEUDANA",
                "GOTOWKA, WYGASLA",
                "KARTA_NA_MIEJSCU, NIEUDANA",
                "KARTA_NA_MIEJSCU, WYGASLA"
        })
        void shouldRejectCompletingOrderWhenPaymentIsNotSettled(
                PaymentMethod paymentMethod,
                PaymentStatus paymentStatus
        ) {
            Payment payment = createPayment(
                    paymentMethod,
                    paymentStatus
            );

            assertThatThrownBy(() -> orderModificationPolicy.validateUpdate(
                            OrderStatus.NOWE,
                            OrderStatus.ZREALIZOWANE,
                            payment))
                    .isInstanceOf(
                            OrderStatusChangeNotAllowedException.class
                    )
                    .hasMessage(
                            "Nie można zrealizować zamówienia przed rozliczeniem płatności"
                    );
        }

        @ParameterizedTest
        @EnumSource(PaymentMethod.class)
        void shouldRejectCancellingPaidOrder(
                PaymentMethod paymentMethod
        ) {
            Payment payment = createPayment(
                    paymentMethod,
                    PaymentStatus.OPLACONA
            );

            assertThatThrownBy(() -> orderModificationPolicy.validateUpdate(
                            OrderStatus.NOWE,
                            OrderStatus.ANULOWANE,
                            payment))
                    .isInstanceOf(
                            OrderStatusChangeNotAllowedException.class
                    )
                    .hasMessage(
                            "Nie można anulować opłaconego zamówienia bez wykonania zwrotu"
                    );
        }

        @Test
        void shouldRejectCancellingOrderWithPendingOnlinePayment() {
            Payment payment = createPayment(
                    PaymentMethod.KARTA_ONLINE,
                    PaymentStatus.OCZEKUJE_NA_PLATNOSC
            );

            assertThatThrownBy(() -> orderModificationPolicy.validateUpdate(
                            OrderStatus.NOWE,
                            OrderStatus.ANULOWANE,
                            payment))
                    .isInstanceOf(
                            OrderStatusChangeNotAllowedException.class
                    )
                    .hasMessage(
                            "Nie można anulować zamówienia podczas aktywnej płatności online"
                    );
        }

        @ParameterizedTest
        @CsvSource({
                "GOTOWKA, NIE_WYMAGANA",
                "KARTA_NA_MIEJSCU, NIE_WYMAGANA",
                "KARTA_ONLINE, NIEUDANA",
                "KARTA_ONLINE, WYGASLA"
        })
        void shouldAllowCancellingOrderWithoutCompletedOrPendingPayment(
                PaymentMethod paymentMethod,
                PaymentStatus paymentStatus
        ) {
            Payment payment = createPayment(
                    paymentMethod,
                    paymentStatus
            );

            assertThatCode(() -> orderModificationPolicy.validateUpdate(
                            OrderStatus.NOWE,
                            OrderStatus.ANULOWANE,
                            payment
                    )
            ).doesNotThrowAnyException();
        }

        @Test
        void shouldRejectUpdateWhenCurrentOrderStatusIsNull() {
            Payment payment = createPayment(
                    PaymentMethod.GOTOWKA,
                    PaymentStatus.NIE_WYMAGANA
            );

            assertThatThrownBy(() -> orderModificationPolicy.validateUpdate(
                            null,
                            OrderStatus.NOWE,
                            payment))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage(
                            "Aktualny status zamówienia nie może być null");
        }

        @Test
        void shouldRejectUpdateWhenTargetOrderStatusIsNull() {
            Payment payment = createPayment(
                    PaymentMethod.GOTOWKA,
                    PaymentStatus.NIE_WYMAGANA
            );

            assertThatThrownBy(() -> orderModificationPolicy.validateUpdate(
                            OrderStatus.NOWE,
                            null,
                            payment))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage(
                            "Docelowy status zamówienia nie może być null");
        }

        @Test
        void shouldRejectUpdateWhenPaymentIsNull() {
            assertThatThrownBy(() -> orderModificationPolicy.validateUpdate(
                            OrderStatus.NOWE,
                            OrderStatus.NOWE,
                            null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage(
                            "Płatność zamówienia nie może być null");
        }
    }

    @Nested
    class OfferChangeValidation {

        @ParameterizedTest
        @EnumSource(PaymentMethod.class)
        void shouldRejectOfferChangeForPaidPayment(
                PaymentMethod paymentMethod
        ) {
            Payment payment = createPayment(
                    paymentMethod,
                    PaymentStatus.OPLACONA
            );

            assertThatThrownBy(() -> orderModificationPolicy.validateOfferChange(payment))
                    .isInstanceOf(
                            OrderOfferChangeNotAllowedException.class
                    )
                    .hasMessage(
                            "Nie można zmienić oferty w opłaconym lub zwróconym zamówieniu");
        }

        @ParameterizedTest
        @EnumSource(PaymentMethod.class)
        void shouldRejectOfferChangeForRefundedPayment(
                PaymentMethod paymentMethod
        ) {
            Payment payment = createPayment(
                    paymentMethod,
                    PaymentStatus.ZWROCONA
            );

            assertThatThrownBy(() -> orderModificationPolicy.validateOfferChange(payment))
                    .isInstanceOf(
                            OrderOfferChangeNotAllowedException.class
                    )
                    .hasMessage(
                            "Nie można zmienić oferty w opłaconym lub zwróconym zamówieniu"
                    );
        }

        @Test
        void shouldRejectOfferChangeForPendingOnlinePayment() {
            Payment payment = createPayment(
                    PaymentMethod.KARTA_ONLINE,
                    PaymentStatus.OCZEKUJE_NA_PLATNOSC
            );

            assertThatThrownBy(() -> orderModificationPolicy.validateOfferChange(payment))
                    .isInstanceOf(
                            OrderOfferChangeNotAllowedException.class
                    )
                    .hasMessage(
                            "Nie można zmienić oferty, gdy płatność online oczekuje na zakończenie");
        }

        @ParameterizedTest
        @CsvSource({
                "GOTOWKA, NIE_WYMAGANA",
                "KARTA_NA_MIEJSCU, NIE_WYMAGANA",
                "KARTA_ONLINE, NIEUDANA",
                "KARTA_ONLINE, WYGASLA"
        })
        void shouldAllowOfferChangeForModifiablePayment(
                PaymentMethod paymentMethod,
                PaymentStatus paymentStatus
        ) {
            Payment payment = createPayment(
                    paymentMethod,
                    paymentStatus
            );

            assertThatCode(() -> orderModificationPolicy.validateOfferChange(payment)).doesNotThrowAnyException();
        }

        @Test
        void shouldRejectOfferChangeWhenPaymentIsNull() {
            assertThatThrownBy(() -> orderModificationPolicy.validateOfferChange(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage(
                            "Płatność zamówienia nie może być null"
                    );
        }
    }

    private Payment createPayment(
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus
    ) {
        return Payment.builder()
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .build();
    }
}
