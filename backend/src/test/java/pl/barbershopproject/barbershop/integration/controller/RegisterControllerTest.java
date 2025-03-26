package pl.barbershopproject.barbershop.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("Should ")
    @Test
    @Transactional
    void shouldRegisterUser() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode userData = objectMapper.createObjectNode()
                .put("firstname", "Test")
                .put("lastname", "Test")
                .put("email", "testuser@example.com")
                .put("password", "testPassword123")
                .put("role", "USER");

        MvcResult registerResult = mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userData))
                )
                .andExpect(status().isOk())
                .andReturn();

        String token = registerResult.getResponse().getContentAsString();
        System.out.println(token);

    }
}