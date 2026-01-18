package pl.barbershopproject.barbershop.integration.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class LoginControllerTest extends BaseIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @DisplayName("Should login successfully, extract token and access secured endpoint with ADMIN role")
    @Test
    void shouldLoginAndAccessSecuredEndpoint() throws Exception {
        // given
        ObjectNode loginData = objectMapper.createObjectNode()
                .put("email", "admin@test.com")
                .put("password", "test123");

        // when
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String token = extractToken(loginResult);

        // then
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").exists());
    }

    @DisplayName("Should fail login with wrong password")
    @Test
    void shouldNotLoginWithWrongPassword() throws Exception {
        // given
        ObjectNode loginData = objectMapper.createObjectNode()
                .put("email", "admin@test.com")
                .put("password", "wrong_password");

        // when then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData))
                )
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Should not allow access to secured endpoint without token")
    @Test
    void shouldDenyAccessWithoutToken() throws Exception {
        // when then
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("Should return forbidden when regular USER tries to delete admin")
    @Test
    void shouldReturnForbiddenWhenNonAdminTriesToDeleteUser() throws Exception {
        // given
        ObjectNode loginData = objectMapper.createObjectNode()
                .put("email", "johndoe@example.com")
                .put("password", "test123");

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isOk())
                .andReturn();

        String token = extractToken(loginResult);

        // when then
        mockMvc.perform(delete("/users/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private String extractToken(MvcResult result) throws Exception {
        String content = result.getResponse().getContentAsString();
        return objectMapper.readTree(content).get("token").toString().replace("\"", "");
    }
}