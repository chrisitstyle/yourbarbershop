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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("Should return user objects after login and authorization")
    @Test
    void shouldLoginAndReceiveToken() throws Exception {

        String email = "insertemail";
        String password = "insertpassword";

        // tworzenie obiektu JSON
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode loginData = objectMapper.createObjectNode()
                .put("email", email)
                .put("password", password);

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData))
                )
                .andExpect(status().isOk())
                .andReturn();

        String token = loginResult.getResponse().getContentAsString();

        mockMvc.perform(get("/users/get")
                        .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk());


    /*    mockMvc.perform(get("/users/get"))
                .andExpect(status().isUnauthorized()); */
    }
}