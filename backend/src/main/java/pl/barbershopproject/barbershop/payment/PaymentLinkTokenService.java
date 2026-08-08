package pl.barbershopproject.barbershop.payment;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.barbershopproject.barbershop.exception.InvalidPaymentLinkTokenException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

/**
 * Creates and verifies signed tokens used in payment links.
 *
 * <p>The token contains the payment identifier and expiration time.
 * Its integrity is protected with an HMAC-SHA256 signature so that
 * clients cannot modify the payment ID or token expiration without
 * invalidating the signature.</p>
 *
 * <p>Tokens are URL-safe and can be embedded directly in payment links
 * sent to customers.</p>
 */
@Service
public class PaymentLinkTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String PAYLOAD_SEPARATOR = ":";

    private final Clock clock;
    private final byte[] secret;

    public PaymentLinkTokenService(
            Clock clock,
            @Value("${payment.link-secret}") String secret
    ) {
        this.clock = Objects.requireNonNull(
                clock,
                "Clock nie może być null");

        String requiredSecret = Objects.requireNonNull(
                secret,
                "Payment link secret nie może być null");

        if (requiredSecret.isBlank()) {
            throw new IllegalArgumentException(
                    "Payment link secret nie może być pusty");
        }

        this.secret = requiredSecret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Creates a signed payment link token for the given payment.
     *
     * @param paymentId identifier of the payment
     * @param expiresAt expiration time of the generated token
     * @return URL-safe signed payment link token
     */
    public String createToken(
            Long paymentId,
            Instant expiresAt) {
        Objects.requireNonNull(
                paymentId,
                "Payment ID nie może być null");

        Objects.requireNonNull(
                expiresAt,
                "Data wygaśnięcia linku nie może być null");

        String payload = paymentId
                + PAYLOAD_SEPARATOR
                + expiresAt.getEpochSecond();

        String encodedPayload = encode(
                payload.getBytes(StandardCharsets.UTF_8));

        String signature = encode(sign(encodedPayload));

        return encodedPayload + "." + signature;
    }

    /**
     * Verifies the signature and expiration time of a payment link token.
     *
     * @param token signed payment link token
     * @return verified token data containing the payment identifier
     * and expiration time
     * @throws InvalidPaymentLinkTokenException when the token is malformed,
     *                                          tampered with, or expired
     */
    public PaymentLinkToken verifyToken(String token) {
        String requiredToken = Objects.requireNonNull(
                token,
                "Token płatności nie może być null"
        );

        if (requiredToken.isBlank()) {
            throw invalidToken();
        }

        String[] parts = requiredToken.split("\\.", -1);

        if (parts.length != 2
                || parts[0].isBlank()
                || parts[1].isBlank()) {
            throw invalidToken();
        }

        verifySignature(parts[0], parts[1]);

        PaymentLinkToken paymentLinkToken = decodePayload(parts[0]);

        if (!Instant.now(clock).isBefore(
                paymentLinkToken.expiresAt()
        )) {
            throw new InvalidPaymentLinkTokenException(
                    "Link do płatności wygasł");
        }

        return paymentLinkToken;
    }

    private void verifySignature(String encodedPayload,
            String encodedSignature) {
        String expectedSignature = encode(sign(encodedPayload));

        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.US_ASCII),
                encodedSignature.getBytes(StandardCharsets.US_ASCII)
        )) {
            throw invalidToken();
        }
    }

    private PaymentLinkToken decodePayload(String encodedPayload) {
        try {
            String payload = new String(
                    Base64.getUrlDecoder().decode(encodedPayload),
                    StandardCharsets.UTF_8
            );

            String[] values = payload.split(
                    PAYLOAD_SEPARATOR,
                    -1
            );

            if (values.length != 2) {
                throw invalidToken();
            }

            Long paymentId = Long.valueOf(values[0]);
            Instant expiresAt = Instant.ofEpochSecond(
                    Long.parseLong(values[1])
            );

            if (paymentId <= 0) {
                throw invalidToken();
            }

            return new PaymentLinkToken(
                    paymentId,
                    expiresAt
            );
        } catch (IllegalArgumentException _) {
            throw invalidToken();
        }
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(
                    secret,
                    HMAC_ALGORITHM));

            return mac.doFinal(
                    value.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Nie udało się podpisać linku płatności",
                    exception);
        }
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value);
    }

    private InvalidPaymentLinkTokenException invalidToken() {
        return new InvalidPaymentLinkTokenException(
                "Nieprawidłowy link do płatności");
    }
}