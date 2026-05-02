package pl.barbershopproject.barbershop.auth.captcha;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.barbershopproject.barbershop.exception.InvalidCaptchaException;

@Service
@Getter
@Setter
public class CaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    @Value("${google.recaptcha.verify-url}")
    private String recaptchaVerifyUrl;

    private final RestClient restClient;

    CaptchaService(RestClient restClient) {
        this.restClient = restClient;
    }

    public void verify(String captchaToken) {
        if (captchaToken == null || captchaToken.isBlank()) {
            throw new InvalidCaptchaException("Token CAPTCHA jest pusty.");
        }

        String url = recaptchaVerifyUrl + "?secret=" + recaptchaSecret + "&response=" + captchaToken;

        CaptchaResponse response = restClient.post()
                .uri(url)
                .retrieve()
                .body(CaptchaResponse.class);

        if (response == null || !response.isSuccess()) {
            throw new InvalidCaptchaException("Weryfikacja CAPTCHA nie powiodła się.");
        }
    }
}