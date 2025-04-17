package pl.barbershopproject.barbershop.order.dto;

import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.util.Status;

import java.time.LocalDateTime;

public record OrderDTO(
        long idOrder,
        UserInOrderDTO user,
        Offer offer,
        LocalDateTime orderDate,
        LocalDateTime visitDate,
        Status status
        ) {
}
