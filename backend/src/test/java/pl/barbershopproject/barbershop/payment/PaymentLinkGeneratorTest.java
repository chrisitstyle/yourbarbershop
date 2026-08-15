package pl.barbershopproject.barbershop.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.payment.link.PaymentLinkGenerator;
import pl.barbershopproject.barbershop.payment.link.PaymentLinkTokenService;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentLinkGeneratorTest {

    private static final Long PAYMENT_ID = 15L;
    private static final String TOKEN = "signed-payment-token";

    private static final Instant NOW = Instant.parse("2030-01-10T12:00:00Z");

    private static final Instant EXPIRES_AT = Instant.parse("2030-01-11T12:00:00Z");

    @Mock
    private PaymentLinkTokenService paymentLinkTokenService;

    private PaymentLinkGenerator paymentLinkGenerator;

    @BeforeEach
    void setUp() {
        paymentLinkGenerator = new PaymentLinkGenerator(
                paymentLinkTokenService,
                Clock.fixed(NOW, ZoneOffset.UTC),
                "http://localhost:3000/payment",
                24);
    }

    @Test
    void shouldCreatePaymentLink() {
        // given
        when(paymentLinkTokenService.createToken(
                PAYMENT_ID,
                EXPIRES_AT
        )).thenReturn(TOKEN);

        // when
        String result =
                paymentLinkGenerator.createLink(PAYMENT_ID);

        // then
        assertThat(result)
                .isEqualTo("http://localhost:3000/payment/"
                                + TOKEN);

        verify(paymentLinkTokenService)
                .createToken(PAYMENT_ID, EXPIRES_AT);
    }
}
