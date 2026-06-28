package pl.barbershopproject.barbershop.email.template;

public final class OrderConfirmationEmailTemplate {

    private static final String SUBJECT = "Potwierdzenie rezerwacji wizyty";

    private OrderConfirmationEmailTemplate() {
    }

    public static String subject() {
        return SUBJECT;
    }

    public static String plainText(
            String firstname,
            String formattedVisitDate,
            String offerKind,
            String offerCost,
            String paymentMethod,
            String paymentStatus
    ) {
        return """
                Cześć %s!

                Dziękujemy za umówienie wizyty w naszym salonie YourBarbershop.

                Data wizyty: %s.
                Wybrana oferta: %s
                Koszt usługi: %s zł.
                Metoda płatności: %s
                Status płatności: %s

                Zapraszamy w uzgodnionym terminie do nas!

                Z poważaniem,
                Zespół YourBarbershop
                """.formatted(
                fallback(firstname, "Kliencie"),
                fallback(formattedVisitDate, "-"),
                fallback(offerKind, "-"),
                fallback(offerCost, "-"),
                formatPaymentMethod(paymentMethod),
                formatPaymentStatus(paymentStatus)
        );
    }

    public static String html(
            String firstname,
            String formattedVisitDate,
            String offerKind,
            String offerCost,
            String paymentMethod,
            String paymentStatus
    ) {
        String safeFirstname = escapeHtml(fallback(firstname, "Kliencie"));
        String safeVisitDate = escapeHtml(fallback(formattedVisitDate, "-"));
        String safeOfferKind = escapeHtml(fallback(offerKind, "-"));
        String safeOfferCost = escapeHtml(fallback(offerCost, "-"));
        String safePaymentMethod = escapeHtml(formatPaymentMethod(paymentMethod));
        String safePaymentStatus = escapeHtml(formatPaymentStatus(paymentStatus));

        return """
                <!doctype html>
                <html lang="pl">
                  <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>Potwierdzenie rezerwacji wizyty</title>
                  </head>
                  <body style="margin:0; padding:0; background-color:#f4f4f5; font-family:Arial, Helvetica, sans-serif; color:#18181b;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f5; padding:32px 16px;">
                      <tr>
                        <td align="center">
                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:560px; background-color:#ffffff; border-radius:16px; overflow:hidden; border:1px solid #e4e4e7;">
                            <tr>
                              <td style="background-color:#18181b; padding:30px 32px; text-align:center;">
                                <h1 style="margin:0; color:#ffffff; font-size:28px; letter-spacing:0.4px;">
                                  YourBarbershop
                                </h1>
                                <p style="margin:8px 0 0; color:#d4d4d8; font-size:14px;">
                                  Potwierdzenie rezerwacji
                                </p>
                              </td>
                            </tr>

                            <tr>
                              <td style="padding:34px 32px;">
                                <h2 style="margin:0 0 14px; color:#18181b; font-size:24px;">
                                  Cześć %s 👋
                                </h2>

                                <p style="margin:0 0 22px; color:#52525b; font-size:15px; line-height:1.6;">
                                  Dziękujemy za umówienie wizyty w naszym salonie YourBarbershop.
                                  Poniżej znajdziesz szczegóły swojej rezerwacji.
                                </p>

                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="border-collapse:separate; border-spacing:0; border:1px solid #e4e4e7; border-radius:12px; overflow:hidden; margin:24px 0;">
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
                                    <td style="padding:14px 18px; background-color:#fafafa; color:#71717a; font-size:13px; border-bottom:1px solid #e4e4e7;">
                                      Koszt usługi
                                    </td>
                                    <td style="padding:14px 18px; color:#18181b; font-size:14px; font-weight:600; border-bottom:1px solid #e4e4e7;">
                                      %s zł
                                    </td>
                                  </tr>
                                  <tr>
                                    <td style="padding:14px 18px; background-color:#fafafa; color:#71717a; font-size:13px; border-bottom:1px solid #e4e4e7;">
                                      Metoda płatności
                                    </td>
                                    <td style="padding:14px 18px; color:#18181b; font-size:14px; font-weight:600; border-bottom:1px solid #e4e4e7;">
                                      %s
                                    </td>
                                  </tr>
                                  <tr>
                                    <td style="padding:14px 18px; background-color:#fafafa; color:#71717a; font-size:13px;">
                                      Status płatności
                                    </td>
                                    <td style="padding:14px 18px; color:#18181b; font-size:14px; font-weight:600;">
                                      %s
                                    </td>
                                  </tr>
                                </table>

                                <div style="background-color:#fafafa; border:1px solid #e4e4e7; border-radius:12px; padding:18px 20px; margin:26px 0;">
                                  <p style="margin:0; color:#3f3f46; font-size:14px; line-height:1.6;">
                                    Zapraszamy w uzgodnionym terminie. Prosimy o przybycie kilka minut wcześniej.
                                  </p>
                                </div>

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
                safeOfferKind,
                safeOfferCost,
                safePaymentMethod,
                safePaymentStatus
        );
    }

    private static String formatPaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return "-";
        }

        return switch (paymentMethod) {
            case "GOTOWKA" -> "Gotówka na miejscu";
            case "KARTA_ONLINE" -> "Karta online";
            case "KARTA_NA_MIEJSCU" -> "Karta na miejscu";
            default -> paymentMethod;
        };
    }

    private static String formatPaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.isBlank()) {
            return "-";
        }

        return switch (paymentStatus) {
            case "NIE_WYMAGANA" -> "Niewymagana";
            case "OCZEKUJE_NA_PLATNOSC" -> "Oczekuje na płatność";
            case "OPLACONA" -> "Opłacona";
            case "NIEUDANA" -> "Nieudana";
            case "WYGASLA" -> "Wygasła";
            case "ZWROCONA" -> "Zwrócona";
            default -> paymentStatus;
        };
    }

    private static String fallback(String value, String fallback) {
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