package pl.barbershopproject.barbershop.order.mapper;

import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.utils.Status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

public final class OrderCreationDTOMapper {

    private OrderCreationDTOMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static Order toEntity(
            OrderCreationDTO dto,
            User user,
            Offer offer,
            Clock clock
    ) {
        Objects.requireNonNull(dto, "Dane zamówienia nie mogą być null");

        Objects.requireNonNull(user, "Użytkownik nie może być null");

        Objects.requireNonNull(offer, "Oferta nie może być null");

        Objects.requireNonNull(clock, "Clock nie może być null");

        return Order.builder()
                .user(user)
                .offer(offer)
                .bookedOffer(BookedOffer.from(offer))
                .orderDate(LocalDateTime.now(clock))
                .visitDate(dto.visitDate())
                .status(Status.NOWE)
                .build();
    }
}