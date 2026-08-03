package pl.barbershopproject.barbershop.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
class PaymentCheckoutSessionUpdateService implements PaymentCheckoutSessionUpdater {

    private static final String PAYMENT_NOT_FOUND_MESSAGE = "Nie znaleziono płatności o ID: ";

    private final PaymentRepository paymentRepository;

    @Override
    // runs the session ID update in a separate transaction after the order transaction has committed
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void assignSession(Long paymentId, String sessionId) {
        Objects.requireNonNull(paymentId,"Payment ID nie może być null");

        String requiredSessionId = Objects.requireNonNull(sessionId,
                "Stripe Checkout session ID nie może być null");

        if (requiredSessionId.isBlank()) {
            throw new IllegalArgumentException("Stripe Checkout session ID nie może być pusty");
        }

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NoSuchElementException(
                        PAYMENT_NOT_FOUND_MESSAGE + paymentId
                ));

        if (payment.getPaymentMethod() != PaymentMethod.KARTA_ONLINE) {
            throw new IllegalStateException("Sesję Stripe można przypisać tylko do płatności online");
        }

        String currentSessionId = payment.getStripeCheckoutSessionId();

        if (currentSessionId == null) {
            payment.setStripeCheckoutSessionId(requiredSessionId);
            paymentRepository.save(payment);
            return;
        }

        if (currentSessionId.equals(requiredSessionId)) {
            return;
        }

        throw new IllegalStateException("Płatność o ID: " + paymentId + " ma już przypisaną inną sesję Stripe"
        );
    }
}
