package pl.barbershopproject.barbershop.auth.captcha;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class CaptchaResponse {
    private boolean success;
    @JsonProperty("error-codes")
    private List<String> errorCodes;

}