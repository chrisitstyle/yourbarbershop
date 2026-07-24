package pl.barbershopproject.barbershop.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stripe")
@Tag(name = "Stripe Webhook", description = "Endpoints for handling incoming Stripe payment webhooks")
public class StripeWebhookController {

    private final StripeWebhookSignatureVerifier signatureVerifier;
    private final StripeWebhookService stripeWebhookService;

    @Operation(
            summary = "Handle Stripe webhook events",
            description = "Receives and verifies raw webhook events sent by Stripe asynchronously for payment status updates. Publicly accessible."
    )
    @ApiResponse(responseCode = "200", description = "Webhook event processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid payload or missing/invalid Stripe signature")
    @SecurityRequirements()
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @Parameter(description = "Stripe cryptographic signature header for payload verification")
            @RequestHeader(name = "Stripe-Signature", required = false) String stripeSignature
    ) {
        signatureVerifier.verify(payload, stripeSignature);
        stripeWebhookService.handleEvent(payload);
        return ResponseEntity.ok("OK");
    }
}