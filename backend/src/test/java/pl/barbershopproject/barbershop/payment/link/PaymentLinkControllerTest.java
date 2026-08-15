package pl.barbershopproject.barbershop.payment.link;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.barbershopproject.barbershop.config.JwtAuthFilter;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.exception.InvalidPaymentLinkTokenException;
import pl.barbershopproject.barbershop.exception.PaymentLinkUnavailableException;
import pl.barbershopproject.barbershop.utils.TestClockConfig;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(
        controllers = PaymentLinkController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestClockConfig.class)
class PaymentLinkControllerTest {

    private static final String TOKEN = "payment-link-token";

    private static final String CHECKOUT_URL = "https://checkout.stripe.com/c/pay/cs_test_123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentLinkService paymentLinkService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void resolveCheckout_ReturnsCheckoutUrl() throws Exception {
        // given
        when(paymentLinkService.resolveCheckoutUrl(TOKEN))
                .thenReturn(CHECKOUT_URL);

        // when then
        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                "/payments/link/{token}/checkout",
                                TOKEN))
                .andExpect(
                        MockMvcResultMatchers.status().isOk())
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.checkoutUrl")
                                .value(CHECKOUT_URL));

        verify(paymentLinkService)
                .resolveCheckoutUrl(TOKEN);
    }

    @Test
    void resolveCheckout_ReturnsBadRequest_WhenTokenIsInvalid()
            throws Exception {
        // given
        when(paymentLinkService.resolveCheckoutUrl(TOKEN))
                .thenThrow(
                        new InvalidPaymentLinkTokenException(
                                "Nieprawidłowy link do płatności"));

        // when then
        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                "/payments/link/{token}/checkout",
                                TOKEN))
                .andExpect(
                        MockMvcResultMatchers.status().isBadRequest())
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.message")
                                .value("Nieprawidłowy link do płatności"))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.status")
                                .value("BAD_REQUEST"));
    }

    @Test
    void resolveCheckout_ReturnsConflict_WhenPaymentIsUnavailable()
            throws Exception {
        // given
        when(paymentLinkService.resolveCheckoutUrl(TOKEN))
                .thenThrow(
                        new PaymentLinkUnavailableException(
                                "Płatność została już opłacona"));

        // when then
        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                "/payments/link/{token}/checkout",
                                TOKEN))
                .andExpect(
                        MockMvcResultMatchers.status().isConflict())
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.message")
                                .value("Płatność została już opłacona"))
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.status")
                                .value("CONFLICT"));
    }
}
