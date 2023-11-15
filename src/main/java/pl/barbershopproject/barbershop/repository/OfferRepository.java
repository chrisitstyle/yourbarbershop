package pl.barbershopproject.barbershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.barbershopproject.barbershop.model.Offer;

import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {


}

