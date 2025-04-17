package pl.barbershopproject.barbershop.user.mapper;

import pl.barbershopproject.barbershop.user.dto.UserOrdersDTO;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.dto.UserDTO;

import java.util.List;

public class UserDTOMapper {
    public static UserDTO toDTO(User user) {
        List<UserOrdersDTO> ordersDTOs = user.getUserOrders()
                .stream()
                .map(UserOrdersDTOMapper::toOrdersInUserDTO).toList();

        return new UserDTO(user.getIdUser(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getRole(),
                ordersDTOs);
    }

    public static User toEntity(UserDTO userDTO) {
        User user = new User();
        user.setIdUser(userDTO.idUser());
        user.setFirstname(userDTO.firstname());
        user.setLastname(userDTO.lastname());
        user.setEmail(userDTO.email());
        user.setRole(userDTO.role());

        return user;
    }
}
