package pl.barbershopproject.barbershop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "user")
@Entity
public class User {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long idUser;
    private String firstname;
    private String lastname;
    @Column(name = "e-mail")
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
}
