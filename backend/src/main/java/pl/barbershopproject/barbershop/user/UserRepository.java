package pl.barbershopproject.barbershop.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.userOrders o " +
            "LEFT JOIN FETCH o.offer")
    List<User> findAllWithOrders();

    @Override
    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.userOrders o " +
            "LEFT JOIN FETCH o.offer " +
            "WHERE u.idUser = ?1")
    Optional<User> findById(Long id);
}
