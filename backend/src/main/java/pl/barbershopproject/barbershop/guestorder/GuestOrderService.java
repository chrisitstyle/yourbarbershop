package pl.barbershopproject.barbershop.guestorder;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.exception.IdempotencyConflictException;
import pl.barbershopproject.barbershop.exception.MissingPaymentException;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationResponseDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderUpdateRequestDTO;
import pl.barbershopproject.barbershop.guestorder.mapper.GuestOrderDTOMapper;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestCollisionException;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestHasher;
import pl.barbershopproject.barbershop.ordercreation.OrderCreationCompletionHandler;
import pl.barbershopproject.barbershop.orderupdate.OrderUpdateCoordinator;
import pl.barbershopproject.barbershop.orderupdate.OrderUpdateResult;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class GuestOrderService {

    private static final String GUEST_ORDER_NOT_FOUND_MSG = "Nie znaleziono zamówienia gościa o ID: ";

    private final GuestOrderRepository guestOrderRepository;
    private final AppointmentReservation appointmentReservation;
    private final GuestOrderEvents guestOrderEvents;
    private final OrderUpdateCoordinator orderUpdateCoordinator;
    private final GuestOrderCreationTransaction guestOrderCreationTransaction;
    private final IdempotencyRequestHasher idempotencyRequestHasher;
    private final OrderCreationCompletionHandler orderCreationCompletionHandler;

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

        GuestOrderCreationTransactionResult transactionResult = createGuestOrderTransaction(
                guestOrderCreationDTO,
                idempotencyKey,
                requestHash);

        String checkoutUrl = orderCreationCompletionHandler.complete(transactionResult);

        return createResponse(transactionResult, checkoutUrl);
    }

    public List<GuestOrderDTO> getAllGuestOrders() {
        return guestOrderRepository.findAll().stream()
                .map(GuestOrderDTOMapper::toDTO)
                .toList();
    }

    public GuestOrderDTO getGuestOrder(Long idGuestOrder) {
        return GuestOrderDTOMapper.toDTO(
                getRequiredGuestOrder(idGuestOrder));
    }

    public List<GuestOrderDTO> getGuestOrdersByStatus(
            OrderStatus orderStatus) {
        return guestOrderRepository
                .findGuestOrdersByStatus(orderStatus)
                .stream()
                .map(GuestOrderDTOMapper::toDTO)
                .toList();
    }

    @Transactional
    public GuestOrderDTO updateGuestOrder(
            GuestOrderUpdateRequestDTO request,
            Long idGuestOrder
    ) {
        GuestOrder guestOrder = getRequiredGuestOrder(idGuestOrder);

        Payment payment = getRequiredPayment(guestOrder);

        OrderUpdateResult updateResult = orderUpdateCoordinator.prepareUpdate(
                guestOrder,
                payment,
                request.idOffer(),
                request.visitDate(),
                request.orderStatus());

        applyUpdate(
                guestOrder,
                request,
                updateResult.targetStatus());

        GuestOrder savedGuestOrder = guestOrderRepository.save(guestOrder);

        guestOrderEvents.updated(
                savedGuestOrder,
                updateResult.currentStatus());

        return GuestOrderDTOMapper.toDTO(
                savedGuestOrder);
    }

    @Transactional
    public void deleteGuestOrderById(Long idGuestOrder) {
        GuestOrder guestOrder =
                getRequiredGuestOrder(idGuestOrder);

        appointmentReservation.releaseIfReserved(
                guestOrder.getVisitDate(),
                guestOrder.getOrderStatus());

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
        } catch (IdempotencyRequestCollisionException _) {
            try {
                return guestOrderCreationTransaction.create(
                        guestOrderCreationDTO,
                        idempotencyKey,
                        requestHash
                );
            } catch (IdempotencyRequestCollisionException _) {
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

    private GuestOrder getRequiredGuestOrder(
            Long idGuestOrder
    ) {
        return guestOrderRepository.findById(idGuestOrder)
                .orElseThrow(() -> new NoSuchElementException(
                        GUEST_ORDER_NOT_FOUND_MSG
                                + idGuestOrder
                ));
    }

    private Payment getRequiredPayment(
            GuestOrder guestOrder
    ) {
        Payment payment = guestOrder.getPayment();

        if (payment == null) {
            throw new MissingPaymentException(
                    "Zamówienie gościa",
                    guestOrder.getIdGuestOrder()
            );
        }

        return payment;
    }

    private void applyUpdate(
            GuestOrder guestOrder,
            GuestOrderUpdateRequestDTO request,
            OrderStatus targetOrderStatus
    ) {
        guestOrder.setFirstname(request.firstname());
        guestOrder.setLastname(request.lastname());
        guestOrder.setPhonenumber(request.phonenumber());
        guestOrder.setEmail(request.email());
        guestOrder.setVisitDate(request.visitDate());
        guestOrder.setOrderStatus(targetOrderStatus);
    }
}