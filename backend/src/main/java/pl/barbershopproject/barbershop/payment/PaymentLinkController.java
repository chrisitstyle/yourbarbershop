package pl.barbershopproject.barbershop.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.barbershopproject.barbershop.payment.dto.PaymentLinkCheckoutResponseDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments/link")
@Tag(
        name = "Payment Links",
        description = "Public endpoints for continuing unpaid online payments")
class PaymentLinkController {

    private final PaymentLinkService paymentLinkService;

    @Operation(
            summary = "Continue an online payment",
            description = "Verifies a signed payment link and returns an active Stripe Checkout URL.")
    @ApiResponse(
            responseCode = "200",
            description = "Payment link resolved successfully")
    @ApiResponse(
            responseCode = "400",
            description = "Invalid or expired payment link")
    @ApiResponse(
            responseCode = "404",
            description = "Payment no longer exists")
    @ApiResponse(
            responseCode = "409",
            description = "Payment can no longer be completed")
    @SecurityRequirements()
    @PostMapping("/{token}/checkout")
    public ResponseEntity<PaymentLinkCheckoutResponseDTO> resolveCheckout(
            @Parameter(description = "Signed payment link token")
            @PathVariable
            String token) {
        String checkoutUrl = paymentLinkService.resolveCheckoutUrl(token);

        return ResponseEntity.ok(new PaymentLinkCheckoutResponseDTO(checkoutUrl));
    }
}
