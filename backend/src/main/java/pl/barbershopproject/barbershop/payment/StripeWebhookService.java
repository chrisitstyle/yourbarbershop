package pl.barbershopproject.barbershop.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentAvailabilityService;
import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.event.OrderCreatedEvent;
import pl.barbershopproject.barbershop.util.Status;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    private final AppointmentAvailabilityService appointmentAvailabilityService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void handleEvent(String payload) {
        try {
            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.path("type").asText();
            JsonNode object = event.path("data").path("object");

            switch (eventType) {
                case "checkout.session.completed" -> handleCheckoutCompleted(object);
                case "checkout.session.expired" -> handleCheckoutExpired(object);
                case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(object);
                case "payment_intent.payment_failed" -> handlePaymentIntentFailed(object);
                case "charge.refunded" -> handleChargeRefunded(object);
                default -> {
                    // Stripe wysyła wiele eventów. Nieobsługiwane eventy ignorujemy.
                }
            }
        } catch (Exception exception) {
            throw new IllegalArgumentException("Nie udało się obsłużyć webhooka Stripe", exception);
        }
    }

    private void handleCheckoutCompleted(JsonNode session) {
        Optional<Payment> optionalPayment = resolvePaymentFromCheckoutSession(session);

        optionalPayment.ifPresent(payment -> {
            if (payment.getPaymentStatus() == PaymentStatus.OPLACONA) {
                return;
            }

            String paymentIntentId = session.path("payment_intent").asText(null);

            payment.setPaymentStatus(PaymentStatus.OPLACONA);
            payment.setStripePaymentIntentId(paymentIntentId);
            payment.setPaidAt(LocalDateTime.now());

            paymentRepository.save(payment);

            publishConfirmation(payment);
        });
    }

    private void handleCheckoutExpired(JsonNode session) {
        Optional<Payment> optionalPayment = resolvePaymentFromCheckoutSession(session);

        optionalPayment.ifPresent(payment -> {
            if (payment.getPaymentStatus() == PaymentStatus.OPLACONA) {
                return;
            }

            payment.setPaymentStatus(PaymentStatus.WYGASLA);
            paymentRepository.save(payment);

            cancelReservation(payment);
        });
    }

    private void handlePaymentIntentSucceeded(JsonNode paymentIntent) {
        Optional<Payment> optionalPayment = resolvePaymentFromPaymentIntent(paymentIntent);

        optionalPayment.ifPresent(payment -> {
            if (payment.getPaymentStatus() == PaymentStatus.OPLACONA) {
                return;
            }

            String paymentIntentId = paymentIntent.path("id").asText(null);

            payment.setPaymentStatus(PaymentStatus.OPLACONA);
            payment.setStripePaymentIntentId(paymentIntentId);
            payment.setPaidAt(LocalDateTime.now());

            paymentRepository.save(payment);

            publishConfirmation(payment);
        });
    }

    private void handlePaymentIntentFailed(JsonNode paymentIntent) {
        Optional<Payment> optionalPayment = resolvePaymentFromPaymentIntent(paymentIntent);

        optionalPayment.ifPresent(payment -> {
            if (payment.getPaymentStatus() == PaymentStatus.OPLACONA) {
                return;
            }

            String paymentIntentId = paymentIntent.path("id").asText(null);

            payment.setPaymentStatus(PaymentStatus.NIEUDANA);
            payment.setStripePaymentIntentId(paymentIntentId);

            paymentRepository.save(payment);
        });
    }

    private void handleChargeRefunded(JsonNode charge) {
        String paymentIntentId = charge.path("payment_intent").asText(null);

        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            return;
        }

        paymentRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(payment -> {
            payment.setPaymentStatus(PaymentStatus.ZWROCONA);
            paymentRepository.save(payment);
        });
    }

    private Optional<Payment> resolvePaymentFromCheckoutSession(JsonNode session) {
        String sessionId = session.path("id").asText(null);

        if (sessionId != null && !sessionId.isBlank()) {
            Optional<Payment> paymentBySessionId = paymentRepository.findByStripeCheckoutSessionId(sessionId);

            if (paymentBySessionId.isPresent()) {
                return paymentBySessionId;
            }
        }

        return resolvePaymentFromMetadata(session);
    }

    private Optional<Payment> resolvePaymentFromPaymentIntent(JsonNode paymentIntent) {
        Optional<Payment> paymentByMetadata = resolvePaymentFromMetadata(paymentIntent);

        if (paymentByMetadata.isPresent()) {
            return paymentByMetadata;
        }

        String paymentIntentId = paymentIntent.path("id").asText(null);

        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            return Optional.empty();
        }

        return paymentRepository.findByStripePaymentIntentId(paymentIntentId);
    }

    private Optional<Payment> resolvePaymentFromMetadata(JsonNode object) {
        String paymentId = object.path("metadata").path("paymentId").asText(null);

        if (paymentId == null || paymentId.isBlank()) {
            return Optional.empty();
        }

        try {
            return paymentRepository.findById(Long.valueOf(paymentId));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private void publishConfirmation(Payment payment) {
        if (payment.getOrder() != null) {
            Order order = payment.getOrder();

            eventPublisher.publishEvent(new OrderCreatedEvent(
                    order.getUser().getEmail(),
                    order.getUser().getFirstname(),
                    order.getVisitDate(),
                    order.getOffer().getKind(),
                    order.getOffer().getCost()
            ));

            return;
        }

        GuestOrder guestOrder = payment.getGuestOrder();

        eventPublisher.publishEvent(new OrderCreatedEvent(
                guestOrder.getEmail(),
                guestOrder.getFirstname(),
                guestOrder.getVisitDate(),
                guestOrder.getOffer().getKind(),
                guestOrder.getOffer().getCost()
        ));
    }

    private void cancelReservation(Payment payment) {
        if (payment.getOrder() != null) {
            Order order = payment.getOrder();

            appointmentAvailabilityService.releaseIfReserved(order.getVisitDate(), order.getStatus());
            order.setStatus(Status.ANULOWANE);

            return;
        }

        GuestOrder guestOrder = payment.getGuestOrder();

        appointmentAvailabilityService.releaseIfReserved(guestOrder.getVisitDate(), guestOrder.getStatus());
        guestOrder.setStatus(Status.ANULOWANE);
    }
}