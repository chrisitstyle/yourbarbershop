package pl.barbershopproject.barbershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pl.barbershopproject.barbershop.model.User;
import pl.barbershopproject.barbershop.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/add")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @GetMapping("/get")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> usersResponse = userService.getAllUsers();
        return ResponseEntity.ok(usersResponse);
    }

    @GetMapping("/get/{idUser}")
    public ResponseEntity<User> getSingleUser(@PathVariable long idUser) {
        User user = userService.getSingleUser(idUser);
        if (user != null) {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @PutMapping("/update/{idUser}")
    public User updateUser(@RequestBody User updatedUser, @PathVariable long idUser) {
        return userService.updateUser(updatedUser, idUser);
    }

    @DeleteMapping("/delete/{idUser}")
    public ResponseEntity<String> deleteUserById(@PathVariable long idUser) {
        try {
            userService.deleteUserById(idUser);
            return new ResponseEntity<>("Użytkownik o ID " + idUser + " został usunięty.", HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
