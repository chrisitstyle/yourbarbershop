package pl.barbershopproject.barbershop.user.mapper;

import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.dto.UserCreationDTO;
import pl.barbershopproject.barbershop.user.dto.UserResponseDTO;

public class UserCreationDTOMapper {

    private UserCreationDTOMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getIdUser(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getRole()
        );
    }

    public static User toEntity(UserCreationDTO dto) {
        User user = new User();
        user.setFirstname(dto.firstname());
        user.setLastname(dto.lastname());
        user.setEmail(dto.email());
        user.setPassword(dto.password());
        user.setRole(Role.valueOf(dto.role()));

        return user;
    }
}
