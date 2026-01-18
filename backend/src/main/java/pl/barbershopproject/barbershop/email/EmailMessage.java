package pl.barbershopproject.barbershop.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessage {

    private String to;
    private String subject;
    private String message;
}
