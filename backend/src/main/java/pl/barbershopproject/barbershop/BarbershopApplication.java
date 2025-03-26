package pl.barbershopproject.barbershop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class BarbershopApplication {

	public static final String LOCALHOST = "http://localhost:3000";
	public static void main(String[] args) {

		SpringApplication.run(BarbershopApplication.class, args);
	}
	@Bean // bean, zeby to bylo widoczne dla Springa
	public WebMvcConfigurer corsConfigurer() {

		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/login").allowedOrigins(LOCALHOST);
				registry.addMapping("/register").allowedOrigins(LOCALHOST).allowedMethods("GET","POST","PUT","DELETE","OPTIONS");
				registry.addMapping("/forgot-password").allowedOrigins(LOCALHOST);
				registry.addMapping("/reset-password").allowedOrigins(LOCALHOST);
				registry.addMapping("/offers/get").allowedOrigins(LOCALHOST);
				registry.addMapping("/offers/get/**").allowedOrigins(LOCALHOST);
				registry.addMapping("/offers/add").allowedOrigins(LOCALHOST).allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true);
				registry.addMapping("/offers/update/**").allowedOrigins(LOCALHOST).allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true);
				registry.addMapping("/users/update/**").allowedOrigins(LOCALHOST).allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true);
				registry.addMapping("/offers/delete/**").allowedOrigins(LOCALHOST).allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true);
				registry.addMapping("/users/**").allowedOrigins(LOCALHOST).allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true);
				registry.addMapping("/orders/**").allowedOrigins(LOCALHOST).allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true);
				registry.addMapping("/guestorders/add").allowedOrigins(LOCALHOST).allowedMethods("POST","OPTIONS");
				registry.addMapping("/guestorders/**").allowedOrigins(LOCALHOST).allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true);
				registry.addMapping("/send-email").allowedOrigins(LOCALHOST);


			}
		};
	}

}
