package pl.barbershopproject.barbershop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation used to apply rate limiting to specific controller methods.
 * It defines the maximum number of requests a single IP address can make
 * within a specified time window.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {

    /**
     * Maximum number of allowed requests.
     *
     * @return the request limit
     */
    int limit() default 5;

    /**
     * Time window in seconds during which the limit applies
     * (e.g., 5 requests per 60 seconds).
     *
     * @return the time window in seconds
     */
    int timeWindowSeconds() default 60;
}
