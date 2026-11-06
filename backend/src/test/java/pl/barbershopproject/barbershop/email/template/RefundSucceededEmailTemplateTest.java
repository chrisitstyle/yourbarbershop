
package pl.barbershopproject.barbershop.email.template;

import org.junit.jupiter.api.Test;
import pl.barbershopproject.barbershop.payment.refund.event.RefundSucceededEvent;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.barbershopproject.barbershop.order.AdminCancellationResult.ReservationType;

class RefundSucceededEmailTemplateTest {

    @Test
    void shouldFormatGuestRefundConfirmation() {
        RefundSucceededEvent event = new RefundSucceededEvent(
                12L,
                42L,
                ReservationType.GUEST,
                "guest@example.com",
                "Anna",
                new BigDecimal("40.50"),
                "PLN");

        String plainText = RefundSucceededEmailTemplate.plainText(event);

        String html = RefundSucceededEmailTemplate.html(event);

        assertThat(plainText)
                .contains("Anna")
                .contains("40,50")
                .contains("Rezerwacja gościa #42")
                .contains("Numer zwrotu: #12");

        assertThat(html)
                .contains("Anna")
                .contains("40,50")
                .contains("Rezerwacja gościa #42")
                .contains("YourBarbershop");
    }

    @Test
    void shouldEscapeCustomerNameInHtml() {
        RefundSucceededEvent event = new RefundSucceededEvent(
                13L,
                43L,
                ReservationType.GUEST,
                "guest@example.com",
                "<script>alert('test')</script>",
                new BigDecimal("25.00"),
                "PLN");

        String html = RefundSucceededEmailTemplate.html(event);

        assertThat(html)
                .contains("&lt;script&gt;")
                .doesNotContain("<script>");
    }

    @Test
    void shouldUseGenericSalutationWhenFirstnameIsMissing() {
        RefundSucceededEvent event = new RefundSucceededEvent(
                14L,
                44L,
                ReservationType.REGISTERED,
                "customer@example.com",
                null,
                new BigDecimal("30.00"),
                "PLN");

        assertThat(RefundSucceededEmailTemplate.plainText(event)).contains("Cześć Kliencie!");

        assertThat(RefundSucceededEmailTemplate.html(event)).contains("Cześć Kliencie!");
    }
}
