package pl.barbershopproject.barbershop.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.barbershopproject.barbershop.user.dto.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Users", description = "Management of user profiles and application users list")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Add a new user (e.g., by an administrator)")
    @ApiResponse(responseCode = "201", description = "User created")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "422", description = "Email already exists in the system")
    @PostMapping
    public ResponseEntity<UserResponseDTO> addUser(@Valid @RequestBody UserCreationDTO userCreationDTO) {
        UserResponseDTO savedUser = userService.addUser(userCreationDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.idUser())
                .toUri();

        return ResponseEntity.created(location).body(savedUser);
    }

    @Operation(summary = "Get information about the currently logged-in user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved current user details")
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/me")
    public CurrentUserResponseDTO getCurrentUser(
            @Parameter(hidden = true) Authentication authentication
    ) {
        return userService.getCurrentUser(authentication.getName());
    }

    @Operation(summary = "Get a list of all users")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Access restricted to administrators")
    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Get specific user data by ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user data")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions to view this user")
    @ApiResponse(responseCode = "404", description = "User not found with the provided ID")
    @GetMapping("/{idUser}")
    public UserDTO getSingleUser(@PathVariable Long idUser) {
        return userService.getUserById(idUser);
    }

    @Operation(summary = "Update user profile data")
    @ApiResponse(responseCode = "200", description = "User profile successfully updated")
    @ApiResponse(responseCode = "400", description = "Invalid input data provided")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions to update this user profile")
    @ApiResponse(responseCode = "404", description = "User not found with the provided ID")
    @PutMapping("/{idUser}")
    public UserDTO updateUser(@Valid @RequestBody UserProfileUpdateRequestDTO updatedUser, @PathVariable Long idUser) {
        return userService.updateUser(updatedUser, idUser);
    }

    @Operation(summary = "Delete user account from the system")
    @ApiResponse(responseCode = "204", description = "User account successfully deleted")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Attempted self-deletion or missing admin permissions")
    @ApiResponse(responseCode = "404", description = "User not found with the provided ID")
    @DeleteMapping("/{idUser}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable Long idUser) {
        userService.deleteUserById(idUser);
    }
}