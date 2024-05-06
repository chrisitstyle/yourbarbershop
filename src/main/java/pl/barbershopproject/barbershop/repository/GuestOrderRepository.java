package pl.barbershopproject.barbershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.barbershopproject.barbershop.model.GuestOrder;

import java.util.List;

@Repository
public interface GuestOrderRepository  extends JpaRepository<GuestOrder, Long> {

    List<GuestOrder> findGuestOrdersByStatus(String status);


}
