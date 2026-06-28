package pl.barbershopproject.barbershop.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentAvailabilityService;
import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.guestorder.GuestOrderRepository;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.OrderRepository;
import pl.barbershopproject.barbershop.order.event.OrderCreatedEvent;
import pl.barbershopproject.barbershop.util.Status;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final GuestOrderRepository guestOrderRepository;
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
        String sessionId = session.path("id").asText();
        String paymentIntentId = session.path("payment_intent").asText(null);

        orderRepository.findByStripeCheckoutSessionId(sessionId).ifPresent(order -> {
            if (order.getPaymentStatus() == PaymentStatus.OPLACONA) {
                return;
            }

            order.setPaymentStatus(PaymentStatus.OPLACONA);
            order.setStripePaymentIntentId(paymentIntentId);
            orderRepository.save(order);

            publishOrderConfirmation(order);
        });

        guestOrderRepository.findByStripeCheckoutSessionId(sessionId).ifPresent(guestOrder -> {
            if (guestOrder.getPaymentStatus() == PaymentStatus.OPLACONA) {
                return;
            }

            guestOrder.setPaymentStatus(PaymentStatus.OPLACONA);
            guestOrder.setStripePaymentIntentId(paymentIntentId);
            guestOrderRepository.save(guestOrder);

            publishGuestOrderConfirmation(guestOrder);
        });
    }

    private void handleCheckoutExpired(JsonNode session) {
        String sessionId = session.path("id").asText();

        orderRepository.findByStripeCheckoutSessionId(sessionId).ifPresent(order -> {
            if (order.getPaymentStatus() == PaymentStatus.OPLACONA) {
                return;
            }

            order.setPaymentStatus(PaymentStatus.WYGASLA);
            appointmentAvailabilityService.releaseIfReserved(order.getVisitDate(), order.getStatus());
            order.setStatus(Status.ANULOWANE);
            orderRepository.save(order);
        });

        guestOrderRepository.findByStripeCheckoutSessionId(sessionId).ifPresent(guestOrder -> {
            if (guestOrder.getPaymentStatus() == PaymentStatus.OPLACONA) {
                return;
            }

            guestOrder.setPaymentStatus(PaymentStatus.WYGASLA);
            appointmentAvailabilityService.releaseIfReserved(guestOrder.getVisitDate(), guestOrder.getStatus());
            guestOrder.setStatus(Status.ANULOWANE);
            guestOrderRepository.save(guestOrder);
        });
    }

    private void handlePaymentIntentFailed(JsonNode paymentIntent) {
        Optional<PaymentTarget> target = resolveTarget(paymentIntent);

        target.ifPresent(paymentTarget ->
                updatePaymentStatus(paymentTarget, PaymentStatus.NIEUDANA)
        );
    }

    private void handleChargeRefunded(JsonNode charge) {
        String paymentIntentId = charge.path("payment_intent").asText(null);

        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            return;
        }

        orderRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(order -> {
            order.setPaymentStatus(PaymentStatus.ZWROCONA);
            orderRepository.save(order);
        });

        guestOrderRepository.findByStripePaymentIntentId(paymentIntentId).ifPresent(guestOrder -> {
            guestOrder.setPaymentStatus(PaymentStatus.ZWROCONA);
            guestOrderRepository.save(guestOrder);
        });
    }

    private Optional<PaymentTarget> resolveTarget(JsonNode object) {
        String targetType = object.path("metadata").path("targetType").asText(null);
        String targetId = object.path("metadata").path("targetId").asText(null);

        if (targetType == null || targetId == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(new PaymentTarget(
                    PaymentTargetType.valueOf(targetType),
                    Long.valueOf(targetId)
            ));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private void updatePaymentStatus(PaymentTarget target, PaymentStatus paymentStatus) {
        if (target.type() == PaymentTargetType.ORDER) {
            orderRepository.findById(target.id()).ifPresent(order -> {
                order.setPaymentStatus(paymentStatus);
                orderRepository.save(order);
            });
            return;
        }

        guestOrderRepository.findById(target.id()).ifPresent(guestOrder -> {
            guestOrder.setPaymentStatus(paymentStatus);
            guestOrderRepository.save(guestOrder);
        });
    }

    private void publishOrderConfirmation(Order order) {
        eventPublisher.publishEvent(new OrderCreatedEvent(
                order.getUser().getEmail(),
                order.getUser().getFirstname(),
                order.getVisitDate(),
                order.getOffer().getKind(),
                order.getOffer().getCost()
        ));
    }

    private void publishGuestOrderConfirmation(GuestOrder guestOrder) {
        eventPublisher.publishEvent(new OrderCreatedEvent(
                guestOrder.getEmail(),
                guestOrder.getFirstname(),
                guestOrder.getVisitDate(),
                guestOrder.getOffer().getKind(),
                guestOrder.getOffer().getCost()
        ));
    }

    private record PaymentTarget(PaymentTargetType type, Long id) {
    }
}