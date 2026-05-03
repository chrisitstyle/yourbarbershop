package pl.barbershopproject.barbershop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pl.barbershopproject.barbershop.interceptor.RateLimitInterceptor;

/**
 * Configuration class for Spring Web MVC.
 * Responsible for registering custom interceptors in the application context.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    public WebMvcConfig(RateLimitInterceptor rateLimitInterceptor) {
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    /**
     * Registers the {@link RateLimitInterceptor} to apply rate limiting across the application endpoints.
     *
     * @param registry the interceptor registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // add rate limit interceptor to all paths
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/**");
    }
}