package pl.barbershopproject.barbershop.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByStripeCheckoutSessionId(String stripeCheckoutSessionId);

    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}