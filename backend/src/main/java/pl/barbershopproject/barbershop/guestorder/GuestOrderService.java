package pl.barbershopproject.barbershop.guestorder;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentAvailabilityService;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationResponseDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderDTO;
import pl.barbershopproject.barbershop.guestorder.mapper.GuestOrderCreationDTOMapper;
import pl.barbershopproject.barbershop.guestorder.mapper.GuestOrderDTOMapper;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.order.event.OrderCreatedEvent;
import pl.barbershopproject.barbershop.payment.*;
import pl.barbershopproject.barbershop.util.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
class GuestOrderService {

    private final GuestOrderRepository guestOrderRepository;
    private final OfferRepository offerRepository;
    private final AppointmentAvailabilityService appointmentAvailabilityService;
    private final StripeCheckoutService stripeCheckoutService;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public GuestOrderCreationResponseDTO addGuestOrder(GuestOrderCreationDTO guestOrderCreationDTO) {
        Offer offer = offerRepository.findById(guestOrderCreationDTO.idOffer())
                .orElseThrow(() -> new NoSuchElementException("Oferta o ID: " + guestOrderCreationDTO.idOffer() + " nie istnieje"));

        GuestOrder guestOrderToSave = GuestOrderCreationDTOMapper.toEntity(guestOrderCreationDTO, offer);
        appointmentAvailabilityService.reserveSlot(guestOrderToSave.getVisitDate());

        GuestOrder savedGuestOrder = guestOrderRepository.save(guestOrderToSave);

        Payment paymentToSave = Payment.builder()
                .guestOrder(savedGuestOrder)
                .paymentMethod(guestOrderCreationDTO.paymentMethod())
                .paymentStatus(PaymentStatus.OCZEKUJE_NA_PLATNOSC)
                .amount(offer.getCost())
                .currency("PLN")
                .createdAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(paymentToSave);

        if (savedPayment.getPaymentMethod() == PaymentMethod.KARTA_ONLINE) {
            StripeCheckoutSessionResponse checkoutSession = stripeCheckoutService.createCheckoutSession(
                    savedPayment,
                    offer
            );

            savedPayment.setStripeCheckoutSessionId(checkoutSession.sessionId());
            paymentRepository.save(savedPayment);

            return new GuestOrderCreationResponseDTO(
                    savedGuestOrder.getIdGuestOrder(),
                    savedPayment.getPaymentMethod(),
                    savedPayment.getPaymentStatus(),
                    checkoutSession.checkoutUrl()
            );
        }

        publishOrderCreatedEvent(savedGuestOrder, savedPayment);

        return new GuestOrderCreationResponseDTO(
                savedGuestOrder.getIdGuestOrder(),
                savedPayment.getPaymentMethod(),
                savedPayment.getPaymentStatus(),
                null
        );
    }

    public List<GuestOrderDTO> getAllGuestOrders() {
        return guestOrderRepository.findAll().stream()
                .map(GuestOrderDTOMapper::toDTO)
                .toList();
    }

    public GuestOrderDTO getGuestOrder(Long idGuestOrder) {
        return guestOrderRepository.findById(idGuestOrder)
                .map(GuestOrderDTOMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Nie znaleziono zamówienia o ID: " + idGuestOrder));
    }

    public List<GuestOrderDTO> getGuestOrdersByStatus(Status status) {
        return guestOrderRepository.findGuestOrdersByStatus(status).stream()
                .map(GuestOrderDTOMapper::toDTO)
                .toList();
    }

    @Transactional
    public GuestOrder updateGuestOrder(GuestOrder updatedGuestOrder, Long idGuestOrder) {
        GuestOrder existingOrder = guestOrderRepository.findById(idGuestOrder)
                .orElseThrow(() -> new NoSuchElementException("Nie znaleziono zamówienia o ID: " + idGuestOrder));

        Status targetStatus = updatedGuestOrder.getStatus() != null
                ? updatedGuestOrder.getStatus()
                : existingOrder.getStatus();

        appointmentAvailabilityService.updateSlotReservation(
                existingOrder.getVisitDate(),
                existingOrder.getStatus(),
                updatedGuestOrder.getVisitDate(),
                targetStatus
        );

        existingOrder.setFirstname(updatedGuestOrder.getFirstname());
        existingOrder.setLastname(updatedGuestOrder.getLastname());
        existingOrder.setPhonenumber(updatedGuestOrder.getPhonenumber());
        existingOrder.setEmail(updatedGuestOrder.getEmail());
        existingOrder.setOffer(updatedGuestOrder.getOffer());
        existingOrder.setVisitDate(updatedGuestOrder.getVisitDate());
        existingOrder.setStatus(targetStatus);

        return guestOrderRepository.save(existingOrder);
    }

    @Transactional
    public void deleteGuestOrderById(Long idGuestOrder) {
        GuestOrder guestOrder = guestOrderRepository.findById(idGuestOrder)
                .orElseThrow(() -> new NoSuchElementException("Nie znaleziono zamówienia o ID: " + idGuestOrder));

        appointmentAvailabilityService.releaseIfReserved(guestOrder.getVisitDate(), guestOrder.getStatus());

        guestOrderRepository.delete(guestOrder);
    }

    private void publishOrderCreatedEvent(GuestOrder guestOrder, Payment payment) {
        eventPublisher.publishEvent(new OrderCreatedEvent(
                guestOrder.getEmail(),
                guestOrder.getFirstname(),
                guestOrder.getVisitDate(),
                guestOrder.getOffer().getKind(),
                guestOrder.getOffer().getCost(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus()
        ));
    }
}