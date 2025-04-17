package pl.barbershopproject.barbershop.user.dto;

import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.util.Status;

import java.time.LocalDateTime;

public record UserOrdersDTO(
        long idOrder,
        Offer offer,
        LocalDateTime orderDate,
        LocalDateTime visitDate,
        Status status
) {}