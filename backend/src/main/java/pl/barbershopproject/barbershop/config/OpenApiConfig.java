package pl.barbershopproject.barbershop.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // global security requirement for all endpoints
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                // definition of the security scheme (JWT)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")))
                // API general information
                .info(new Info()
                        .title("YourBarbershop API")
                        .version("1.0")
                        .description("Dokumentacja API dla systemu rezerwacji salonu barbershop")
                        .contact(new Contact()
                                .name("Chris (@chrisitstyle)")
                                .url("https://github.com/chrisitstyle")));
    }
}
