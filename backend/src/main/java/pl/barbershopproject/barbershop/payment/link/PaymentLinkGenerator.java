package pl.barbershopproject.barbershop.payment.link;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Generates complete customer-facing payment links.
 *
 * <p>The generated URL points to the frontend payment page and contains
 * a signed token identifying the payment. The token is valid only for
 * the configured amount of time.</p>
 */
@Service
public class PaymentLinkGenerator {

    private final PaymentLinkTokenService paymentLinkTokenService;
    private final Clock clock;
    private final String paymentLinkUrl;
    private final long validityHours;

    public PaymentLinkGenerator(
            PaymentLinkTokenService paymentLinkTokenService,
            Clock clock,
            @Value("${payment.link-url}") String paymentLinkUrl,
            @Value("${payment.link-validity-hours:24}") long validityHours
    ) {
        this.paymentLinkTokenService = Objects.requireNonNull(
                paymentLinkTokenService,
                "PaymentLinkTokenService nie może być null");

        this.clock = Objects.requireNonNull(
                clock,
                "Clock nie może być null"
        );

        String requiredPaymentLinkUrl = Objects.requireNonNull(
                paymentLinkUrl,
                "Payment link URL nie może być null");

        if (requiredPaymentLinkUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment link URL nie może być pusty"
            );
        }

        if (validityHours <= 0) {
            throw new IllegalArgumentException(
                    "Czas ważności linku płatniczego musi być większy od zera"
            );
        }

        this.paymentLinkUrl = requiredPaymentLinkUrl;
        this.validityHours = validityHours;
    }

    /**
     * Creates a complete frontend payment link for the given payment.
     *
     * @param paymentId identifier of the payment
     * @return customer-facing payment URL containing a signed token
     */
    public String createLink(Long paymentId) {
        Objects.requireNonNull(
                paymentId,
                "Payment ID nie może być null"
        );

        Instant expiresAt = Instant.now(clock)
                .plus(validityHours, ChronoUnit.HOURS);

        String token = paymentLinkTokenService.createToken(
                paymentId,
                expiresAt
        );

        return UriComponentsBuilder
                .fromUriString(paymentLinkUrl)
                .pathSegment(token)
                .build()
                .toUriString();
    }
}
