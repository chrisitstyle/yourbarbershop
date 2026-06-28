package pl.barbershopproject.barbershop.payment;

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
public class StripeWebhookController {

    private final StripeWebhookSignatureVerifier signatureVerifier;
    private final StripeWebhookService stripeWebhookService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(name = "Stripe-Signature", required = false) String stripeSignature
    ) {
        signatureVerifier.verify(payload, stripeSignature);
        stripeWebhookService.handleEvent(payload);
        return ResponseEntity.ok("OK");
    }
}