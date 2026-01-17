package pl.barbershopproject.barbershop.user;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.barbershopproject.barbershop.config.JwtAuthFilter;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.exception.EmailAlreadyExistsException;
import pl.barbershopproject.barbershop.exception.SelfDeletionException;
import pl.barbershopproject.barbershop.user.dto.UserCreationDTO;
import pl.barbershopproject.barbershop.user.dto.UserDTO;
import pl.barbershopproject.barbershop.user.dto.UserResponseDTO;
import pl.barbershopproject.barbershop.utils.TestEntities;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@WebMvcTest(UserController.class)
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
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    void addUser_ReturnsCreated() throws Exception {

        UserCreationDTO userCreationDTO = TestEntities.createUserCreationDTO();
        UserResponseDTO userResponseDTO = TestEntities.createUserResponseDTO();


        Mockito.when(userService.addUser(Mockito.any(UserCreationDTO.class))).thenReturn(userResponseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreationDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idUser").value(1L));
    }

    @Test
    void getAllUsers_ReturnsAllUsers() throws Exception {

        UserDTO user = TestEntities.createUserDTO();

        List<UserDTO> usersList = List.of(user);

        Mockito.when(userService.getAllUsers()).thenReturn(usersList);

        mockMvc.perform(MockMvcRequestBuilders.get("/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].idUser").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].userOrders[0].idOrder").value(10L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].userOrders[0].offer.kind").value("test_kind"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].userOrders[0].offer.cost").value(120.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].userOrders[0].status").value("NOWE"));
    }


    @Test
    void getSingleUser_ReturnsUser() throws Exception {

        UserDTO user = TestEntities.createUserDTO();

        Mockito.when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idUser").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userOrders[0].idOrder").value(10L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userOrders[0].offer.kind").value("test_kind"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userOrders[0].offer.cost").value(120.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userOrders[0].status").value("NOWE"));
    }

    @Test
    void updateUser_ReturnsUpdatedUser() throws Exception {

        User updatedUser = TestEntities.createUser();
        User returnedUser = TestEntities.userBuilder()
                .role(Role.ADMIN)
                .build();


        Mockito.when(userService.updateUser(Mockito.any(User.class), Mockito.eq(1L))).thenReturn(returnedUser);

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idUser").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value("ADMIN"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(returnedUser.getEmail()));
    }


    @Test
    void deleteUserById_ReturnsNoContent() throws Exception {
        Mockito.doNothing().when(userService).deleteUserById(4L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/4"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void getSingleUser_ReturnsNotFound_WhenNoSuchElementException() throws Exception {
        Mockito.when(userService.getUserById(99L))
                .thenThrow(new java.util.NoSuchElementException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/users/99"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().string("User not found"));
    }

    @Test
    void getSingleUser_ReturnsBadRequest_WhenIllegalArgumentException() throws Exception {
        Mockito.when(userService.getUserById(98L))
                .thenThrow(new IllegalArgumentException("Illegal argument"));

        mockMvc.perform(MockMvcRequestBuilders.get("/users/98"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Illegal argument"));
    }

    @Test
    void addUser_ReturnsUnprocessableEntity_WhenEmailAlreadyExistsException() throws Exception {
        UserCreationDTO userCreationDTO = TestEntities.createUserCreationDTO();

        Mockito.when(userService.addUser(Mockito.any(UserCreationDTO.class)))
                .thenThrow(new EmailAlreadyExistsException("Email exists"));

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreationDTO)))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableContent())
                .andExpect(MockMvcResultMatchers.content().string("Email exists"));
    }

    @Test
    void deleteUserById_ReturnsForbidden_WhenAccessDeniedException() throws Exception {
        Mockito.doThrow(new AccessDeniedException("Permission denied"))
                .when(userService).deleteUserById(123L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/123"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string("Permission denied"));
    }

    @Test
    void deleteUserById_ReturnsForbidden_WhenSelfDeletionException() throws Exception {
        Mockito.doThrow(new SelfDeletionException("Cannot remove yourself"))
                .when(userService).deleteUserById(321L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/321"))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.content().string("Cannot remove yourself"));
    }
}
