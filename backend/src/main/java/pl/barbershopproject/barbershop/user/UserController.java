package pl.barbershopproject.barbershop.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.barbershopproject.barbershop.user.dto.CurrentUserResponseDTO;
import pl.barbershopproject.barbershop.user.dto.UserCreationDTO;
import pl.barbershopproject.barbershop.user.dto.UserDTO;
import pl.barbershopproject.barbershop.user.dto.UserResponseDTO;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDTO> addUser(@Valid @RequestBody UserCreationDTO userCreationDTO) {
        UserResponseDTO savedUser = userService.addUser(userCreationDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.idUser())
                .toUri();

        return ResponseEntity.created(location).body(savedUser);
    }

    @GetMapping("/me")
    public CurrentUserResponseDTO getCurrentUser(
            Authentication authentication
    ) {
        return userService.getCurrentUser(authentication.getName());
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();

    }

    @GetMapping("/{idUser}")
    public UserDTO getSingleUser(@PathVariable Long idUser) {
        return userService.getUserById(idUser);
    }

    @PutMapping("/{idUser}")
    public User updateUser(@Valid @RequestBody User updatedUser, @PathVariable Long idUser) {
        return userService.updateUser(updatedUser, idUser);
    }

    @DeleteMapping("/{idUser}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable Long idUser) {
        userService.deleteUserById(idUser);

    }
}
