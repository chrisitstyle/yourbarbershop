package pl.barbershopproject.barbershop.order.mapper;

import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.util.Status;

import java.time.LocalDateTime;

public class OrderCreationDTOMapper {

    private OrderCreationDTOMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static Order toEntity(OrderCreationDTO dto, User user, Offer offer) {
        return Order.builder()
                .user(user)
                .offer(offer)
                .orderDate(LocalDateTime.now())
                .visitDate(dto.visitDate())
                .status(Status.NOWE)
                .build();
    }
}
