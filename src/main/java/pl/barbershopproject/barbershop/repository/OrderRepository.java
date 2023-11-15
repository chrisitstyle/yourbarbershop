package pl.barbershopproject.barbershop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.barbershopproject.barbershop.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {


}
