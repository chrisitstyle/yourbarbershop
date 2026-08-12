package pl.barbershopproject.barbershop.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import pl.barbershopproject.barbershop.order.Order;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name = "\"user\"")
@Entity
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long idUser;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties({"user"})
    @Builder.Default
    private List<Order> userOrders = new ArrayList<>();

}
