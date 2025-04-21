package pl.barbershopproject.barbershop.user;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.barbershopproject.barbershop.exception.EmailAlreadyExistsException;
import pl.barbershopproject.barbershop.exception.SelfDeletionException;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import pl.barbershopproject.barbershop.user.UserService;
import pl.barbershopproject.barbershop.user.dto.UserCreationDTO;
import pl.barbershopproject.barbershop.user.dto.UserDTO;
import pl.barbershopproject.barbershop.user.dto.UserResponseDTO;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;


    private User user;
    private UserResponseDTO userResponseDTO;
    private UserCreationDTO userCreationDTO;

    @BeforeEach
    void setUp() {

        userCreationDTO = new UserCreationDTO(
                "john@doe.com",
                "test_password",
                "John",
                "Doe",
                "USER"
        );
        user = User.builder()
                .idUser(1L)
                .email("john@doe.com")
                .password("encoded_password")
                .firstname("John")
                .lastname("Doe")
                .role(Role.USER)
                .userOrders(List.of(new Order()))
                .build();

        userResponseDTO = new UserResponseDTO(
                1L,
                "John",
                "Doe",
                "john@doe.com",
                Role.USER
        );


        SecurityContextHolder.setContext(securityContext);

    }

    @Test
    void addUser_ReturnsUserResponseDTO_WhenSuccessful() {

        when(userRepository.existsByEmail(userCreationDTO.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponseDTO result = userService.addUser(userCreationDTO);

        assertNotNull(result);
        assertEquals(userResponseDTO.email(), result.email());
        verify(userRepository).existsByEmail(userCreationDTO.email());
    }

    @Test
    void addUser_ThrowsException_WhenUserEmailExists() {

        when(userRepository.existsByEmail(userCreationDTO.email())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.addUser(userCreationDTO));
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsersDTO() {

        when(userRepository.findAllWithOrders()).thenReturn(List.of(user));

        List<UserDTO> usersDTOList = userService.getAllUsers();

        assertNotNull(usersDTOList);
        assertEquals(1, usersDTOList.size());

    }

    @Test
    void getUserById_ShouldReturnUserDTO_WhenAuthorized(){
        User adminUser = createAdminUser();
        mockSecurityContext(adminUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_ThrowsException_WhenUnauthorized(){
        User regularUser = createRegularUser(2L);
        mockSecurityContext(regularUser);

        assertThrows(AccessDeniedException.class, () -> userService.getUserById(1L));

    }

    @Test
    void updateUser_ShouldUpdateFields_WhenValid() {
        User updatedUser = createUpdatedUser();
        mockSecurityContext(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("new_encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(updatedUser, 1L);

        assertEquals("Jane", result.getFirstname());
        assertEquals("new_encoded_password", result.getPassword());
        verify(passwordEncoder).encode("new_password");
    }

    @Test
    void updateUser_ShouldThrowNoSuchElement_WhenUserNotFound() {

        long nonExistingId = 999L;
        User updatedUser = createUpdatedUser();
        User adminUser = createAdminUser();
        mockSecurityContext(adminUser);

        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());


        assertThrows(NoSuchElementException.class, () -> {
            userService.updateUser(updatedUser, nonExistingId);
        });

        verify(userRepository, times(1)).findById(nonExistingId);
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void deleteUserById_ShouldThrowException_WhenDeletingSelf() {
        mockSecurityContext(user);

        assertThrows(SelfDeletionException.class,
                () -> userService.deleteUserById(1L));
    }

    @Test
    void deleteUserById_ShouldDelete_WhenAdminDeletesOtherUser() {
        User adminUser = createAdminUser();
        mockSecurityContext(adminUser);

        when(userRepository.existsById(2L)).thenReturn(true);

        userService.deleteUserById(2L);

        verify(userRepository).deleteById(2L);
    }




    private User createAdminUser() {
        return User.builder()
                .idUser(99L)
                .role(Role.ADMIN)
                .build();
    }

    private User createRegularUser(Long id) {
        return User.builder()
                .idUser(id)
                .role(Role.USER)
                .build();
    }

    private User createUpdatedUser() {
        return User.builder()
                .firstname("Jane")
                .lastname("Smith")
                .email("jane@smith.com")
                .password("new_password")
                .build();
    }

    private void mockSecurityContext(User user) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
    }

}

