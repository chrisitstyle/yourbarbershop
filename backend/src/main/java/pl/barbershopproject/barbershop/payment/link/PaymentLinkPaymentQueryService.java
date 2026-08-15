package pl.barbershopproject.barbershop.payment.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;
import pl.barbershopproject.barbershop.payment.PaymentRepository;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Loads payment and reservation data required to resolve a payment link.
 *
 * <p>JPA entities and their lazy associations are accessed only inside
 * the read-only transaction. The returned data can safely be used later
 * while communicating with Stripe outside the database transaction.</p>
 */
@Service
@RequiredArgsConstructor
public class PaymentLinkPaymentQueryService {

    private static final String PAYMENT_NOT_FOUND_MESSAGE = "Nie znaleziono płatności o ID: ";

    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public PaymentLinkPaymentData getPayment(Long paymentId) {
        Objects.requireNonNull(
                paymentId,
                "Payment ID nie może być null");

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException(
                        PAYMENT_NOT_FOUND_MESSAGE + paymentId
                ));

        if (payment.isForOrder()) {
            return fromOrder(
                    payment,
                    payment.getOrder()
            );
        }

        if (payment.isForGuestOrder()) {
            return fromGuestOrder(
                    payment,
                    payment.getGuestOrder()
            );
        }

        throw new IllegalStateException(
                "Płatność nie jest przypisana do żadnego zamówienia"
        );
    }

    private PaymentLinkPaymentData fromOrder(
            Payment payment,
            Order order) {
        String productName = Objects.requireNonNull(
                order.getBookedOffer().getName(),
                "Nazwa zarezerwowanej oferty nie może być null");

        return new PaymentLinkPaymentData(
                PaymentCheckoutRequest.from(
                        payment,
                        productName
                ),
                payment.getStripeCheckoutSessionId(),
                order.getOrderStatus(),
                order.getVisitDate()
        );
    }

    private PaymentLinkPaymentData fromGuestOrder(
            Payment payment,
            GuestOrder guestOrder
    ) {
        String productName = Objects.requireNonNull(
                guestOrder.getBookedOffer().getName(),
                "Nazwa zarezerwowanej oferty nie może być null"
        );

        return new PaymentLinkPaymentData(
                PaymentCheckoutRequest.from(
                        payment,
                        productName),
                payment.getStripeCheckoutSessionId(),
                guestOrder.getOrderStatus(),
                guestOrder.getVisitDate()
        );
    }
}
