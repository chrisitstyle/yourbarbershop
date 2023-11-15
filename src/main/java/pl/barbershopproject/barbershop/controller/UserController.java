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
    //create
    @PostMapping("")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        return userService.addUser(user);
    }
    //read
    @GetMapping("")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    @GetMapping("/{id_user}")
    public User getSingleUser(@PathVariable long id_user){
        return userService.getSingleUser(id_user);
    }
    //update
    @PutMapping("/{id_user}")
    public User updateUser(@RequestBody User updatedUser, @PathVariable long id_user){
        return userService.updateUser(updatedUser, id_user);
    }

    //delete
    @DeleteMapping("/{id_user}")
    public ResponseEntity<String> deleteUserById(@PathVariable long id_user) {
        try {
            userService.deleteUserById(id_user);
            return new ResponseEntity<>("Użytkownik o ID " + id_user + " został usunięty.", HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
