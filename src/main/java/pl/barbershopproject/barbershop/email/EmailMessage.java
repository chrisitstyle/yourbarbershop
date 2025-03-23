package pl.barbershopproject.barbershop.email;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EmailMessage {

    private String to;
    private String subject;
    private String message;
}
