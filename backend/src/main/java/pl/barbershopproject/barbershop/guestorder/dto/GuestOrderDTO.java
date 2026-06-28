package pl.barbershopproject.barbershop.guestorder.dto;

import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.util.Status;

import java.time.LocalDateTime;

public record GuestOrderDTO(
        Long idGuestOrder,
        String firstname,
        String lastname,
        String phonenumber,
        String email,
        Offer offer,
        LocalDateTime orderDate,
        LocalDateTime visitDate,
        Status status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus
) {
}
