package pl.barbershopproject.barbershop.email;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Payload for sending custom email notifications")
public class EmailMessage {

    @Schema(description = "Recipient email address", example = "client@example.com")
    private String to;

    @Schema(description = "Email subject line", example = "Appointment Reminder")
    private String subject;

    @Schema(description = "Body of the email message", example = "Your appointment is scheduled for tomorrow at 10:00 AM.")
    private String message;
}
