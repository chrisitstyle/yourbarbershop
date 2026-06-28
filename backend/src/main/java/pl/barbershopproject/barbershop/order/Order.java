package pl.barbershopproject.barbershop.order;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.util.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "user_order")
@Entity
public class Order {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long idOrder;
    @ManyToOne
    @JoinColumn(name = "id_user")
    @JsonIgnoreProperties({"userOrders","enabled","authorities","accountNonLocked","credentialsNonExpired","accountNonExpired", "password","role"})
    private User user;
    @ManyToOne
    @JoinColumn(name = "id_offer")
    private Offer offer;

    @Column (name = "order_date")
    private LocalDateTime orderDate;

    @Column (name = "visit_date")

    private LocalDateTime visitDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "stripe_checkout_session_id", unique = true)
    private String stripeCheckoutSessionId;

    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "payment_amount")
    private BigDecimal paymentAmount;

    @Column(name = "payment_currency")
    private String paymentCurrency;

}
