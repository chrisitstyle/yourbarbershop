package pl.barbershopproject.barbershop.guestorder;


import jakarta.persistence.*;
import lombok.*;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.util.Status;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "guest_order")
@Entity
public class GuestOrder implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long idGuestOrder;
    private String firstname;
    private String lastname;
    private String phonenumber;
    private String email;
    @ManyToOne
    @JoinColumn(name = "id_offer")
    private Offer offer;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "visit_date")

    private LocalDateTime visitDate;
    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToOne(mappedBy = "guestOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;
}
