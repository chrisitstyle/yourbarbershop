package pl.barbershopproject.barbershop.payment.stripe.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;

@Component
public class StripeWebhookSignatureVerifier {

    private static final long DEFAULT_TOLERANCE_SECONDS = 300;

    private final String webhookSecret;

    public StripeWebhookSignatureVerifier(@Value("${stripe.webhook-secret:}") String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public void verify(String payload, String stripeSignatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("Brakuje konfiguracji stripe.webhook-secret");
        }

        if (stripeSignatureHeader == null || stripeSignatureHeader.isBlank()) {
            throw new IllegalArgumentException("Brakuje nagłówka Stripe-Signature");
        }

        String timestamp = extractPart(stripeSignatureHeader, "t");
        String signature = extractPart(stripeSignatureHeader, "v1");

        long eventTimestamp = Long.parseLong(timestamp);
        long age = Math.abs(Instant.now().getEpochSecond() - eventTimestamp);

        if (age > DEFAULT_TOLERANCE_SECONDS) {
            throw new IllegalArgumentException("Webhook Stripe jest zbyt stary");
        }

        String signedPayload = timestamp + "." + payload;
        String expectedSignature = hmacSha256(signedPayload, webhookSecret);

        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
        )) {
            throw new IllegalArgumentException("Niepoprawny podpis webhooka Stripe");
        }
    }

    private String extractPart(String header, String key) {
        return Arrays.stream(header.split(","))
                .map(String::trim)
                .filter(part -> part.startsWith(key + "="))
                .map(part -> part.substring((key + "=").length()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Niepoprawny nagłówek Stripe-Signature"));
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("Nie udało się zweryfikować podpisu Stripe", exception);
        }
    }
}
