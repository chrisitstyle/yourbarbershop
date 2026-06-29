package pl.barbershopproject.barbershop.captcha;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import pl.barbershopproject.barbershop.auth.captcha.CaptchaClientConfig;
import pl.barbershopproject.barbershop.auth.captcha.CaptchaResponse;
import pl.barbershopproject.barbershop.auth.captcha.CaptchaService;
import pl.barbershopproject.barbershop.exception.InvalidCaptchaException;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(CaptchaService.class)
@Import(CaptchaClientConfig.class)
class CaptchaServiceTest {

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String SECRET = "test-secret";
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    @BeforeEach
    void setUp() {
        captchaService.setRecaptchaSecret(SECRET);
        captchaService.setRecaptchaVerifyUrl(VERIFY_URL);
        // reset mocked server before each test
        mockServer.reset();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("Should throw InvalidCaptchaException when token is null, empty or blank")
    void verify_throwsExceptionWhenTokenIsInvalid(String invalidToken) {
        assertThrows(InvalidCaptchaException.class, () -> captchaService.verify(invalidToken));
    }

    @Test
    @DisplayName("Should pass verification when reCAPTCHA returns success true")
    void verify_passesWhenCaptchaIsSuccessful() {
        // given
        String token = "valid-token";
        String expectedUrl = VERIFY_URL + "?secret=" + SECRET + "&response=" + token;

        CaptchaResponse successResponse = new CaptchaResponse();
        successResponse.setSuccess(true);

        // Google response 200
        mockServer.expect(requestTo(expectedUrl))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(successResponse),
                        MediaType.APPLICATION_JSON
                ));

        // when, then
        assertDoesNotThrow(() -> captchaService.verify(token));

        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw InvalidCaptchaException when reCAPTCHA returns success false")
    void verify_throwsExceptionWhenCaptchaFails() {
        // given
        String token = "invalid-token";
        String expectedUrl = VERIFY_URL + "?secret=" + SECRET + "&response=" + token;

        CaptchaResponse failureResponse = new CaptchaResponse();
        failureResponse.setSuccess(false);

        // Google return false
        mockServer.expect(requestTo(expectedUrl))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(failureResponse),
                        MediaType.APPLICATION_JSON
                ));

        // when, then
        assertThrows(InvalidCaptchaException.class, () -> captchaService.verify(token));
        mockServer.verify();
    }
}
