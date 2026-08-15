package pl.barbershopproject.barbershop.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.barbershopproject.barbershop.exception.InvalidPaymentLinkTokenException;
import pl.barbershopproject.barbershop.payment.link.PaymentLinkToken;
import pl.barbershopproject.barbershop.payment.link.PaymentLinkTokenService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentLinkTokenServiceTest {

    private static final Instant NOW = Instant.parse("2030-01-10T12:00:00Z");

    private static final Long PAYMENT_ID = 15L;

    private PaymentLinkTokenService paymentLinkTokenService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

        paymentLinkTokenService = new PaymentLinkTokenService(clock,"payment-link-test-secret");
    }

    @Test
    void shouldCreateAndVerifyPaymentLinkToken() {
        // given
        Instant expiresAt = NOW.plusSeconds(3600);

        String token = paymentLinkTokenService.createToken(
                PAYMENT_ID,
                expiresAt);

        // when
        PaymentLinkToken result = paymentLinkTokenService.verifyToken(token);

        // then
        assertThat(result.paymentId())
                .isEqualTo(PAYMENT_ID);

        assertThat(result.expiresAt())
                .isEqualTo(expiresAt);
    }

    @Test
    void shouldRejectModifiedPaymentLinkToken() {
        // given
        String token = paymentLinkTokenService.createToken(
                PAYMENT_ID,
                NOW.plusSeconds(3600));

        String[] parts = token.split("\\.");

        char replacement = parts[1].charAt(0) == 'A'
                ? 'B'
                : 'A';

        String modifiedSignature = replacement + parts[1].substring(1);

        String modifiedToken = parts[0] + "." + modifiedSignature;

        // when then
        assertThatThrownBy(() -> paymentLinkTokenService.verifyToken(modifiedToken))
                .isInstanceOf(
                        InvalidPaymentLinkTokenException.class)
                .hasMessage("Nieprawidłowy link do płatności");
    }

    @Test
    void shouldRejectExpiredPaymentLinkToken() {
        // given
        String token = paymentLinkTokenService.createToken(
                PAYMENT_ID,
                NOW.minusSeconds(1));

        // when then
        assertThatThrownBy(() -> paymentLinkTokenService.verifyToken(token))
                .isInstanceOf(
                        InvalidPaymentLinkTokenException.class)
                .hasMessage(
                        "Link do płatności wygasł");
    }

    @Test
    void shouldRejectMalformedPaymentLinkToken() {
        // when then
        assertThatThrownBy(() ->
                paymentLinkTokenService.verifyToken(
                        "not-a-valid-token"))
                .isInstanceOf(
                        InvalidPaymentLinkTokenException.class
                )
                .hasMessage(
                        "Nieprawidłowy link do płatności");
    }

    @Test
    void shouldRejectNonCanonicalSignature() {
        // given
        String token = paymentLinkTokenService.createToken(
                PAYMENT_ID,
                NOW.plusSeconds(3600)
        );

        String[] parts = token.split("\\.");

        String base64UrlAlphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";

        String signature = parts[1];

        char lastCharacter = signature.charAt(signature.length() - 1);

        int index = base64UrlAlphabet.indexOf(lastCharacter);

        char equivalentNonCanonicalCharacter = base64UrlAlphabet.charAt(index + 1);

        String modifiedSignature = signature.substring(
                        0,
                        signature.length() - 1) + equivalentNonCanonicalCharacter;

        String modifiedToken = parts[0] + "." + modifiedSignature;

        // when then
        assertThatThrownBy(() -> paymentLinkTokenService.verifyToken(modifiedToken)
        )
                .isInstanceOf(
                        InvalidPaymentLinkTokenException.class)
                .hasMessage(
                        "Nieprawidłowy link do płatności");
    }
}
