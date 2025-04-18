package pl.barbershopproject.barbershop.guestorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.barbershopproject.barbershop.util.Status;

import java.util.List;

@Repository
public interface GuestOrderRepository  extends JpaRepository<GuestOrder, Long> {

    @Query("SELECT DISTINCT g FROM GuestOrder g LEFT JOIN FETCH g.offer WHERE g.status = :status")
    List<GuestOrder> findGuestOrdersByStatus(@Param("status") Status status);


    @Override
    @Query("SELECT DISTINCT g FROM GuestOrder g LEFT JOIN FETCH g.offer")
    List<GuestOrder> findAll();


}
