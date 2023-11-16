package pl.barbershopproject.barbershop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pl.barbershopproject.barbershop.repository.UserRepository;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {

    private final UserRepository userRepository;
    @Bean
    public UserDetailsService userDetailsService(){
        return userName -> userRepository
                .findByEmail(userName)
                .orElseThrow(() ->
                        new UsernameNotFoundException(String.format("User '%s' not found!", userName)));
    }
    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider daoAuthProvider = new DaoAuthenticationProvider();
        daoAuthProvider.setUserDetailsService(userDetailsService());
        daoAuthProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
    throws Exception{
        return config.getAuthenticationManager();
    }
}
