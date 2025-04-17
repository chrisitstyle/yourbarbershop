package pl.barbershopproject.barbershop.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.barbershopproject.barbershop.user.dto.UserDTO;

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
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> usersResponse = userService.getAllUsers();
        return ResponseEntity.ok(usersResponse);
    }

    @GetMapping("/get/{idUser}")
    public ResponseEntity<UserDTO> getSingleUser(@PathVariable long idUser) {
        UserDTO user = userService.getSingleUser(idUser);
        return user != null ?
                ResponseEntity.ok(user) :
                ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
