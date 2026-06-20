package pl.barbershopproject.barbershop.auth.otp;

public final class LoginCodeEmailTemplate {

    private LoginCodeEmailTemplate() {
    }

    public static String subject() {
        return "YourBarbershop login code";
    }

    public static String plainText(String code, int expirationMinutes) {
        return """
                Your YourBarbershop login code is: %s

                The code expires in %d minutes.

                If you did not request this code, you can ignore this email.
                """.formatted(code, expirationMinutes);
    }

    public static String html(String code, int expirationMinutes) {
        return """
                <!doctype html>
                <html lang="en">
                  <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>YourBarbershop login code</title>
                  </head>
                  <body style="margin:0; padding:0; background-color:#f4f4f5; font-family:Arial, Helvetica, sans-serif; color:#18181b;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background-color:#f4f4f5; padding:32px 16px;">
                      <tr>
                        <td align="center">
                          <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:520px; background-color:#ffffff; border-radius:16px; overflow:hidden; border:1px solid #e4e4e7;">
                            <tr>
                              <td style="background-color:#18181b; padding:28px 32px; text-align:center;">
                                <h1 style="margin:0; color:#ffffff; font-size:26px; letter-spacing:0.4px;">
                                  YourBarbershop
                                </h1>
                                <p style="margin:8px 0 0; color:#d4d4d8; font-size:14px;">
                                  Secure email login
                                </p>
                              </td>
                            </tr>

                            <tr>
                              <td style="padding:32px;">
                                <h2 style="margin:0 0 12px; color:#18181b; font-size:22px;">
                                  Your login code
                                </h2>

                                <p style="margin:0 0 24px; color:#52525b; font-size:15px; line-height:1.6;">
                                  Use the code below to sign in to your YourBarbershop account.
                                  This code is valid for <strong>%d minutes</strong>.
                                </p>

                                <div style="text-align:center; margin:28px 0;">
                                  <div style="display:inline-block; padding:18px 28px; background-color:#fafafa; border:1px solid #d4d4d8; border-radius:12px;">
                                    <span style="font-size:34px; font-weight:700; letter-spacing:8px; color:#18181b;">
                                      %s
                                    </span>
                                  </div>
                                </div>

                                <p style="margin:0 0 16px; color:#52525b; font-size:14px; line-height:1.6;">
                                  Enter this code in the login form to continue.
                                </p>

                                <p style="margin:0; color:#71717a; font-size:13px; line-height:1.6;">
                                  If you did not request this code, you can safely ignore this email.
                                  Do not share this code with anyone.
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
                """.formatted(expirationMinutes, code);
    }
}
