package pl.barbershopproject.barbershop.user.mapper;

import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.user.dto.UserOrdersDTO;

public class UserOrdersDTOMapper {
    public static UserOrdersDTO toOrdersInUserDTO(Order order) {
        return new UserOrdersDTO(
                order.getIdOrder(),
                order.getOffer(),
                order.getOrderDate(),
                order.getVisitDate(),
                order.getStatus()
        );
    }
}
