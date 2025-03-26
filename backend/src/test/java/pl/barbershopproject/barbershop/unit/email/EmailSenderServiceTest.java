package pl.barbershopproject.barbershop.unit.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import pl.barbershopproject.barbershop.email.EmailSenderService;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSenderServiceTest {


    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailSenderService emailSenderService;

    @BeforeEach
   void setUp() {
        emailSenderService = new EmailSenderService(mailSender);
    }

    @Test
    void EmailSenderService_SendEmail_ShouldSendEmail_WhenValidInput() {
        String to = "test@example.com";
        String subject = "Test Subject";
        String message = "Test Message";

        emailSenderService.sendEmail(to, subject, message);

        SimpleMailMessage expectedMessage = new SimpleMailMessage();
        expectedMessage.setTo(to);
        expectedMessage.setSubject(subject);
        expectedMessage.setText(message);

        verify(mailSender, times(1)).send(expectedMessage);
    }



}
