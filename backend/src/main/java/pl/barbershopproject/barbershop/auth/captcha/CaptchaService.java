package pl.barbershopproject.barbershop.auth.captcha;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pl.barbershopproject.barbershop.exception.InvalidCaptchaException;

@Service
@RequiredArgsConstructor
@Getter
@Setter
public class CaptchaService {

    @Value("${google.recaptcha.secret}")
    private String recaptchaSecret;

    @Value("${google.recaptcha.verify-url}")
    private String recaptchaVerifyUrl;

    private final RestTemplate restTemplate;


    public void verify(String captchaToken) {
        if (captchaToken == null || captchaToken.isBlank()) {
            throw new InvalidCaptchaException("Token CAPTCHA jest pusty.");
        }

        String url = recaptchaVerifyUrl + "?secret=" + recaptchaSecret + "&response=" + captchaToken;
        CaptchaResponse response = restTemplate.postForObject(url, null, CaptchaResponse.class);

        if (response == null || !response.isSuccess()) {
            throw new InvalidCaptchaException("Weryfikacja CAPTCHA nie powiodła się.");
        }
    }
}