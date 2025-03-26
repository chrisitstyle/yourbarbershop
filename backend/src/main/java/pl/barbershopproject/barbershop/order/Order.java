package pl.barbershopproject.barbershop.order;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.util.Status;

import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "user_order")
@Entity
public class Order {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long idOrder;
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

}
