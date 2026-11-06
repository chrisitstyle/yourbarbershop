package pl.barbershopproject.barbershop.integration.payment.refund;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.OrderRepository;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.payment.refund.PaymentRefund;
import pl.barbershopproject.barbershop.payment.refund.PaymentRefundRepository;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;
import pl.barbershopproject.barbershop.user.UserRepository;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRefundIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private PaymentRefundRepository paymentRefundRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistRefundWithPaymentReference() {
        Offer selectedOffer = offerRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow();

        Order order = Order.builder()
                .user(userRepository.findByEmail("johndoe@example.com").orElseThrow())
                .offer(selectedOffer)
                .bookedOffer(BookedOffer.from(selectedOffer))
                .orderDate(LocalDateTime.now())
                .visitDate(LocalDateTime.now().plusDays(30))
                .orderStatus(OrderStatus.NOWE)
                .build();

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OPLACONA)
                .stripePaymentIntentId("pi_test_" + UUID.randomUUID())
                .amount(selectedOffer.getCost())
                .currency("PLN")
                .createdAt(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();

        payment.setOrder(order);
        order.setPayment(payment);

        Order savedOrder = orderRepository.saveAndFlush(order);

        String idempotencyKey = UUID.randomUUID().toString();

        PaymentRefund refund = PaymentRefund.builder()
                .payment(savedOrder.getPayment())
                .idempotencyKey(idempotencyKey)
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .refundStatus(RefundStatus.REQUESTED)
                .requestedAt(Instant.now())
                .build();

        PaymentRefund savedRefund = paymentRefundRepository.saveAndFlush(refund);

        PaymentRefund persistedRefund = paymentRefundRepository
                .findByIdempotencyKey(idempotencyKey)
                .orElseThrow();

        assertNotNull(savedRefund.getIdPaymentRefund());
        assertNotNull(persistedRefund.getVersion());

        assertEquals(savedOrder.getPayment().getIdPayment(),
                persistedRefund.getPayment().getIdPayment());

        assertEquals(RefundStatus.REQUESTED, persistedRefund.getRefundStatus());

        assertEquals(0, payment.getAmount().compareTo(persistedRefund.getAmount()));

        assertEquals(idempotencyKey, persistedRefund.getIdempotencyKey());

        assertNull(persistedRefund.getStripeRefundId());
        assertNull(persistedRefund.getCompletedAt());
    }
}
