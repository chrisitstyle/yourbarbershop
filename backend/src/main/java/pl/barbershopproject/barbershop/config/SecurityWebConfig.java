package pl.barbershopproject.barbershop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pl.barbershopproject.barbershop.auth.oauth2.OAuth2LoginSuccessHandler;
import pl.barbershopproject.barbershop.user.Role;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityWebConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                                .requestMatchers("/register", "/login", "/forgot-password", "/reset-password**", "/oauth2/**", "/login/oauth2/**").permitAll()
                                // Users endpoints
                                .requestMatchers(HttpMethod.POST, "/users").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.GET, "/users").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.GET, "/users/**").authenticated()
                                .requestMatchers(HttpMethod.PUT, "/users/**").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.DELETE, "/users/**").hasAuthority(Role.ADMIN.toString())
                                // Offers endpoints
                                .requestMatchers(HttpMethod.POST, "/offers").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.GET, "/offers", "/offers/**").permitAll()
                                .requestMatchers(HttpMethod.PUT, "/offers/**").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.DELETE, "/offers/**").hasAuthority(Role.ADMIN.toString())
                                // Orders endpoints
                                .requestMatchers(HttpMethod.POST, "/orders").authenticated()
                                .requestMatchers(HttpMethod.GET, "/orders", "/orders/**").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.PUT, "/orders/**").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.DELETE, "/orders/**").hasAuthority(Role.ADMIN.toString())
                                // Guestorders endpoints
                                .requestMatchers(HttpMethod.POST, "/guestorders").permitAll()
                                .requestMatchers(HttpMethod.GET, "/guestorders", "/guestorders/**").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.PUT, "/guestorders/**").hasAuthority(Role.ADMIN.toString())
                                .requestMatchers(HttpMethod.DELETE, "/guestorders/**").hasAuthority(Role.ADMIN.toString())
                                // Email endpoints
                                .requestMatchers("/send-email").authenticated()

                                .anyRequest().authenticated()
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .failureUrl("http://localhost:3000/login?error=social_login_failed")

                        .successHandler(oAuth2LoginSuccessHandler))

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return http.build();
    }
}
