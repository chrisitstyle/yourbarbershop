package pl.barbershopproject.barbershop.email.template;

/**
 * Builds plain-text and HTML email content for reservations
 * awaiting an online card payment.
 */
public final class OnlinePaymentPendingEmailTemplate {

    private static final String SUBJECT =
            "Dokończ płatność za swoją wizytę";

    private OnlinePaymentPendingEmailTemplate() {
    }

    public static String subject() {
        return SUBJECT;
    }

    public static String plainText(
            String firstname,
            String formattedVisitDate,
            String offerName,
            String offerCost,
            String paymentLink
    ) {
        return """
                Cześć %s!

                Twoja wizyta w YourBarbershop została zarezerwowana,
                ale płatność online nie została jeszcze zakończona.

                Data wizyty: %s
                Wybrana oferta: %s
                Koszt usługi: %s zł

                Aby dokończyć płatność, skorzystaj z poniższego linku:
                %s

                Link jest czasowy. Jeśli płatność została już wykonana,
                nie musisz podejmować żadnych dodatkowych działań.

                Z poważaniem,
                Zespół YourBarbershop
                """.formatted(
                fallback(firstname, "Kliencie"),
                fallback(formattedVisitDate, "-"),
                fallback(offerName, "-"),
                fallback(offerCost, "-"),
                fallback(paymentLink, "-")
        );
    }

    public static String html(
            String firstname,
            String formattedVisitDate,
            String offerName,
            String offerCost,
            String paymentLink
    ) {
        String safeFirstname =
                escapeHtml(fallback(firstname, "Kliencie"));

        String safeVisitDate =
                escapeHtml(fallback(formattedVisitDate, "-"));

        String safeOfferName =
                escapeHtml(fallback(offerName, "-"));

        String safeOfferCost =
                escapeHtml(fallback(offerCost, "-"));

        String safePaymentLink =
                escapeHtml(fallback(paymentLink, "#"));

        return """
                <!doctype html>
                <html lang="pl">
                  <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>Dokończ płatność za swoją wizytę</title>
                  </head>
                  <body style="margin:0; padding:0; background-color:#f4f4f5; font-family:Arial, Helvetica, sans-serif; color:#18181b;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"
                           style="background-color:#f4f4f5; padding:32px 16px;">
                      <tr>
                        <td align="center">
                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"
                                 style="max-width:560px; background-color:#ffffff; border-radius:16px; overflow:hidden; border:1px solid #e4e4e7;">
                            <tr>
                              <td style="background-color:#18181b; padding:30px 32px; text-align:center;">
                                <h1 style="margin:0; color:#ffffff; font-size:28px; letter-spacing:0.4px;">
                                  YourBarbershop
                                </h1>
                                <p style="margin:8px 0 0; color:#d4d4d8; font-size:14px;">
                                  Dokończ płatność
                                </p>
                              </td>
                            </tr>

                            <tr>
                              <td style="padding:34px 32px;">
                                <h2 style="margin:0 0 14px; color:#18181b; font-size:24px;">
                                  Cześć %s 👋
                                </h2>

                                <p style="margin:0 0 22px; color:#52525b; font-size:15px; line-height:1.6;">
                                  Twoja wizyta została zarezerwowana,
                                  ale płatność online nie została jeszcze zakończona.
                                </p>

                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"
                                       style="border-collapse:separate; border-spacing:0; border:1px solid #e4e4e7; border-radius:12px; overflow:hidden; margin:24px 0;">
                                  <tr>
                                    <td style="padding:14px 18px; background-color:#fafafa; color:#71717a; font-size:13px; border-bottom:1px solid #e4e4e7;">
                                      Data wizyty
                                    </td>
                                    <td style="padding:14px 18px; color:#18181b; font-size:14px; font-weight:600; border-bottom:1px solid #e4e4e7;">
                                      %s
                                    </td>
                                  </tr>
                                  <tr>
                                    <td style="padding:14px 18px; background-color:#fafafa; color:#71717a; font-size:13px; border-bottom:1px solid #e4e4e7;">
                                      Wybrana oferta
                                    </td>
                                    <td style="padding:14px 18px; color:#18181b; font-size:14px; font-weight:600; border-bottom:1px solid #e4e4e7;">
                                      %s
                                    </td>
                                  </tr>
                                  <tr>
                                    <td style="padding:14px 18px; background-color:#fafafa; color:#71717a; font-size:13px;">
                                      Do zapłaty
                                    </td>
                                    <td style="padding:14px 18px; color:#18181b; font-size:14px; font-weight:600;">
                                      %s zł
                                    </td>
                                  </tr>
                                </table>

                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0"
                                       style="margin:28px 0;">
                                  <tr>
                                    <td align="center">
                                      <a href="%s"
                                         style="display:inline-block; background-color:#18181b; color:#ffffff; text-decoration:none; font-size:15px; font-weight:600; padding:14px 26px; border-radius:10px;">
                                        Przejdź do płatności
                                      </a>
                                    </td>
                                  </tr>
                                </table>

                                <p style="margin:0 0 18px; color:#71717a; font-size:13px; line-height:1.6;">
                                  Link jest czasowy. Jeśli płatność została już wykonana,
                                  nie musisz podejmować żadnych dodatkowych działań.
                                </p>

                                <p style="margin:0; color:#71717a; font-size:13px; line-height:1.6;">
                                  Z poważaniem,<br />
                                  Zespół YourBarbershop
                                </p>
                              </td>
                            </tr>

                            <tr>
                              <td style="padding:20px 32px; background-color:#fafafa; border-top:1px solid #e4e4e7; text-align:center;">
                                <p style="margin:0; color:#71717a; font-size:12px;">
                                  © YourBarbershop. To jest wiadomość automatyczna.
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
                safeVisitDate,
                safeOfferName,
                safeOfferCost,
                safePaymentLink
        );
    }

    private static String fallback(
            String value,
            String fallback
    ) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        return value;
    }

    private static String escapeHtml(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
