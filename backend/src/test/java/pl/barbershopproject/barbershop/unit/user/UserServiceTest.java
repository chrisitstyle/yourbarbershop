package pl.barbershopproject.barbershop.unit.user;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import pl.barbershopproject.barbershop.user.UserService;

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

    @InjectMocks
    private UserService userService;


    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setIdUser(1L);
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setEmail("john@doe.com");
        user.setPassword("test_password");
        user.setRole(Role.USER);

        SecurityContextHolder.setContext(securityContext);

    }

    @Test
    void UserService_AddUser_ReturnsSavedUser() {
        //given
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);
        //when
        User savedUser = userService.addUser(user).getBody();
        //then
        Assertions.assertThat(savedUser).isNotNull();
        Assertions.assertThat(savedUser.getIdUser()).isEqualTo(1L);
    }

    @Test
    void UserService_AddUser_ThrowsExceptionWhenUserEmailExists() {

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.addUser(user));

        Assertions.assertThat(exception.getStatusCode().value()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    void UserService_GetSingleUser_ShouldReturnUser_whenUserIsSameId() {

        User user2 = new User();
        user2.setIdUser(2L);
        user2.setRole(Role.USER);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user2);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));

        User result = userService.getSingleUser(2L);

        assertNotNull(result);
        assertEquals(2L, result.getIdUser());

    }

    @Test
    void UserService_GetSingleUser_ShouldReturnUser_whenUserIsAdmin() {

        User adminUser = new User();
        adminUser.setIdUser(2L);
        adminUser.setRole(Role.ADMIN);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User userFromDB = userService.getSingleUser(1L);

        assertNotNull(userFromDB);
        assertEquals(1L, userFromDB.getIdUser());
        assertEquals(Role.ADMIN, adminUser.getRole());

    }

    @Test
    void UserService_GetSingleUser_ShouldReturnNull_whenUserHasDifferentIdAndNotAdmin() {

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);

        User result = userService.getSingleUser(4L);

        assertNotEquals(4L, user.getIdUser());
        assertNotEquals(Role.ADMIN, user.getRole());
        assertNull(result);

    }

    @Test
    void UserService_GetSingleUser_ShouldThrowException_whenUserNotFound() {

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());


        assertThrows(NoSuchElementException.class, () -> userService.getSingleUser(1L));

    }

    @Test
    void UserService_GetAllUsers_ShouldReturnListOfUsers() {

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> usersList = userService.getAllUsers();

        assertNotNull(usersList);
        assertEquals(1, usersList.size());

    }

    @Test
    void UserService_UpdateUser_ShouldUpdateUser_WhenUserExistsAndPasswordIsProvided() {

        User updatedUser = new User();
        updatedUser.setFirstname("Jane");
        updatedUser.setLastname("Smith");
        updatedUser.setEmail("jane.smith@example.com");
        updatedUser.setRole(Role.ADMIN);
        updatedUser.setPassword("newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.updateUser(updatedUser, 1L);


        assertNotNull(savedUser);
        assertEquals("Jane", savedUser.getFirstname());
        assertEquals("Smith", savedUser.getLastname());
        assertEquals("jane.smith@example.com", savedUser.getEmail());
        assertEquals(Role.ADMIN, savedUser.getRole());
        assertEquals("newPassword", savedUser.getPassword());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
   void UserService_UpdateUser_ShouldUpdateUserExcludingPassword_WhenUserExistsAndPasswordIsNull() {
        User updatedUser = new User();
        updatedUser.setFirstname("Jane");
        updatedUser.setLastname("Smith");
        updatedUser.setEmail("jane.smith@example.com");
        updatedUser.setRole(Role.ADMIN);
        updatedUser.setPassword(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.updateUser(updatedUser, 1L);

        assertNotNull(savedUser);
        assertEquals("Jane", savedUser.getFirstname());
        assertEquals("Smith", savedUser.getLastname());
        assertEquals("jane.smith@example.com", savedUser.getEmail());
        assertEquals("test_password", savedUser.getPassword());
        assertEquals(Role.ADMIN, savedUser.getRole());
        
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }


    @Test
   void UserService_UpdateUser_ShouldUpdateUserExcludingPassword_WhenUserExistsAndPasswordIsEmpty() {
        User updatedUser = new User();
        updatedUser.setFirstname("Jane");
        updatedUser.setLastname("Smith");
        updatedUser.setEmail("jane.smith@example.com");
        updatedUser.setRole(Role.ADMIN);
        updatedUser.setPassword(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.updateUser(updatedUser, 1L);

        assertNotNull(savedUser);
        assertEquals("Jane", savedUser.getFirstname());
        assertEquals("Smith", savedUser.getLastname());
        assertEquals("jane.smith@example.com", savedUser.getEmail());
        assertEquals(Role.ADMIN, savedUser.getRole());
        assertEquals("test_password", savedUser.getPassword());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void UserService_UpdateUser_ShouldThrowException_WhenUserDoesNotExist() {

        User updatedUser = new User();
        updatedUser.setFirstname("Jane");
        updatedUser.setLastname("Smith");
        updatedUser.setEmail("jane.smith@example.com");
        updatedUser.setRole(Role.ADMIN);
        updatedUser.setPassword(null);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.updateUser(updatedUser, 1L));

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void UserService_DeleteUserById_ShouldDeleteUser_WhenUserExistsAndNotLoggedInUser(){
        User adminUser = new User();
        adminUser.setIdUser(1L);
        adminUser.setRole(Role.ADMIN);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        userService.deleteUserById(2L);

        verify(userRepository, times(1)).deleteById(2L);

    }

    @Test
    void UserService_DeleteUserById_ShouldThrowException_WhenUserExistsAndIsLoggedInUser() {
        User adminUser = new User();
        adminUser.setIdUser(1L);
        adminUser.setRole(Role.ADMIN);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUserById(1L));

        verify(userRepository, never()).deleteById(1L);
    }

    @Test
    void UserService_DeleteUserById_ShouldThrowException_WhenUserDoesNotExist() {
        User adminUser = new User();
        adminUser.setIdUser(1L);
        adminUser.setRole(Role.ADMIN);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(adminUser);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.deleteUserById(2L));

        verify(userRepository, never()).deleteById(2L);
    }

}

