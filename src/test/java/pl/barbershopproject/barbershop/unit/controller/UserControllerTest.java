package pl.barbershopproject.barbershop.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.barbershopproject.barbershop.controller.UserController;
import pl.barbershopproject.barbershop.model.User;
import pl.barbershopproject.barbershop.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // userzy sa tworzeni w ramach testow
    @Test
    void addUserTest() {
        User user = new User();
        when(userService.addUser(any(User.class))).thenReturn(new ResponseEntity<>(user, HttpStatus.CREATED));

        ResponseEntity<User> responseEntity = userController.addUser(user);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(user, responseEntity.getBody());
    }

    @Test
    void getAllUsersTest() {
        List<User> users = Arrays.asList(new User(), new User()); // lista userow do testow
        when(userService.getAllUsers()).thenReturn(users);

        ResponseEntity<List<User>> responseEntity = userController.getAllUsers();

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(users, responseEntity.getBody());
    }

    @Test
    void getSingleUserTest() {
        long userId = 1L;
        User user = new User();
        when(userService.getSingleUser(userId)).thenReturn(user);

        ResponseEntity<User> responseEntity = userController.getSingleUser(userId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(user, responseEntity.getBody());
    }

    @Test
    void getSingleUserNotFoundTest() {
        long userId = 1L;
        when(userService.getSingleUser(userId)).thenReturn(null);

        ResponseEntity<User> responseEntity = userController.getSingleUser(userId);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    void updateUserTest() {
        long userId = 1L;
        User updatedUser = new User();
        when(userService.updateUser(any(User.class), eq(userId))).thenReturn(updatedUser);

        User result = userController.updateUser(updatedUser, userId);

        assertEquals(updatedUser, result);
        verify(userService, Mockito.times(1)).updateUser(updatedUser, userId);
    }
    @Test
    void deleteUserByIdTest() {
        long userId = 1L;
        doNothing().when(userService).deleteUserById(userId);

        ResponseEntity<String> responseEntity = userController.deleteUserById(userId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Użytkownik o ID " + userId + " został usunięty.", responseEntity.getBody());
    }

    @Test
    void deleteUserByIdNotFoundTest() {
        long userId = 1L;
        doThrow(new NoSuchElementException("User not found")).when(userService).deleteUserById(userId);

        ResponseEntity<String> responseEntity = userController.deleteUserById(userId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("User not found", responseEntity.getBody());
    }


}

