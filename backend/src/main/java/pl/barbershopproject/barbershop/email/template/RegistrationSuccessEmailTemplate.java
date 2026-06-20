package pl.barbershopproject.barbershop.email.template;

public final class RegistrationSuccessEmailTemplate {

    private static final String SUBJECT = "Welcome to YourBarbershop";
    private RegistrationSuccessEmailTemplate() {
    }

    public static String subject() {
        return SUBJECT;
    }

    public static String plainText(String firstname) {
        return """
                Hi %s,

                Your YourBarbershop account has been created successfully.

                You can now log in, book appointments, and manage your visits from your profile.

                If you did not create this account, please ignore this email.

                YourBarbershop Team
                """.formatted(firstname);
    }

    public static String html(String firstname) {
        String safeFirstname = escapeHtml(firstname);

        return """
                <!doctype html>
                <html lang="en">
                  <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>Welcome to YourBarbershop</title>
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
                                  Welcome to your new account
                                </p>
                              </td>
                            </tr>

                            <tr>
                              <td style="padding:34px 32px;">
                                <h2 style="margin:0 0 14px; color:#18181b; font-size:24px;">
                                  Hi %s 👋
                                </h2>

                                <p style="margin:0 0 18px; color:#52525b; font-size:15px; line-height:1.6;">
                                  Your YourBarbershop account has been created successfully.
                                </p>

                                <p style="margin:0 0 24px; color:#52525b; font-size:15px; line-height:1.6;">
                                  You can now log in, book appointments, and manage your visit history directly from your profile.
                                </p>

                                <div style="background-color:#fafafa; border:1px solid #e4e4e7; border-radius:12px; padding:18px 20px; margin:26px 0;">
                                  <p style="margin:0; color:#3f3f46; font-size:14px; line-height:1.6;">
                                    Tip: You can also use the one-time email code login if you prefer signing in without a password.
                                  </p>
                                </div>

                                <p style="margin:0; color:#71717a; font-size:13px; line-height:1.6;">
                                  If you did not create this account, you can safely ignore this email.
                                </p>
                              </td>
                            </tr>

                            <tr>
                              <td style="padding:20px 32px; background-color:#fafafa; border-top:1px solid #e4e4e7; text-align:center;">
                                <p style="margin:0; color:#71717a; font-size:12px;">
                                  © YourBarbershop. This is an automatic message.
                                </p>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.formatted(safeFirstname);
    }

    private static String escapeHtml(String value) {
        if (value == null || value.isBlank()) {
            return "there";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}