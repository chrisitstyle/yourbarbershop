package pl.barbershopproject.barbershop.auth.captcha;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class CaptchaClientConfig {

    @Bean
    RestClient captchaRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("https://www.google.com/recaptcha/api")
                .build();
    }
}
