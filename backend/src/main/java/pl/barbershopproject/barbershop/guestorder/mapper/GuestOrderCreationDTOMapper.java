package pl.barbershopproject.barbershop.guestorder.mapper;

import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.util.Status;

import java.time.LocalDateTime;

public class GuestOrderCreationDTOMapper {

    private GuestOrderCreationDTOMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static GuestOrder toEntity(GuestOrderCreationDTO dto, Offer offer) {
        return GuestOrder.builder()
                .firstname(dto.firstname())
                .lastname(dto.lastname())
                .phonenumber(dto.phonenumber())
                .email(dto.email())
                .offer(offer)
                .orderDate(LocalDateTime.now())
                .visitDate(dto.visitDate())
                .status(Status.NOWE)
                .paymentMethod(dto.paymentMethod())
                .paymentStatus(PaymentStatus.OCZEKUJE_NA_PLATNOSC)
                .paymentAmount(offer.getCost())
                .paymentCurrency("PLN")
                .build();
    }
}
