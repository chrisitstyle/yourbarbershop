package pl.barbershopproject.barbershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.barbershopproject.barbershop.model.GuestOrder;
@Repository
public interface GuestOrderRepository  extends JpaRepository<GuestOrder, Long> {


}
