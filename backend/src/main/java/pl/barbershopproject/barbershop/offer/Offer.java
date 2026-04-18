package pl.barbershopproject.barbershop.offer;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "offer")
@Entity

public class Offer implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long idOffer;
    private String kind;
    private BigDecimal cost;
}
