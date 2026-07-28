package pl.barbershopproject.barbershop.order;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.utils.Status;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "user_order")
@Entity
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long idOrder;
    @ManyToOne
    @JoinColumn(name = "id_user")
    @JsonIgnoreProperties({"userOrders", "enabled", "authorities", "accountNonLocked", "credentialsNonExpired", "accountNonExpired", "password", "role"})
    private User user;
    @ManyToOne
    @JoinColumn(name = "id_offer")
    private Offer offer;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "visit_date")

    private LocalDateTime visitDate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;
}
