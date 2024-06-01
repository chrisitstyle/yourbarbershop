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
import pl.barbershopproject.barbershop.model.Role;

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
                                .requestMatchers("/register", "/login").permitAll()
                                .requestMatchers("/users/get").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers("/users/get/**").authenticated()
                                .requestMatchers("/users/update/**").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers("/offers/get","/offers/get/**").permitAll()
                                .requestMatchers("/offers/add").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers("/offers/update/**").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers("/offers/delete/**").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers("/orders/add").authenticated()
                                .requestMatchers("/orders/**").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers("/guestorders/add").permitAll()
                                .requestMatchers("/guestorders/**").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers("/send-email").permitAll()

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
