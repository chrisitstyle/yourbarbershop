package pl.barbershopproject.barbershop.order.mapper;

import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.dto.UserInOrderDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;

public class OrderDTOMapper {

    public static OrderDTO toDTO(Order order) {

        UserInOrderDTO userInOrder = new UserInOrderDTO(
                order.getUser().getIdUser(),
                order.getUser().getFirstname(),
                order.getUser().getLastname(),
                order.getUser().getEmail()
        );

        
        return new OrderDTO(
                order.getIdOrder(),
                userInOrder,
                order.getOffer(),
                order.getOrderDate(),
                order.getVisitDate(),
                order.getStatus()
        );
    }
}
