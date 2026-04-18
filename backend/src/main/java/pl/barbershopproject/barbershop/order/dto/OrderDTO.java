package pl.barbershopproject.barbershop.order.dto;

import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.util.Status;

import java.io.Serializable;
import java.time.LocalDateTime;


public record OrderDTO(
        Long idOrder,
        UserInOrderDTO user,
        Offer offer,
        LocalDateTime orderDate,
        LocalDateTime visitDate,
        Status status
) implements Serializable {
}
