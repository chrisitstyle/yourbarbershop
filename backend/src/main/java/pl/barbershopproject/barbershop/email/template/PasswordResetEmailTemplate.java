package pl.barbershopproject.barbershop.email.template;

public final class PasswordResetEmailTemplate {

    private static final String SUBJECT = "Resetowanie hasła YourBarbershop";
    private PasswordResetEmailTemplate() {
    }

    public static String subject() {
        return SUBJECT;
    }

    public static String plainText(String firstname, String resetUrl, int expirationMinutes) {
        return """
                Cześć %s!

                Otrzymaliśmy prośbę o zresetowanie hasła do Twojego konta YourBarbershop.

                Kliknij poniższy link, aby ustawić nowe hasło:
                %s

                Link wygaśnie za %d minut.

                Jeśli nie prosiłeś o reset hasła, możesz zignorować tę wiadomość.

                Z poważaniem,
                Zespół YourBarbershop
                """.formatted(
                fallback(firstname),
                resetUrl,
                expirationMinutes
        );
    }

    public static String html(String firstname, String resetUrl, int expirationMinutes) {
        String safeFirstname = escapeHtml(fallback(firstname));
        String safeResetUrl = escapeHtml(resetUrl);

        return """
                <!doctype html>
                <html lang="pl">
                  <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>Resetowanie hasła YourBarbershop</title>
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
                                  Resetowanie hasła
                                </p>
                              </td>
                            </tr>

                            <tr>
                              <td style="padding:34px 32px;">
                                <h2 style="margin:0 0 14px; color:#18181b; font-size:24px;">
                                  Cześć %s 👋
                                </h2>

                                <p style="margin:0 0 18px; color:#52525b; font-size:15px; line-height:1.6;">
                                  Otrzymaliśmy prośbę o zresetowanie hasła do Twojego konta YourBarbershop.
                                </p>

                                <p style="margin:0 0 26px; color:#52525b; font-size:15px; line-height:1.6;">
                                  Kliknij przycisk poniżej, aby ustawić nowe hasło.
                                  Link jest ważny przez <strong>%d minut</strong>.
                                </p>

                                <div style="text-align:center; margin:30px 0;">
                                  <a href="%s" style="display:inline-block; background-color:#18181b; color:#ffffff; text-decoration:none; padding:14px 26px; border-radius:10px; font-size:15px; font-weight:700;">
                                    Zresetuj hasło
                                  </a>
                                </div>

                                <p style="margin:0 0 16px; color:#71717a; font-size:13px; line-height:1.6;">
                                  Jeśli przycisk nie działa, skopiuj i wklej poniższy link do przeglądarki:
                                </p>

                                <p style="margin:0 0 22px; color:#52525b; font-size:12px; line-height:1.6; word-break:break-all;">
                                  %s
                                </p>

                                <div style="background-color:#fafafa; border:1px solid #e4e4e7; border-radius:12px; padding:18px 20px; margin:26px 0;">
                                  <p style="margin:0; color:#3f3f46; font-size:14px; line-height:1.6;">
                                    Jeśli nie prosiłeś o reset hasła, możesz bezpiecznie zignorować tę wiadomość.
                                    Nie udostępniaj tego linku nikomu.
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
                expirationMinutes,
                safeResetUrl,
                safeResetUrl
        );
    }

    private static String fallback(String value) {
        if (value == null || value.isBlank()) {
            return "Kliencie";
        }

        return value;
    }

    private static String escapeHtml(String value) {
        if (value == null || value.isBlank()) {
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
