package pl.barbershopproject.barbershop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.barbershopproject.barbershop.user.Role;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityWebConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                                .requestMatchers("/register", "/login", "/forgot-password", "/reset-password**").permitAll()
                                // Users endpoints
                                .requestMatchers(HttpMethod.POST,"/users").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.GET,"/users").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.GET,"/users/{idUser}").authenticated()
                                .requestMatchers(HttpMethod.PUT,"/users/{idUser}").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.DELETE,"/users/{idUser}").hasAuthority(Role.ADMIN.toString())
                                // Offers endpoints
                                .requestMatchers(HttpMethod.POST,"/offers").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.GET,"/offers","/offers/{idOffer}").permitAll()
                                .requestMatchers(HttpMethod.PUT,"/offers/{idOffer}").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.DELETE,"/offers/{idOffer}").hasAuthority(Role.ADMIN.toString())
                                // Orders endpoints
                                .requestMatchers(HttpMethod.POST,"/orders").authenticated()
                                .requestMatchers(HttpMethod.GET,"/orders", "/orders/{idOrder}").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.PUT,"/orders/{idOrder}").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.DELETE,"/orders/{idOrder}").hasAuthority(Role.ADMIN.toString())
                                // Guestorders endpoints
                                .requestMatchers(HttpMethod.POST,"/guestorders").permitAll()
                                .requestMatchers(HttpMethod.GET,"/guestorders","/guestorders{idGuestOrder}").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.PUT,"/guestorders/{idGuestOrder}").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.DELETE,"/guestorders/{idGuestOrder}").hasAuthority(Role.ADMIN.toString())
                                // Email endpoints
                                .requestMatchers("/send-email").hasAuthority(Role.ADMIN.toString())

                                .anyRequest().authenticated()
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
