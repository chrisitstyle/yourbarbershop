package pl.barbershopproject.barbershop.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
@Service
public class JwtService {
    private static final String SECRET_KEY="556A586E3272357538782F413F4428472D4B6150645367566B59703373367639";
    private SecretKey getSignKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }
    public String generateToken(UserDetails userDetails){
        return generateToken(Map.of(), userDetails);
    }
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails){
        final Instant now = Instant.now();
        final Instant expiry = now.plus(8, ChronoUnit.HOURS);
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    public String extractUserName(String token){
        return extractClaim(token, Claims::getSubject);
    }
    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }
    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(Date.from(Instant.now()));
    }
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String userName = extractUserName(token);
        return userName.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
