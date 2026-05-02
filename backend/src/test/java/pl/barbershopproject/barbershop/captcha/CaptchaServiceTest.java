package pl.barbershopproject.barbershop.captcha;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;
import pl.barbershopproject.barbershop.auth.captcha.CaptchaResponse;
import pl.barbershopproject.barbershop.auth.captcha.CaptchaService;
import pl.barbershopproject.barbershop.exception.InvalidCaptchaException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CaptchaService captchaService;

    private final String SECRET = "test-secret";
    private final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    @BeforeEach
    void setUp() {
        captchaService.setRecaptchaSecret(SECRET);
        captchaService.setRecaptchaVerifyUrl(VERIFY_URL);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    @DisplayName("Should throw InvalidCaptchaException when token is null, empty or blank")
    void verify_throwsExceptionWhenTokenIsInvalid(String invalidToken) {
        // then
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

        when(restTemplate.postForObject(eq(expectedUrl), any(), eq(CaptchaResponse.class)))
                .thenReturn(successResponse);

        // when, then
        assertDoesNotThrow(() -> captchaService.verify(token));
    }

    @Test
    @DisplayName("Should throw InvalidCaptchaException when reCAPTCHA returns success false")
    void verify_throwsExceptionWhenCaptchaFails() {
        // given
        String token = "invalid-token";
        CaptchaResponse failureResponse = new CaptchaResponse();
        failureResponse.setSuccess(false);

        when(restTemplate.postForObject(any(String.class), any(), eq(CaptchaResponse.class)))
                .thenReturn(failureResponse);

        // when, then
        assertThrows(InvalidCaptchaException.class, () -> captchaService.verify(token));
    }

    @Test
    @DisplayName("Should throw InvalidCaptchaException when response from Google is null")
    void verify_throwsExceptionWhenResponseIsNull() {
        // given
        String token = "any-token";
        when(restTemplate.postForObject(any(String.class), any(), eq(CaptchaResponse.class)))
                .thenReturn(null);

        // when, then
        assertThrows(InvalidCaptchaException.class, () -> captchaService.verify(token));
    }
}
