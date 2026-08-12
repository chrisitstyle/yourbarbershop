package pl.barbershopproject.barbershop.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailSenderService {

    private static final List<String> BLOCKED_DOMAINS = List.of(
            "github.placeholder.com",
            "facebook.placeholder.com"
    );

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String message) {
        if (shouldSkipEmail(to)) {
            return;
        }

        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setTo(to);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(message);

            mailSender.send(simpleMailMessage);
        } catch (Exception ex) {
            log.error("Failed to send email to '{}'. Error: {}", to, ex.getMessage());
        }
    }

    public void sendHtmlEmail(String to, String subject, String plainText, String html) {
        if (shouldSkipEmail(to)) {
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainText, html);

            mailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            log.error("Failed to build HTML email for '{}'. Error: {}", to, ex.getMessage());
        } catch (Exception ex) {
            log.error("Failed to send HTML email to '{}'. Error: {}", to, ex.getMessage());
        }
    }

    private boolean shouldSkipEmail(String email) {
        boolean blocked = isBlocked(email);

        if (blocked) {
            log.warn("Email sending to '{}' was cancelled. The recipient is blocked.", email);
        }

        return blocked;
    }

    private boolean isBlocked(String email) {
        if (email == null || email.isBlank()) {
            return true;
        }

        return BLOCKED_DOMAINS.stream()
                .anyMatch(domain -> email.toLowerCase().endsWith(domain.toLowerCase()));
    }
}