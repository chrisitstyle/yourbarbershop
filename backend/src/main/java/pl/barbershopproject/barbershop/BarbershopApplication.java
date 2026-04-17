package pl.barbershopproject.barbershop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableCaching
public class BarbershopApplication {

	 static void main(String[] args) {

		SpringApplication.run(BarbershopApplication.class, args);
	}


}
