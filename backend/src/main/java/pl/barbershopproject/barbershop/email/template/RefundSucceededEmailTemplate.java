
package pl.barbershopproject.barbershop.email.template;

import pl.barbershopproject.barbershop.payment.refund.event.RefundSucceededEvent;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Builds plain-text and HTML confirmation messages for
 * successfully completed Stripe refunds.
 */
public final class RefundSucceededEmailTemplate {

    private static final String SUBJECT = "Potwierdzenie zwrotu środków - YourBarbershop";

    private static final Locale POLISH_LOCALE = Locale.of("pl", "PL");

    private RefundSucceededEmailTemplate() {}

    /**
     * Returns the successful refund notification subject.
     *
     * @return email subject
     */
    public static String subject() {
        return SUBJECT;
    }

    /**
     * Builds the plain-text refund confirmation.
     *
     * @param event successfully completed refund details
     * @return plain-text message
     */
    public static String plainText(RefundSucceededEvent event) {
        return """
                Cześć %s!

                Potwierdzamy, że zwrot płatności za anulowaną
                rezerwację w YourBarbershop został pomyślnie
                zrealizowany przez Stripe.

                Kwota zwrotu: %s
                Rezerwacja: %s
                Numer zwrotu: #%s

                Środki zostaną zaksięgowane zgodnie z terminami
                obowiązującymi w Twoim banku.

                Nie musisz podejmować żadnych dodatkowych działań.

                Dziękujemy za skorzystanie z usług YourBarbershop!

                Z poważaniem,
                Zespół YourBarbershop

                To jest wiadomość automatyczna.
                """.formatted(
                customerFirstname(event),
                formatAmount(event),
                reservationDescription(event),
                event.refundId());
    }

    /**
     * Builds a branded HTML refund confirmation.
     * <p>
     * Customer-provided content is escaped before being inserted
     * into the HTML document.
     *
     * @param event successfully completed refund details
     * @return HTML message
     */
    public static String html(RefundSucceededEvent event) {
        String safeFirstname = escapeHtml(customerFirstname(event));

        String safeFormattedAmount = escapeHtml(formatAmount(event));

        String safeReservationDescription = escapeHtml(reservationDescription(event));

        String safeRefundId = escapeHtml(String.valueOf(event.refundId()));

        return """
                <!doctype html>
                <html lang="pl">
                  <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0" />
                    <title>Potwierdzenie zwrotu środków</title>
                  </head>

                  <body style="margin:0; padding:0;
                               background-color:#f4f4f5;
                               font-family:Arial, Helvetica, sans-serif;
                               color:#18181b;">

                    <table role="presentation"
                           width="100%%"
                           cellspacing="0"
                           cellpadding="0"
                           style="background-color:#f4f4f5;
                                  padding:32px 16px;">
                      <tr>
                        <td align="center">

                          <table role="presentation"
                                 width="100%%"
                                 cellspacing="0"
                                 cellpadding="0"
                                 style="max-width:560px;
                                        background-color:#ffffff;
                                        border-radius:16px;
                                        overflow:hidden;
                                        border:1px solid #e4e4e7;">

                            <tr>
                              <td style="background-color:#18181b;
                                         padding:30px 32px;
                                         text-align:center;">

                                <h1 style="margin:0;
                                           color:#ffffff;
                                           font-size:28px;">
                                  YourBarbershop
                                </h1>

                                <p style="margin:10px 0 0;
                                          color:#d4d4d8;
                                          font-size:14px;">
                                  Potwierdzenie zwrotu płatności
                                </p>
                              </td>
                            </tr>

                            <tr>
                              <td style="padding:34px 32px;">

                                <h2 style="margin:0 0 14px;
                                           color:#18181b;
                                           font-size:23px;">
                                  Cześć %s!
                                </h2>

                                <p style="margin:0 0 24px;
                                          color:#52525b;
                                          font-size:15px;
                                          line-height:1.7;">
                                  Potwierdzamy, że zwrot płatności
                                  za anulowaną rezerwację został
                                  pomyślnie zrealizowany przez Stripe.
                                </p>

                                <table role="presentation"
                                       width="100%%"
                                       cellspacing="0"
                                       cellpadding="0"
                                       style="border:1px solid #e4e4e7;
                                              border-radius:12px;
                                              margin-bottom:24px;">

                                  <tr>
                                    <td style="padding:22px 20px;
                                               background-color:#f0fdf4;
                                               border-bottom:1px solid #e4e4e7;">

                                      <p style="margin:0 0 8px;
                                                font-size:13px;
                                                color:#52525b;">
                                        Kwota zwrotu
                                      </p>

                                      <p style="margin:0;
                                                font-size:28px;
                                                font-weight:bold;
                                                color:#15803d;">
                                        %s
                                      </p>
                                    </td>
                                  </tr>

                                  <tr>
                                    <td style="padding:18px 20px;">
                                      <p style="margin:0 0 8px;
                                                font-size:13px;
                                                color:#71717a;">
                                        Rezerwacja
                                      </p>

                                      <p style="margin:0;
                                                font-size:15px;
                                                font-weight:bold;
                                                color:#18181b;">
                                        %s
                                      </p>
                                    </td>
                                  </tr>

                                  <tr>
                                    <td style="padding:0 20px 18px;">
                                      <p style="margin:0 0 8px;
                                                font-size:13px;
                                                color:#71717a;">
                                        Numer zwrotu
                                      </p>

                                      <p style="margin:0;
                                                font-size:14px;
                                                color:#18181b;">
                                        #%s
                                      </p>
                                    </td>
                                  </tr>
                                </table>

                                <p style="margin:0 0 18px;
                                          font-size:14px;
                                          color:#52525b;
                                          line-height:1.7;">
                                  Środki zostaną zaksięgowane zgodnie
                                  z terminami obowiązującymi
                                  w Twoim banku.
                                </p>

                                <p style="margin:0 0 26px;
                                          font-size:14px;
                                          color:#52525b;
                                          line-height:1.7;">
                                  Nie musisz podejmować żadnych
                                  dodatkowych działań.
                                </p>

                                <p style="margin:0;
                                          font-size:13px;
                                          color:#71717a;
                                          line-height:1.7;">
                                  Z poważaniem,<br />
                                  Zespół YourBarbershop
                                </p>

                              </td>
                            </tr>

                            <tr>
                              <td style="background-color:#fafafa;
                                         border-top:1px solid #e4e4e7;
                                         padding:20px 32px;
                                         text-align:center;">

                                <p style="margin:0;
                                          font-size:12px;
                                          color:#71717a;">
                                  © YourBarbershop.
                                  To jest wiadomość automatyczna.
                                </p>
                              </td>
                            </tr>

                          </table>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.formatted(
                safeFirstname,
                safeFormattedAmount,
                safeReservationDescription,
                safeRefundId);
    }

    /**
     * Resolves the recipient's display name.
     *
     * @param event refund notification event
     * @return customer first name or a generic salutation
     */
    private static String customerFirstname(
            RefundSucceededEvent event
    ) {
        String firstname = event.customerFirstname();

        if (firstname == null || firstname.isBlank()) {
            return "Kliencie";
        }

        return firstname;
    }

    /**
     * Formats the refunded monetary amount in Polish notation.
     *
     * @param event refund notification event
     * @return localized amount and currency
     */
    private static String formatAmount(RefundSucceededEvent event) {
        NumberFormat amountFormatter = NumberFormat.getCurrencyInstance(POLISH_LOCALE);

        amountFormatter.setCurrency(Currency.getInstance(
                        event.currency().toUpperCase(Locale.ROOT)));

        return amountFormatter.format(event.amount());
    }

    /**
     * Creates a human-readable reservation reference.
     *
     * @param event refund notification event
     * @return reservation type and identifier
     */
    private static String reservationDescription(
            RefundSucceededEvent event
    ) {
        return switch (event.reservationType()) {
            case REGISTERED -> "Rezerwacja użytkownika #" + event.reservationId();

            case GUEST -> "Rezerwacja gościa #" + event.reservationId();
        };
    }

    /**
     * Escapes customer-provided text before inserting it into HTML.
     *
     * @param value text to escape
     * @return HTML-safe text
     */
    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
