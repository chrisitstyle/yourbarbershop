package pl.barbershopproject.barbershop.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailSenderService {

    private final JavaMailSender mailSender;
    private static final List<String> BLOCKED_DOMAINS = List.of(
            "github.placeholder.com",
            "facebook.placeholder.com"
    );

    public void sendEmail(String to, String subject, String message) {

        if (isBlocked(to)) {
            log.warn("Anulowano wysłanie emaila do: '{}'. Domena znajduje się na czarnej liście.", to);
            return;
        }

        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(to);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(message);

            this.mailSender.send(simpleMailMessage);
            log.info("Wysłano email do: '{}'", to);
        } catch (Exception e) {
            log.error("Błąd podczas wysyłania emaila do: {}. Treść błędu: {}", to, e.getMessage());
        }


    }


    private boolean isBlocked(String email) {
        if (email == null || email.isBlank()) {
            return true;
        }

        return BLOCKED_DOMAINS.stream()
                .anyMatch(domain -> email.toLowerCase().endsWith(domain.toLowerCase()));
    }

}
