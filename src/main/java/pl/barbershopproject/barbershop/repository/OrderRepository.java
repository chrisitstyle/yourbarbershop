package pl.barbershopproject.barbershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.barbershopproject.barbershop.model.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


}
