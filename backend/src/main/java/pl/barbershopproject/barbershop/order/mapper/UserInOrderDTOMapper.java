package pl.barbershopproject.barbershop.order.mapper;

import pl.barbershopproject.barbershop.order.dto.UserInOrderDTO;
import pl.barbershopproject.barbershop.user.User;

public class UserInOrderDTOMapper {

    public static UserInOrderDTO toUserInOrderDTO(User user) {
        return new UserInOrderDTO(
                user.getIdUser(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail()
        );
    }
}
