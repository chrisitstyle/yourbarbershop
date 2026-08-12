package pl.barbershopproject.barbershop.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pl.barbershopproject.barbershop.config.JwtAuthFilter;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.exception.EmailAlreadyExistsException;
import pl.barbershopproject.barbershop.exception.SelfDeletionException;
import pl.barbershopproject.barbershop.user.dto.*;
import pl.barbershopproject.barbershop.utils.testentities.UserTestEntities;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private static final String USER_EMAIL = "johndoe@example.com";
    private static final ZoneId TEST_ZONE = ZoneId.of("Europe/Warsaw");
    private static final Instant TEST_INSTANT = Instant.parse("2026-01-16T12:00:00Z");

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

    @MockitoBean
    private Clock clock;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        lenient().when(clock.getZone()).thenReturn(TEST_ZONE);
        lenient().when(clock.instant()).thenReturn(TEST_INSTANT);
    }

    @Test
    void addUser_ReturnsCreated() throws Exception {

        UserCreationDTO userCreationDTO = UserTestEntities.createUserCreationDTO();
        UserResponseDTO userResponseDTO = UserTestEntities.createUserResponseDTO();


        when(userService.addUser(any(UserCreationDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUser").value(1L));
    }

    @DisplayName("Should return current user")
    @Test
    void shouldReturnCurrentUser() throws Exception {
        // given
        CurrentUserResponseDTO currentUser = new CurrentUserResponseDTO(
                1L,
                "John",
                "Doe",
                USER_EMAIL,
                Role.USER);

        when(userService.getCurrentUser())
                .thenReturn(currentUser);

        // when, then
        mockMvc.perform(MockMvcRequestBuilders.get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"))
                .andExpect(jsonPath("$.email").value(USER_EMAIL))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getCurrentUser();
    }

    @Test
    void getAllUsers_ReturnsAllUsers() throws Exception {

        UserDTO user = UserTestEntities.createUserDTO();

        List<UserDTO> usersList = List.of(user);

        when(userService.getAllUsers()).thenReturn(usersList);

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idUser").value(1L))
                .andExpect(jsonPath("$[0].userOrders[0].idOrder").value(10L))
                .andExpect(jsonPath("$[0].userOrders[0].offer.kind").value("test_kind"))
                .andExpect(jsonPath("$[0].userOrders[0].offer.cost").value(120.0))
                .andExpect(jsonPath("$[0].userOrders[0].orderStatus").value("NOWE"));
    }


    @Test
    void getSingleUser_ReturnsUser() throws Exception {

        UserDTO user = UserTestEntities.createUserDTO();

        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUser").value(1L))
                .andExpect(jsonPath("$.userOrders[0].idOrder").value(10L))
                .andExpect(jsonPath("$.userOrders[0].offer.kind").value("test_kind"))
                .andExpect(jsonPath("$.userOrders[0].offer.cost").value(120.0))
                .andExpect(jsonPath("$.userOrders[0].orderStatus").value("NOWE"));
    }

    @Test
    void updateUser_ReturnsUpdatedUser() throws Exception {

        UserProfileUpdateRequestDTO updatedUser = new UserProfileUpdateRequestDTO(
                "John",
                "Doe",
                USER_EMAIL
        );

        UserDTO returnedUser = new UserDTO(
                1L,
                "John",
                "Doe",
                USER_EMAIL,
                Role.ADMIN,
                List.of()
        );


        when(userService.updateUser(any(UserProfileUpdateRequestDTO.class), eq(1L)))
                .thenReturn(returnedUser);

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUser").value(1L))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value(returnedUser.email()));
    }


    @Test
    void deleteUserById_ReturnsNoContent() throws Exception {
        doNothing().when(userService).deleteUserById(4L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/4"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getSingleUser_ReturnsNotFound_WhenNoSuchElementException() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new java.util.NoSuchElementException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    void getSingleUser_ReturnsBadRequest_WhenIllegalArgumentException() throws Exception {
        when(userService.getUserById(98L))
                .thenThrow(new IllegalArgumentException("Illegal argument"));

        mockMvc.perform(MockMvcRequestBuilders.get("/users/98"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Illegal argument"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @Test
    void addUser_ReturnsUnprocessableEntity_WhenEmailAlreadyExistsException() throws Exception {
        UserCreationDTO userCreationDTO = UserTestEntities.createUserCreationDTO();

        when(userService.addUser(any(UserCreationDTO.class)))
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
        doThrow(new AccessDeniedException("Permission denied"))
                .when(userService).deleteUserById(123L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/123"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Permission denied"))
                .andExpect(jsonPath("$.status").value("FORBIDDEN"));
    }

    @Test
    void deleteUserById_ReturnsForbidden_WhenSelfDeletionException() throws Exception {
        doThrow(new SelfDeletionException("Cannot remove yourself"))
                .when(userService).deleteUserById(321L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/321"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Cannot remove yourself"))
                .andExpect(jsonPath("$.status").value("FORBIDDEN"));

    }
}