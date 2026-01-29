package pl.barbershopproject.barbershop.email;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.barbershopproject.barbershop.auth.oauth2.OAuth2LoginSuccessHandler;
import pl.barbershopproject.barbershop.config.JwtAuthFilter;
import pl.barbershopproject.barbershop.config.JwtService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(value = EmailController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailSenderService emailSenderService;

    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;
    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Test
    void sendEmail_ReturnsOk_WhenServiceSucceeds() throws Exception {
        // given
        EmailMessage emailMessage = EmailMessage.builder()
                .to("klient@example.com")
                .subject("Potwierdzenie wizyty")
                .message("Twoja wizyta została potwierdzona.")
                .build();

        Mockito.doNothing().when(emailSenderService).sendEmail(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString()
        );

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/send-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailMessage)))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(emailSenderService).sendEmail(
                "klient@example.com",
                "Potwierdzenie wizyty",
                "Twoja wizyta została potwierdzona."
        );
    }
}
