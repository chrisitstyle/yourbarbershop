package pl.barbershopproject.barbershop.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.Order;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

/**
 * Creates and persists payments for registered-user and guest orders.
 *
 * <p>For online card payments, the service additionally creates a Stripe
 * Checkout session and assigns its identifier to the payment entity.</p>
 */
@Service
@Transactional
class PaymentCreationService implements PaymentCreator {

    private final PaymentRepository paymentRepository;
    private final StripeCheckoutService stripeCheckoutService;
    private final Clock clock;
    private final String currency;

    /**
     * Creates a payment creation service.
     *
     * @param paymentRepository repository used to persist payments
     * @param stripeCheckoutService service used to create Stripe Checkout sessions
     * @param clock clock used to determine the payment creation time
     * @param currency configured payment currency
     */
    PaymentCreationService(
            PaymentRepository paymentRepository,
            StripeCheckoutService stripeCheckoutService,
            Clock clock,
            @Value("${stripe.currency:pln}") String currency
    ) {
        this.paymentRepository = Objects.requireNonNull(
                paymentRepository,
                "PaymentRepository nie może być null"
        );
        this.stripeCheckoutService = Objects.requireNonNull(
                stripeCheckoutService,
                "StripeCheckoutService nie może być null"
        );
        this.clock = Objects.requireNonNull(
                clock,
                "Clock nie może być null"
        );
        this.currency = normalizeCurrency(currency);
    }

    /**
     * Creates a payment associated with a registered-user order.
     *
     * <p>The method maintains both sides of the relationship between
     * the order and the payment.</p>
     *
     * @param order order for which the payment is created
     * @param offer offer used to determine the payment amount
     * @param paymentMethod selected payment method
     * @return created payment together with an optional checkout URL
     * @throws NullPointerException if any argument is {@code null}
     */
    @Override
    public PaymentCreationResult createForOrder(
            Order order,
            Offer offer,
            PaymentMethod paymentMethod
    ) {
        Objects.requireNonNull(order, "Order nie może być null");

        Payment payment = createPayment(offer, paymentMethod)
                .order(order)
                .build();

        order.setPayment(payment);

        return saveAndCreateCheckoutIfRequired(payment, offer);
    }

    /**
     * Creates a payment associated with a guest order.
     *
     * <p>The method maintains both sides of the relationship between
     * the guest order and the payment.</p>
     *
     * @param guestOrder guest order for which the payment is created
     * @param offer offer used to determine the payment amount
     * @param paymentMethod selected payment method
     * @return created payment together with an optional checkout URL
     * @throws NullPointerException if any argument is {@code null}
     */
    @Override
    public PaymentCreationResult createForGuestOrder(
            GuestOrder guestOrder,
            Offer offer,
            PaymentMethod paymentMethod
    ) {
        Objects.requireNonNull(
                guestOrder,
                "GuestOrder nie może być null"
        );

        Payment payment = createPayment(offer, paymentMethod)
                .guestOrder(guestOrder)
                .build();

        guestOrder.setPayment(payment);

        return saveAndCreateCheckoutIfRequired(payment, offer);
    }

    /**
     * Creates a payment builder initialized with common payment data.
     *
     * @param offer offer used to determine the payment amount
     * @param paymentMethod selected payment method
     * @return initialized payment builder
     * @throws NullPointerException if the offer, its cost or payment method
     *                              is {@code null}
     */
    private Payment.PaymentBuilder createPayment(
            Offer offer,
            PaymentMethod paymentMethod
    ) {
        Objects.requireNonNull(offer, "Offer nie może być null");
        Objects.requireNonNull(
                offer.getCost(),
                "Koszt oferty nie może być null"
        );
        Objects.requireNonNull(
                paymentMethod,
                "PaymentMethod nie może być null"
        );

        return Payment.builder()
                .paymentMethod(paymentMethod)
                .paymentStatus(initialStatus(paymentMethod))
                .amount(offer.getCost())
                .currency(currency)
                .createdAt(LocalDateTime.now(clock));
    }

    /**
     * Persists the payment and creates a Stripe Checkout session when required.
     *
     * <p>For payment methods that do not require online checkout, the returned
     * checkout URL is {@code null}. The Stripe session identifier is persisted
     * through JPA dirty checking when the transaction is committed.</p>
     *
     * @param payment payment to persist
     * @param offer offer used to create the checkout session
     * @return payment creation result
     */
    private PaymentCreationResult saveAndCreateCheckoutIfRequired(
            Payment payment,
            Offer offer
    ) {
        Payment savedPayment = paymentRepository.save(payment);

        if (!requiresOnlineCheckout(savedPayment.getPaymentMethod())) {
            return new PaymentCreationResult(savedPayment, null);
        }

        StripeCheckoutSessionResponse checkoutSession =
                Objects.requireNonNull(
                        stripeCheckoutService.createCheckoutSession(
                                savedPayment,
                                offer
                        ),
                        "Stripe Checkout session nie może być null"
                );

        String sessionId = Objects.requireNonNull(
                checkoutSession.sessionId(),
                "Stripe Checkout session ID nie może być null"
        );

        savedPayment.setStripeCheckoutSessionId(sessionId);

        return new PaymentCreationResult(
                savedPayment,
                checkoutSession.checkoutUrl()
        );
    }

    /**
     * Determines the initial payment status.
     *
     * @param paymentMethod selected payment method
     * @return pending status for an online payment or not-required status
     *         for other payment methods
     */
    private PaymentStatus initialStatus(PaymentMethod paymentMethod) {
        return requiresOnlineCheckout(paymentMethod)
                ? PaymentStatus.OCZEKUJE_NA_PLATNOSC
                : PaymentStatus.NIE_WYMAGANA;
    }

    /**
     * Checks whether the payment method requires a Stripe Checkout session.
     *
     * @param paymentMethod selected payment method
     * @return {@code true} when online card payment is selected
     */
    private boolean requiresOnlineCheckout(PaymentMethod paymentMethod) {
        return paymentMethod == PaymentMethod.KARTA_ONLINE;
    }

    /**
     * Validates and normalizes the configured currency code.
     *
     * @param currency configured currency code
     * @return trimmed, uppercase currency code
     * @throws NullPointerException if the currency is {@code null}
     * @throws IllegalArgumentException if the currency is blank
     */
    private String normalizeCurrency(String currency) {
        String normalizedCurrency = Objects.requireNonNull(
                currency,
                "Waluta nie może być null"
        ).trim();

        if (normalizedCurrency.isEmpty()) {
            throw new IllegalArgumentException(
                    "Waluta nie może być pusta"
            );
        }

        return normalizedCurrency.toUpperCase(Locale.ROOT);
    }
}