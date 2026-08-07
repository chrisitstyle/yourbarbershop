package pl.barbershopproject.barbershop.integration.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class RegisterControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;


    @DisplayName("Should register new user and return orderStatus OK")
    @Test
    void shouldRegisterUser() throws Exception {
        // given
        JsonNode userData = objectMapper.createObjectNode()
                .put("firstname", "New")
                .put("lastname", "User")
                .put("email", "newuser@example.com")
                .put("password", "test_password")
                .put("role", "USER")
                .put("captchaToken", "test-token-value");

        // when then
        MvcResult registerResult = mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andReturn();

        //then
        String responseContent = registerResult.getResponse().getContentAsString();
        System.out.println("Register response: " + responseContent);

        assertNotNull(responseContent);
        assertFalse(responseContent.isEmpty(), "Response should not be empty");

    }
}