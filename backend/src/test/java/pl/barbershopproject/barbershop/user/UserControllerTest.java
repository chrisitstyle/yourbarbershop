package pl.barbershopproject.barbershop.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pl.barbershopproject.barbershop.config.JwtAuthFilter;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.exception.EmailAlreadyExistsException;
import pl.barbershopproject.barbershop.exception.SelfDeletionException;
import pl.barbershopproject.barbershop.user.dto.CurrentUserResponseDTO;
import pl.barbershopproject.barbershop.user.dto.UserCreationDTO;
import pl.barbershopproject.barbershop.user.dto.UserDTO;
import pl.barbershopproject.barbershop.user.dto.UserResponseDTO;
import pl.barbershopproject.barbershop.utils.testentities.UserTestEntities;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;
    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_EMAIL = "johndoe@example.com";

    @Test
    void addUser_ReturnsCreated() throws Exception {

        UserCreationDTO userCreationDTO = UserTestEntities.createUserCreationDTO();
        UserResponseDTO userResponseDTO = UserTestEntities.createUserResponseDTO();


        Mockito.when(userService.addUser(Mockito.any(UserCreationDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUser").value(1L));
    }

    @DisplayName("Should allow authenticated user to access current user endpoint")
    @Test
    void shouldAllowAuthenticatedUserToAccessCurrentUserEndpoint() throws Exception {
        // given
        CurrentUserResponseDTO currentUser = new CurrentUserResponseDTO(
                1L,
                "John",
                "Doe",
                USER_EMAIL,
                Role.USER
        );

        Mockito.when(userService.getCurrentUser(USER_EMAIL))
                .thenReturn(currentUser);

        // when + then
        mockMvc.perform(MockMvcRequestBuilders.get("/users/me")
                        .principal(authenticationFor(USER_EMAIL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"))
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.role").value("USER"));

        Mockito.verify(userService).getCurrentUser(USER_EMAIL);
    }

    @Test
    void getAllUsers_ReturnsAllUsers() throws Exception {

        UserDTO user = UserTestEntities.createUserDTO();

        List<UserDTO> usersList = List.of(user);

        Mockito.when(userService.getAllUsers()).thenReturn(usersList);

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idUser").value(1L))
                .andExpect(jsonPath("$[0].userOrders[0].idOrder").value(10L))
                .andExpect(jsonPath("$[0].userOrders[0].offer.kind").value("test_kind"))
                .andExpect(jsonPath("$[0].userOrders[0].offer.cost").value(120.0))
                .andExpect(jsonPath("$[0].userOrders[0].status").value("NOWE"));
    }


    @Test
    void getSingleUser_ReturnsUser() throws Exception {

        UserDTO user = UserTestEntities.createUserDTO();

        Mockito.when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUser").value(1L))
                .andExpect(jsonPath("$.userOrders[0].idOrder").value(10L))
                .andExpect(jsonPath("$.userOrders[0].offer.kind").value("test_kind"))
                .andExpect(jsonPath("$.userOrders[0].offer.cost").value(120.0))
                .andExpect(jsonPath("$.userOrders[0].status").value("NOWE"));
    }

    @Test
    void updateUser_ReturnsUpdatedUser() throws Exception {

        User updatedUser = UserTestEntities.createUser();
        User returnedUser = UserTestEntities.userBuilder()
                .role(Role.ADMIN)
                .build();


        Mockito.when(userService.updateUser(Mockito.any(User.class), Mockito.eq(1L))).thenReturn(returnedUser);

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUser").value(1L))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value(returnedUser.getEmail()));
    }


    @Test
    void deleteUserById_ReturnsNoContent() throws Exception {
        Mockito.doNothing().when(userService).deleteUserById(4L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/4"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getSingleUser_ReturnsNotFound_WhenNoSuchElementException() throws Exception {
        Mockito.when(userService.getUserById(99L))
                .thenThrow(new java.util.NoSuchElementException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    void getSingleUser_ReturnsBadRequest_WhenIllegalArgumentException() throws Exception {
        Mockito.when(userService.getUserById(98L))
                .thenThrow(new IllegalArgumentException("Illegal argument"));

        mockMvc.perform(MockMvcRequestBuilders.get("/users/98"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Illegal argument"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    void addUser_ReturnsUnprocessableEntity_WhenEmailAlreadyExistsException() throws Exception {
        UserCreationDTO userCreationDTO = UserTestEntities.createUserCreationDTO();

        Mockito.when(userService.addUser(Mockito.any(UserCreationDTO.class)))
                .thenThrow(new EmailAlreadyExistsException("Email exists"));

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreationDTO)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("Email exists"))
                .andExpect(jsonPath("$.status").value("UNPROCESSABLE_CONTENT"));
    }

    @Test
    void deleteUserById_ReturnsForbidden_WhenAccessDeniedException() throws Exception {
        Mockito.doThrow(new AccessDeniedException("Permission denied"))
                .when(userService).deleteUserById(123L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/123"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Permission denied"))
                .andExpect(jsonPath("$.status").value("FORBIDDEN"));
    }

    @Test
    void deleteUserById_ReturnsForbidden_WhenSelfDeletionException() throws Exception {
        Mockito.doThrow(new SelfDeletionException("Cannot remove yourself"))
                .when(userService).deleteUserById(321L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/321"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Cannot remove yourself"))
                .andExpect(jsonPath("$.status").value("FORBIDDEN"));

    }

    private Authentication authenticationFor(String email) {
        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}