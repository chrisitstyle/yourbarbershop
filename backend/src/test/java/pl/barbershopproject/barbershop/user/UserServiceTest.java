package pl.barbershopproject.barbershop.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.barbershopproject.barbershop.audit.event.AuditEvent;
import pl.barbershopproject.barbershop.exception.EmailAlreadyExistsException;
import pl.barbershopproject.barbershop.exception.SelfDeletionException;
import pl.barbershopproject.barbershop.security.AuthenticatedUser;
import pl.barbershopproject.barbershop.security.CurrentUserProvider;
import pl.barbershopproject.barbershop.user.dto.UserCreationDTO;
import pl.barbershopproject.barbershop.user.dto.UserDTO;
import pl.barbershopproject.barbershop.user.dto.UserProfileUpdateRequestDTO;
import pl.barbershopproject.barbershop.user.dto.UserResponseDTO;
import pl.barbershopproject.barbershop.utils.testentities.UserTestEntities;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserResponseDTO userResponseDTO;
    private UserCreationDTO userCreationDTO;

    @BeforeEach
    void setUp() {
        userCreationDTO = UserTestEntities.createUserCreationDTO();

        user = UserTestEntities.createUser();
        user.setIdUser(1L);
        user.setPassword("encoded_password");

        userResponseDTO = UserTestEntities.createUserResponseDTO();
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
        verify(eventPublisher, times(1)).publishEvent(any(AuditEvent.class));
    }

    @Test
    void addUser_ThrowsException_WhenUserEmailExists() {
        when(userRepository.existsByEmail(userCreationDTO.email())).thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.addUser(userCreationDTO)
        );

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsersDTO() {
        when(userRepository.findAllWithOrders()).thenReturn(List.of(user));

        List<UserDTO> usersDTOList = userService.getAllUsers();

        assertNotNull(usersDTOList);
        assertEquals(1, usersDTOList.size());
    }

    @Test
    void getUserById_ShouldReturnUserDTO_WhenAuthorized() {
        User adminUser = UserTestEntities.createAdminUser();
        mockCurrentUser(adminUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_ThrowsException_WhenUnauthorized() {
        User regularUser = UserTestEntities.createRegularUser(2L);
        mockCurrentUser(regularUser);

        assertThrows(AccessDeniedException.class, () -> userService.getUserById(1L));
    }

    @Test
    void updateUser_ShouldUpdateFields_WhenValid() {
        UserProfileUpdateRequestDTO updatedUser = UserTestEntities.createUserProfileUpdateRequestDTO();
        mockCurrentUser(user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO result = userService.updateUser(updatedUser, 1L);

        assertNotNull(result);
        assertEquals("Jane", result.firstname());
        assertEquals("Smith", result.lastname());
        assertEquals("jane@smith.com", result.email());

        assertEquals("Jane", user.getFirstname());
        assertEquals("Smith", user.getLastname());
        assertEquals("jane@smith.com", user.getEmail());
        assertEquals("encoded_password", user.getPassword());

        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
        verify(eventPublisher, times(1)).publishEvent(any(AuditEvent.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateUser_ShouldThrowNoSuchElement_WhenUserNotFound() {
        Long nonExistingId = 999L;
        UserProfileUpdateRequestDTO updatedUser = UserTestEntities.createUserProfileUpdateRequestDTO();
        User adminUser = UserTestEntities.createAdminUser();
        mockCurrentUser(adminUser);

        when(userRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.updateUser(updatedUser, nonExistingId));

        verify(userRepository, times(1)).findById(nonExistingId);
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deleteUserById_ShouldThrowException_WhenDeletingSelf() {
        mockCurrentUser(user);

        assertThrows(
                SelfDeletionException.class,
                () -> userService.deleteUserById(1L)
        );

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deleteUserById_ShouldDelete_WhenAdminDeletesOtherUser() {
        User adminUser = UserTestEntities.createAdminUser();
        mockCurrentUser(adminUser);

        when(userRepository.existsById(2L)).thenReturn(true);

        userService.deleteUserById(2L);

        verify(userRepository).deleteById(2L);
        verify(eventPublisher, times(1)).publishEvent(any(AuditEvent.class));
    }

    private void mockCurrentUser(User user) {
        when(currentUserProvider.getCurrentUser())
                .thenReturn(new AuthenticatedUser(
                        user.getIdUser(),
                        user.getRole()));
    }
}