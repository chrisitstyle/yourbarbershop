package pl.barbershopproject.barbershop.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.barbershopproject.barbershop.auth.AuthRequest;
import pl.barbershopproject.barbershop.auth.AuthResponse;
import pl.barbershopproject.barbershop.auth.AuthService;
import pl.barbershopproject.barbershop.integration.controller.TestConfig;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.model.Role;
import pl.barbershopproject.barbershop.model.User;
import pl.barbershopproject.barbershop.validation.ValidationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestConfig.class)
class AuthControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthControllerTest.class);

    @MockBean
    private AuthService authService;

    @MockBean
    private ValidationService<User> validator;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtService jwtService;

    @Test
    void validUserLogin() throws Exception {
        AuthRequest authRequest = new AuthRequest("test@example.com", "test123");

        when(authService.authenticate(any(AuthRequest.class)))
                .thenReturn(new AuthResponse("testToken", 123L, Role.USER));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andDo(result -> logger.info(result.getResponse().getContentAsString()));
    }


    @Test
    void validUserRegister() throws Exception {
        //  User user = new User(1,"Marcin","Kowalski","markow111@wp.pl","testowy123",Role.USER);
        doNothing().when(validator).validate(any(User.class));


        when(authService.register(any(User.class))).thenReturn(new AuthResponse("testToken", 123L, Role.USER));

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"id\": \"60\", \"firstname\": \"John\",\"lastname\": \"Doe\", \"email\": \"johndoe@gmail.com\",\"password\": \"test123\",\"role\": \"USER\" }"))
                .andExpect(status().isOk())
                .andDo(result -> logger.info(result.getResponse().getContentAsString()));
    }





}