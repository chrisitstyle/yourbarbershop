package pl.barbershopproject.barbershop.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.offer " +
            "WHERE o.orderStatus = :orderStatus")
    List<Order> findOrdersByStatus(@Param("orderStatus") OrderStatus orderStatus);

    @Override
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.offer")
    List<Order> findAll();

}
