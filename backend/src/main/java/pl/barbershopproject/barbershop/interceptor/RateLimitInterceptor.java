package pl.barbershopproject.barbershop.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import pl.barbershopproject.barbershop.annotation.RateLimited;

import java.io.IOException;
import java.time.Duration;

/**
 * Interceptor responsible for enforcing rate limits on endpoints annotated with {@link RateLimited}.
 * It utilizes Redis (Valkey) to track the number of requests per IP address.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    public RateLimitInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws IOException {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true; // allow the request to proceed
        }

        RateLimited rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);
        if (rateLimited == null) {
            return true; // allow the request to proceed
        }

        // retrieve the user's IP address (accounting for potential proxies/load balancers)
        String userIP = getClientIp(request);

        // create a unique key for Valkey - "rate_limit:IP:URI"
        String key = "rate_limit:" + userIP + ":" + request.getRequestURI();

        // increment counter in Valkey
        Long countRequests = redisTemplate.opsForValue().increment(key);

        // if this is the first request, set the expiration time (TTL) for the key
        setExpirationOnFirstRequest(key, countRequests, rateLimited);

        // if the limit is exceeded, block the request
        if (isLimitExceeded(countRequests, rateLimited)) {
            blockRequest(response, key, rateLimited);
            return false; // if the limit is exceeded, block the request
        }

        return true; // allow the request to proceed
    }

    private void setExpirationOnFirstRequest(String key, Long countRequests, RateLimited rateLimited) {
        if (countRequests != null && countRequests == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(rateLimited.timeWindowSeconds()));
        }
    }

    private boolean isLimitExceeded(Long countRequests, RateLimited rateLimited) {
        return countRequests != null && countRequests > rateLimited.limit();
    }

    private void blockRequest(HttpServletResponse response, String key, RateLimited rateLimited) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());

        Long expireTime = redisTemplate.getExpire(key);
        long secondsToWait = getSecondsToWait(expireTime, rateLimited);

        response.setHeader("Retry-After", String.valueOf(secondsToWait));

        long minutes = (secondsToWait / 60) + 1;
        String message = String.format("Przekroczono limit zapytań. Spróbuj ponownie za około %d minut.", minutes);

        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }

    private long getSecondsToWait(Long expireTime, RateLimited rateLimited) {
        if (expireTime != null && expireTime > 0) {
            return expireTime;
        }

        return rateLimited.timeWindowSeconds();
    }

    /**
     * Helper method to retrieve the real client IP address, checking the X-Forwarded-For header first.
     *
     * @param request the current HTTP request
     * @return the extracted IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0]; // returns the first IP from the list in case of multiple proxies
    }
}