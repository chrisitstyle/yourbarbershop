package pl.barbershopproject.barbershop.guestorder;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.util.Status;

import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "guest_order")
@Entity
public class GuestOrder {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long idGuestOrder;
    private String firstname;
    private String lastname;
    private String phonenumber;
    private String email;
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
