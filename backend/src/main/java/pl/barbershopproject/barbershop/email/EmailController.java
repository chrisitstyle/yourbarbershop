package pl.barbershopproject.barbershop.email;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Tag(name = "Email", description = "Endpoints for sending email notifications and messages")
public class EmailController {

    private final EmailSenderService emailSenderService;

    @Operation(summary = "Send a custom email", description = "Sends a custom plain-text email message to a specified recipient. Requires authentication.")
    @ApiResponse(responseCode = "200", description = "Email request processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid email request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid authentication token")
    @PostMapping("/send-email")
    public ResponseEntity<Void> sendEmail(@RequestBody EmailMessage emailMessage) {
        this.emailSenderService.sendEmail(emailMessage.getTo(), emailMessage.getSubject(), emailMessage.getMessage());
        return ResponseEntity.ok().build();
    }
}