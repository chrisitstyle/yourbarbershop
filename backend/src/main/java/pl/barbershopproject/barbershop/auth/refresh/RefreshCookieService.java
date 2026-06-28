package pl.barbershopproject.barbershop.auth.refresh;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RefreshCookieService {

    public static final String REFRESH_COOKIE_NAME = "refresh_token";

    @Value("${application.security.refresh-token-days:14}")
    private long refreshTokenDays;

    @Value("${application.security.refresh-cookie-secure:true}")
    private boolean secureCookie;

    @Value("${application.security.refresh-cookie-same-site:None}")
    private String sameSite;

    public void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/auth")
                .maxAge(Duration.ofDays(refreshTokenDays))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/auth")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
