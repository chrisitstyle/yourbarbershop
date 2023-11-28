package pl.barbershopproject.barbershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.barbershopproject.barbershop.model.GuestOrder;

public interface GuestOrderRepository  extends JpaRepository<GuestOrder, Long> {


}
