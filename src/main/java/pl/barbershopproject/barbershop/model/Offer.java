package pl.barbershopproject.barbershop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Table(name = "offer")
@Entity
public class Offer {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long idOffer;
    private String kind;
    private BigDecimal cost;
}
