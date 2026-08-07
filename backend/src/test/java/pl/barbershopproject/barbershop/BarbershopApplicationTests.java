package pl.barbershopproject.barbershop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

class BarbershopApplicationTests extends BaseIntegrationTest {

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }
}