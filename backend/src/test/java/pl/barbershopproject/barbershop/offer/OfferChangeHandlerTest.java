package pl.barbershopproject.barbershop.offer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentOfferUpdater;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.offerBuilder;

class OfferChangeHandlerTest {

    private PaymentOfferUpdater paymentOfferUpdater;

    private OfferChangeHandler offerChangeHandler;

    @BeforeEach
    void setUp() {
        paymentOfferUpdater = mock(PaymentOfferUpdater.class);

        offerChangeHandler = new OfferChangeHandler(paymentOfferUpdater);
    }

    @Test
    void shouldUpdateOfferWhenCurrentOfferIsNull() {
        Payment payment = mock(Payment.class);

        Offer targetOffer = offerBuilder()
                .idOffer(2L)
                .kind("new_offer")
                .cost(new BigDecimal("160.00"))
                .build();

        AtomicReference<Offer> updatedOffer = new AtomicReference<>();

        AtomicReference<BookedOffer> updatedBookedOffer = new AtomicReference<>();

        offerChangeHandler.updateIfChanged(
                null,
                targetOffer,
                payment,
                updatedOffer::set,
                updatedBookedOffer::set
        );

        verify(paymentOfferUpdater)
                .updateAfterOfferChange(
                        payment,
                        targetOffer);

        assertThat(updatedOffer.get())
                .isSameAs(targetOffer);

        assertBookedOfferMatches(
                updatedBookedOffer.get(),
                targetOffer);
    }

    @Test
    void shouldUpdateOfferWhenOfferHasChanged() {
        Payment payment = mock(Payment.class);

        Offer currentOffer = offerBuilder()
                .idOffer(1L)
                .build();

        Offer targetOffer = offerBuilder()
                .idOffer(2L)
                .kind("new_offer")
                .cost(new BigDecimal("160.00"))
                .build();

        AtomicReference<Offer> updatedOffer = new AtomicReference<>();

        AtomicReference<BookedOffer> updatedBookedOffer = new AtomicReference<>();

        offerChangeHandler.updateIfChanged(
                currentOffer,
                targetOffer,
                payment,
                updatedOffer::set,
                updatedBookedOffer::set
        );

        verify(paymentOfferUpdater)
                .updateAfterOfferChange(
                        payment,
                        targetOffer);

        assertThat(updatedOffer.get())
                .isSameAs(targetOffer);

        assertBookedOfferMatches(
                updatedBookedOffer.get(),
                targetOffer
        );
    }

    @Test
    void shouldNotUpdateOfferWhenOfferHasNotChanged() {
        Payment payment = mock(Payment.class);

        Offer currentOffer = offerBuilder()
                .idOffer(1L)
                .kind("current_offer")
                .build();

        Offer targetOffer = offerBuilder()
                .idOffer(1L)
                .kind("different_name")
                .cost(new BigDecimal("200.00"))
                .build();

        AtomicReference<Offer> updatedOffer =
                new AtomicReference<>();

        AtomicReference<BookedOffer> updatedBookedOffer = new AtomicReference<>();

        offerChangeHandler.updateIfChanged(
                currentOffer,
                targetOffer,
                payment,
                updatedOffer::set,
                updatedBookedOffer::set
        );

        verifyNoInteractions(paymentOfferUpdater);

        assertThat(updatedOffer.get())
                .isNull();

        assertThat(updatedBookedOffer.get())
                .isNull();
    }

    private void assertBookedOfferMatches(
            BookedOffer bookedOffer,
            Offer offer) {
        assertThat(bookedOffer).isNotNull();

        assertThat(bookedOffer.getName())
                .isEqualTo(offer.getKind());

        assertThat(bookedOffer.getPrice())
                .isEqualByComparingTo(offer.getCost());
    }
}
