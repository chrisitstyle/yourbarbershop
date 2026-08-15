package pl.barbershopproject.barbershop.payment.creation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.payment.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Creates and persists payments inside an existing order transaction.
 *
 * <p>This service does not communicate with Stripe. It only prepares
 * immutable checkout data that can be used after the database transaction
 * has been committed.</p>
 */
@Service
@Transactional(propagation = Propagation.MANDATORY) // requires an already active order transaction when creating and persisting a payment
public class PaymentCreationService implements PaymentCreator {

    private final PaymentRepository paymentRepository;
    private final Clock clock;
    private final String currency;

    public PaymentCreationService(
            PaymentRepository paymentRepository,
            Clock clock,
            @Value("${stripe.currency:pln}") String currency
    ) {
        this.paymentRepository = Objects.requireNonNull(
                paymentRepository,
                "PaymentRepository nie może być null"
        );

        this.clock = Objects.requireNonNull(clock,"Clock nie może być null");

        this.currency = normalizeCurrency(currency);
    }

    @Override
    public PaymentCreationResult createForOrder(Order order, PaymentMethod paymentMethod) {
        Objects.requireNonNull(order, "Order nie może być null");

        BookedOffer bookedOffer = getRequiredBookedOffer(order.getBookedOffer());

        Payment payment = createPayment(bookedOffer, paymentMethod)
                .order(order)
                .build();

        order.setPayment(payment);

        return savePayment(payment, bookedOffer.getName());
    }

    @Override
    public PaymentCreationResult createForGuestOrder(
            GuestOrder guestOrder,
            PaymentMethod paymentMethod
    ) {
        Objects.requireNonNull(
                guestOrder,
                "GuestOrder nie może być null"
        );

        BookedOffer bookedOffer = getRequiredBookedOffer(
                guestOrder.getBookedOffer()
        );

        Payment payment = createPayment(bookedOffer,paymentMethod)
                .guestOrder(guestOrder)
                .build();

        guestOrder.setPayment(payment);

        return savePayment(payment, bookedOffer.getName());
    }

    private Payment.PaymentBuilder createPayment(
            BookedOffer bookedOffer,
            PaymentMethod paymentMethod
    ) {
        Objects.requireNonNull(
                paymentMethod,
                "PaymentMethod nie może być null"
        );

        return Payment.builder()
                .paymentMethod(paymentMethod)
                .paymentStatus(initialStatus(paymentMethod))
                .stripeCheckoutIdempotencyKey(
                        createStripeCheckoutIdempotencyKey(paymentMethod)
                )
                .amount(bookedOffer.getPrice())
                .currency(currency)
                .createdAt(LocalDateTime.now(clock));
    }

    private String createStripeCheckoutIdempotencyKey(PaymentMethod paymentMethod) {
        return requiresOnlineCheckout(paymentMethod) ? UUID.randomUUID().toString() : null;
    }

    private PaymentCreationResult savePayment(
            Payment payment,
            String productName
    ) {
        Payment savedPayment = paymentRepository.saveAndFlush(payment);

        PaymentCheckoutRequest checkoutRequest = PaymentCheckoutRequest.from(savedPayment, productName);

        return new PaymentCreationResult(savedPayment, checkoutRequest);
    }

    private BookedOffer getRequiredBookedOffer(BookedOffer bookedOffer) {
        BookedOffer requiredBookedOffer = Objects.requireNonNull(bookedOffer,"BookedOffer nie może być null");

        Objects.requireNonNull(requiredBookedOffer.getName(),"Nazwa zarezerwowanej oferty nie może być null");

        Objects.requireNonNull(
                requiredBookedOffer.getPrice(),
                "Cena zarezerwowanej oferty nie może być null"
        );

        return requiredBookedOffer;
    }

    private PaymentStatus initialStatus(
            PaymentMethod paymentMethod
    ) {
        return requiresOnlineCheckout(paymentMethod)
                ? PaymentStatus.OCZEKUJE_NA_PLATNOSC
                : PaymentStatus.NIE_WYMAGANA;
    }

    private boolean requiresOnlineCheckout(
            PaymentMethod paymentMethod
    ) {
        return paymentMethod == PaymentMethod.KARTA_ONLINE;
    }

    private String normalizeCurrency(String currency) {
        String normalizedCurrency = Objects.requireNonNull(currency, "Waluta nie może być null")
                .trim();

        if (normalizedCurrency.isEmpty()) {
            throw new IllegalArgumentException("Waluta nie może być pusta");
        }

        return normalizedCurrency.toUpperCase(Locale.ROOT);
    }
}