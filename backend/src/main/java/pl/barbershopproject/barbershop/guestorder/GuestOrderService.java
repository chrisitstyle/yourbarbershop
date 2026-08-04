package pl.barbershopproject.barbershop.guestorder;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.exception.IdempotencyConflictException;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationResponseDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderUpdateRequestDTO;
import pl.barbershopproject.barbershop.guestorder.mapper.GuestOrderDTOMapper;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestCollisionException;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestHasher;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestManager;
import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.payment.PaymentCheckout;
import pl.barbershopproject.barbershop.payment.PaymentOfferUpdater;
import pl.barbershopproject.barbershop.utils.Status;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GuestOrderService {

    private static final String ORDER_NOT_FOUND_MSG = "Nie znaleziono zamówienia o ID: ";

    private final GuestOrderRepository guestOrderRepository;
    private final OfferQuery offerQuery;
    private final AppointmentReservation appointmentReservation;
    private final PaymentOfferUpdater paymentOfferUpdater;
    private final GuestOrderEvents guestOrderEvents;
    private final GuestOrderCreationTransaction guestOrderCreationTransaction;
    private final PaymentCheckout paymentCheckout;
    private final IdempotencyRequestHasher idempotencyRequestHasher;
    private final IdempotencyRequestManager idempotencyRequestManager;

    @CacheEvict(value = "guestOrders", allEntries = true)
    public GuestOrderCreationResponseDTO addGuestOrder(
            GuestOrderCreationDTO guestOrderCreationDTO,
            String idempotencyKey
    ) {
        String requestHash = idempotencyRequestHasher.hash(
                "guest-order-creation-v1",
                "firstname",
                guestOrderCreationDTO.firstname(),
                "lastname",
                guestOrderCreationDTO.lastname(),
                "phonenumber",
                guestOrderCreationDTO.phonenumber(),
                "email",
                guestOrderCreationDTO.email(),
                "idOffer",
                guestOrderCreationDTO.idOffer(),
                "visitDate",
                guestOrderCreationDTO.visitDate(),
                "paymentMethod",
                guestOrderCreationDTO.paymentMethod().name()
        );

        GuestOrderCreationTransactionResult transactionResult =
                createGuestOrderTransaction(
                        guestOrderCreationDTO,
                        idempotencyKey,
                        requestHash
                );

        if (transactionResult.isInProgress()) {
            throw new IdempotencyConflictException(
                    "Żądanie z tym Idempotency-Key jest nadal przetwarzane"
            );
        }

        if (transactionResult.isCompleted()) {
            return createResponse(
                    transactionResult,
                    transactionResult.checkoutUrl()
            );
        }

        String checkoutUrl = paymentCheckout.createCheckoutIfRequired(
                transactionResult.checkoutRequest()
        );

        idempotencyRequestManager.markCompleted(
                transactionResult.idempotencyRequestId(),
                checkoutUrl
        );

        return createResponse(
                transactionResult,
                checkoutUrl
        );
    }

    public List<GuestOrderDTO> getAllGuestOrders() {
        return guestOrderRepository.findAll().stream()
                .map(GuestOrderDTOMapper::toDTO)
                .toList();
    }

    public GuestOrderDTO getGuestOrder(Long idGuestOrder) {
        return GuestOrderDTOMapper.toDTO(
                getRequiredGuestOrder(idGuestOrder)
        );
    }

    public List<GuestOrderDTO> getGuestOrdersByStatus(Status status) {
        return guestOrderRepository.findGuestOrdersByStatus(status).stream()
                .map(GuestOrderDTOMapper::toDTO)
                .toList();
    }

    @Transactional
    public GuestOrderDTO updateGuestOrder(
            GuestOrderUpdateRequestDTO request,
            Long idGuestOrder
    ) {
        GuestOrder guestOrder = getRequiredGuestOrder(idGuestOrder);

        Offer targetOffer = offerQuery.getRequiredOffer(
                request.idOffer()
        );

        Status oldStatus = guestOrder.getStatus();
        Status targetStatus = request.status() != null
                ? request.status()
                : oldStatus;

        updateOfferIfChanged(
                guestOrder,
                targetOffer
        );

        appointmentReservation.updateSlotReservation(
                guestOrder.getVisitDate(),
                oldStatus,
                request.visitDate(),
                targetStatus
        );

        applyUpdate(
                guestOrder,
                request,
                targetStatus
        );

        GuestOrder savedGuestOrder =
                guestOrderRepository.save(guestOrder);

        guestOrderEvents.updated(
                savedGuestOrder,
                oldStatus
        );

        return GuestOrderDTOMapper.toDTO(savedGuestOrder);
    }

    @Transactional
    public void deleteGuestOrderById(Long idGuestOrder) {
        GuestOrder guestOrder =
                getRequiredGuestOrder(idGuestOrder);

        appointmentReservation.releaseIfReserved(
                guestOrder.getVisitDate(),
                guestOrder.getStatus()
        );

        guestOrderRepository.delete(guestOrder);
        guestOrderEvents.deleted(idGuestOrder);
    }

    private GuestOrderCreationTransactionResult createGuestOrderTransaction(
            GuestOrderCreationDTO guestOrderCreationDTO,
            String idempotencyKey,
            String requestHash
    ) {
        try {
            return guestOrderCreationTransaction.create(
                    guestOrderCreationDTO,
                    idempotencyKey,
                    requestHash
            );
        } catch (IdempotencyRequestCollisionException firstCollision) {
            try {
                return guestOrderCreationTransaction.create(
                        guestOrderCreationDTO,
                        idempotencyKey,
                        requestHash
                );
            } catch (IdempotencyRequestCollisionException secondCollision) {
                throw new IdempotencyConflictException(
                        "Żądanie z tym Idempotency-Key jest już przetwarzane"
                );
            }
        }
    }

    private GuestOrderCreationResponseDTO createResponse(
            GuestOrderCreationTransactionResult transactionResult,
            String checkoutUrl
    ) {
        return new GuestOrderCreationResponseDTO(
                transactionResult.guestOrderId(),
                transactionResult.checkoutRequest().paymentMethod(),
                transactionResult.checkoutRequest().paymentStatus(),
                checkoutUrl
        );
    }

    private void updateOfferIfChanged(
            GuestOrder guestOrder,
            Offer targetOffer
    ) {
        if (!hasOfferChanged(
                guestOrder.getOffer(),
                targetOffer
        )) {
            return;
        }

        paymentOfferUpdater.updateAfterOfferChange(
                guestOrder.getPayment(),
                targetOffer
        );

        guestOrder.setOffer(targetOffer);
        guestOrder.setBookedOffer(
                BookedOffer.from(targetOffer)
        );
    }

    private boolean hasOfferChanged(
            Offer currentOffer,
            Offer targetOffer
    ) {
        if (currentOffer == null) {
            return true;
        }

        return !Objects.equals(
                currentOffer.getIdOffer(),
                targetOffer.getIdOffer()
        );
    }

    private GuestOrder getRequiredGuestOrder(Long idGuestOrder) {
        return guestOrderRepository.findById(idGuestOrder)
                .orElseThrow(() -> new NoSuchElementException(
                        ORDER_NOT_FOUND_MSG + idGuestOrder
                ));
    }

    private void applyUpdate(
            GuestOrder guestOrder,
            GuestOrderUpdateRequestDTO request,
            Status targetStatus
    ) {
        guestOrder.setFirstname(request.firstname());
        guestOrder.setLastname(request.lastname());
        guestOrder.setPhonenumber(request.phonenumber());
        guestOrder.setEmail(request.email());
        guestOrder.setVisitDate(request.visitDate());
        guestOrder.setStatus(targetStatus);
    }
}