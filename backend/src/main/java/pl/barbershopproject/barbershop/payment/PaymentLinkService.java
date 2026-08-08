package pl.barbershopproject.barbershop.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.barbershopproject.barbershop.exception.PaymentLinkUnavailableException;
import pl.barbershopproject.barbershop.exception.InvalidPaymentLinkTokenException;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Resolves signed customer payment links to an active Stripe Checkout URL.
 *
 * <p>The service verifies the payment link, validates the current payment
 * and reservation state, and either reuses an existing open Checkout Session
 * or recreates checkout when no Stripe session was previously persisted.</p>
 */
@Service
@RequiredArgsConstructor
public class PaymentLinkService {

    private final PaymentLinkTokenService paymentLinkTokenService;
    private final PaymentLinkPaymentQueryService paymentQueryService;
    private final PaymentCheckout paymentCheckout;
    private final StripeCheckoutService stripeCheckoutService;
    private final Clock clock;

    /**
     * Resolves a payment link token to a Stripe Checkout URL.
     *
     * @param token signed payment link token
     * @return URL of the active Stripe Checkout Session
     * @throws InvalidPaymentLinkTokenException when the token is invalid or expired
     * @throws PaymentLinkUnavailableException when the reservation can no longer
     * be paid through the link
     */
    public String resolveCheckoutUrl(String token) {
        PaymentLinkToken paymentLinkToken = paymentLinkTokenService.verifyToken(token);

        PaymentLinkPaymentData payment = paymentQueryService.getPayment(
                        paymentLinkToken.paymentId());

        validatePayment(payment);

        String sessionId = payment.stripeCheckoutSessionId();

        if (sessionId == null) {
            return createCheckout(payment);
        }

        return resolveExistingCheckout(sessionId);
    }

    private void validatePayment(
            PaymentLinkPaymentData payment
    ) {
        PaymentCheckoutRequest checkoutRequest = payment.checkoutRequest();

        if (checkoutRequest.paymentMethod()
                != PaymentMethod.KARTA_ONLINE) {
            throw new PaymentLinkUnavailableException(
                    "Link jest dostępny tylko dla płatności online"
            );
        }

        if (payment.orderStatus() == OrderStatus.ANULOWANE) {
            throw new PaymentLinkUnavailableException(
                    "Nie można opłacić anulowanej wizyty"
            );
        }

        if (!payment.visitDate().isAfter(
                LocalDateTime.now(clock))) {
            throw new PaymentLinkUnavailableException(
                    "Nie można opłacić wizyty, której termin już minął");
        }

        switch (checkoutRequest.paymentStatus()) {
            case OCZEKUJE_NA_PLATNOSC -> {
                // Payment can still be completed
            }
            case OPLACONA -> throw new PaymentLinkUnavailableException(
                    "Płatność została już opłacona"
            );
            case WYGASLA -> throw new PaymentLinkUnavailableException(
                    "Sesja płatności wygasła"
            );
            case ZWROCONA -> throw new PaymentLinkUnavailableException(
                    "Płatność została zwrócona"
            );
            case NIE_WYMAGANA -> throw new PaymentLinkUnavailableException(
                    "Ta rezerwacja nie wymaga płatności online"
            );
            case NIEUDANA -> throw new PaymentLinkUnavailableException(
                    "Płatność nie może zostać ponowiona"
            );
        }
    }

    private String createCheckout(
            PaymentLinkPaymentData payment
    ) {
        String checkoutUrl = paymentCheckout
                .createCheckoutIfRequired(
                        payment.checkoutRequest()
                );

        if (checkoutUrl == null || checkoutUrl.isBlank()) {
            throw new IllegalStateException(
                    "Nie udało się utworzyć adresu Stripe Checkout");
        }

        return checkoutUrl;
    }

    private String resolveExistingCheckout(
            String sessionId) {
        StripeCheckoutSessionDetails session = stripeCheckoutService.retrieveCheckoutSession(
                        sessionId);

        return switch (session.status()) {
            case OPEN -> requireCheckoutUrl(session);

            case COMPLETE -> throw new PaymentLinkUnavailableException(
                            "Płatność została już zakończona");

            case EXPIRED -> throw new PaymentLinkUnavailableException(
                            "Sesja płatności wygasła");
        };
    }

    private String requireCheckoutUrl(
            StripeCheckoutSessionDetails session
    ) {
        String checkoutUrl = session.checkoutUrl();

        if (checkoutUrl == null || checkoutUrl.isBlank()) {
            throw new IllegalStateException(
                    "Otwarta sesja Stripe nie zawiera adresu Checkout"
            );
        }

        return checkoutUrl;
    }
}
