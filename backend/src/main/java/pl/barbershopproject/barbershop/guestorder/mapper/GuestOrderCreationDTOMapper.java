package pl.barbershopproject.barbershop.guestorder.mapper;

import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

public final class GuestOrderCreationDTOMapper {

    private GuestOrderCreationDTOMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static GuestOrder toEntity(
            GuestOrderCreationDTO dto,
            Offer offer,
            Clock clock
    ) {
        Objects.requireNonNull(dto, "Dane zamówienia gościa nie mogą być null");

        Objects.requireNonNull(offer, "Oferta nie może być null");

        Objects.requireNonNull(
                clock,
                "Clock nie może być null"
        );

        return GuestOrder.builder()
                .firstname(dto.firstname())
                .lastname(dto.lastname())
                .phonenumber(dto.phonenumber())
                .email(dto.email())
                .offer(offer)
                .bookedOffer(BookedOffer.from(offer))
                .orderDate(LocalDateTime.now(clock))
                .visitDate(dto.visitDate())
                .orderStatus(OrderStatus.NOWE)
                .build();
    }
}