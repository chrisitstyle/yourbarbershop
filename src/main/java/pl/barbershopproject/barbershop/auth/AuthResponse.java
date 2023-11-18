package pl.barbershopproject.barbershop.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.barbershopproject.barbershop.model.Role;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;
    private Role role;
}
