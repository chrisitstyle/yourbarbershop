package pl.barbershopproject.barbershop.guestorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestOrderRepository  extends JpaRepository<GuestOrder, Long> {

    List<GuestOrder> findGuestOrdersByStatus(String status);


}
