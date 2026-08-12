package pl.barbershopproject.barbershop.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pl.barbershopproject.barbershop.security.UserPrincipal;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;

    @Value("${JWT_EXPIRATION_HOURS:8}")
    private long expirationHours;

    @Value("${JWT_ACCESS_EXPIRATION_MINUTES:15}")
    private long accessExpirationMinutes;

    public String generateAccessToken(UserPrincipal principal) {
        Map<String, Object> extraClaims = new HashMap<>();

        extraClaims.put("role", principal.role().toString());
        extraClaims.put("id", principal.userId());

        return generateAccessToken(extraClaims, principal);
    }

    public String generateAccessToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        final Instant now = Instant.now();
        final Instant expiry = now.plus(accessExpirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSignKey(), Jwts.SIG.HS256)
                .compact();
    }


    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    @Deprecated
    public String generateToken(UserPrincipal principal) {
        Map<String, Object> extraClaims = new HashMap<>();

        extraClaims.put("role", principal.role().toString());
        extraClaims.put("id", principal.userId());

        return generateToken(extraClaims, principal);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        final Instant now = Instant.now();
        final Instant expiry = now.plus(expirationHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSignKey(), Jwts.SIG.HS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(Date.from(Instant.now()));
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}