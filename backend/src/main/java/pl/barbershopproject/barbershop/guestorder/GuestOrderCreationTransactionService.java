package pl.barbershopproject.barbershop.guestorder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.mapper.GuestOrderCreationDTOMapper;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestManager;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestResult;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.payment.PaymentCreationResult;
import pl.barbershopproject.barbershop.payment.PaymentCreator;

import java.time.Clock;

@Service
@RequiredArgsConstructor
class GuestOrderCreationTransactionService
        implements GuestOrderCreationTransaction {

    private final GuestOrderRepository guestOrderRepository;
    private final OfferQuery offerQuery;
    private final AppointmentReservation appointmentReservation;
    private final PaymentCreator paymentCreator;
    private final GuestOrderEvents guestOrderEvents;
    private final IdempotencyRequestManager idempotencyRequestManager;
    private final Clock clock;

    @Override
    // resolves idempotency and persists the guest order, slot and payment atomically.
    @Transactional
    public GuestOrderCreationTransactionResult create(
            GuestOrderCreationDTO guestOrderCreationDTO,
            String idempotencyKey,
            String requestHash
    ) {
        IdempotencyRequestResult idempotencyResult = idempotencyRequestManager.startGuestOrderCreation(
                        idempotencyKey,
                        requestHash);

        return switch (idempotencyResult.resolution()) {
            case NEW -> createNewGuestOrder(
                    idempotencyResult.requestId(),
                    guestOrderCreationDTO
            );

            case IN_PROGRESS -> GuestOrderCreationTransactionResult.inProgress(
                            idempotencyResult.requestId());

            case RESOURCE_CREATED -> GuestOrderCreationTransactionResult.resourceCreated(
                            idempotencyResult.requestId(),
                            idempotencyResult.resourceId(),
                            idempotencyResult.checkoutRequest());

            case COMPLETED -> GuestOrderCreationTransactionResult.completed(
                            idempotencyResult.requestId(),
                            idempotencyResult.resourceId(),
                            idempotencyResult.checkoutRequest(),
                            idempotencyResult.checkoutUrl());
        };
    }

    private GuestOrderCreationTransactionResult createNewGuestOrder(
            Long idempotencyRequestId,
            GuestOrderCreationDTO guestOrderCreationDTO
    ) {
        Offer offer = offerQuery.getRequiredOffer(
                guestOrderCreationDTO.idOffer());

        GuestOrder guestOrder = GuestOrderCreationDTOMapper.toEntity(
                guestOrderCreationDTO,
                offer,
                clock
        );

        appointmentReservation.reserveSlot(
                guestOrder.getVisitDate()
        );

        GuestOrder savedGuestOrder = guestOrderRepository.save(guestOrder);

        PaymentCreationResult paymentCreationResult = paymentCreator.createForGuestOrder(
                        savedGuestOrder,
                        guestOrderCreationDTO.paymentMethod());

        guestOrderEvents.created(
                savedGuestOrder,
                paymentCreationResult.payment()
        );

        idempotencyRequestManager.markResourceCreated(
                idempotencyRequestId,
                savedGuestOrder.getIdGuestOrder(),
                paymentCreationResult.checkoutRequest()
        );

        return GuestOrderCreationTransactionResult.resourceCreated(
                idempotencyRequestId,
                savedGuestOrder.getIdGuestOrder(),
                paymentCreationResult.checkoutRequest()
        );
    }
}