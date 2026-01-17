package pl.barbershopproject.barbershop;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.barbershopproject.barbershop.config.JwtService;

class BarbershopApplicationTests {

    @MockitoBean
    private JwtService jwtService;

    @Test
    void contextLoads() {}
}
