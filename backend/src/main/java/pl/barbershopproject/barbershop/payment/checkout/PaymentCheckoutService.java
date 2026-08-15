package pl.barbershopproject.barbershop.payment.checkout;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.payment.*;
import pl.barbershopproject.barbershop.payment.stripe.checkout.StripeCheckoutService;
import pl.barbershopproject.barbershop.payment.stripe.checkout.StripeCheckoutSessionResponse;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NEVER) //prevents Stripe API calls from running inside an active database transaction
public class PaymentCheckoutService implements PaymentCheckout {

    private final StripeCheckoutService stripeCheckoutService;
    private final PaymentCheckoutSessionUpdater checkoutSessionUpdater;

    @Override
    public String createCheckoutIfRequired(PaymentCheckoutRequest request) {
        Objects.requireNonNull(request, "PaymentCheckoutRequest nie może być null");

        if (!request.requiresOnlineCheckout()) {
            return null;
        }

        StripeCheckoutSessionResponse checkoutSession = Objects.requireNonNull(
                stripeCheckoutService.createCheckoutSession(request),
                "Stripe Checkout session nie może być null");

        String sessionId = Objects.requireNonNull(
                checkoutSession.sessionId(),
                "Stripe Checkout session ID nie może być null");

        String checkoutUrl = Objects.requireNonNull(checkoutSession.checkoutUrl(),
                "Stripe Checkout URL nie może być null");

        if (sessionId.isBlank()) {
            throw new IllegalStateException("Stripe Checkout session ID nie może być pusty");
        }

        if (checkoutUrl.isBlank()) {
            throw new IllegalStateException("Stripe Checkout URL nie może być pusty");
        }

        checkoutSessionUpdater.assignSession(
                request.paymentId(),
                sessionId
        );

        return checkoutUrl;
    }
}
