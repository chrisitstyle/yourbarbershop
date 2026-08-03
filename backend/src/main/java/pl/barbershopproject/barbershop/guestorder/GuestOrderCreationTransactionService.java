package pl.barbershopproject.barbershop.guestorder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.mapper.GuestOrderCreationDTOMapper;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.payment.PaymentCreationResult;
import pl.barbershopproject.barbershop.payment.PaymentCreator;

import java.time.Clock;

@Service
@RequiredArgsConstructor
class GuestOrderCreationTransactionService implements GuestOrderCreationTransaction {

    private final GuestOrderRepository guestOrderRepository;
    private final OfferQuery offerQuery;
    private final AppointmentReservation appointmentReservation;
    private final PaymentCreator paymentCreator;
    private final GuestOrderEvents guestOrderEvents;
    private final Clock clock;

    @Override
    // persists the guest order, appointment slot and payment atomically in one transaction.
    @Transactional
    public GuestOrderCreationTransactionResult create(
            GuestOrderCreationDTO guestOrderCreationDTO) {
        Offer offer = offerQuery.getRequiredOffer(guestOrderCreationDTO.idOffer());

        GuestOrder guestOrder = GuestOrderCreationDTOMapper.toEntity(
                guestOrderCreationDTO,
                offer,
                clock
        );

        appointmentReservation.reserveSlot(guestOrder.getVisitDate());

        GuestOrder savedGuestOrder = guestOrderRepository.save(guestOrder);

        PaymentCreationResult paymentCreationResult = paymentCreator.createForGuestOrder(
                        savedGuestOrder,
                        guestOrderCreationDTO.paymentMethod());

        guestOrderEvents.created(savedGuestOrder,
                paymentCreationResult.payment()
        );

        return new GuestOrderCreationTransactionResult(
                savedGuestOrder.getIdGuestOrder(),
                paymentCreationResult.checkoutRequest()
        );
    }
}
