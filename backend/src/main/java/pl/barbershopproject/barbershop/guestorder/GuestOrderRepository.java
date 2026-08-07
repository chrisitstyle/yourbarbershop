package pl.barbershopproject.barbershop.guestorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.util.List;

@Repository
public interface GuestOrderRepository extends JpaRepository<GuestOrder, Long> {

    @Query("SELECT DISTINCT g FROM GuestOrder g LEFT JOIN FETCH g.offer WHERE g.orderStatus = :orderStatus")
    List<GuestOrder> findGuestOrdersByStatus(@Param("orderStatus") OrderStatus orderStatus);


    @Override
    @Query("SELECT DISTINCT g FROM GuestOrder g LEFT JOIN FETCH g.offer")
    List<GuestOrder> findAll();

}
