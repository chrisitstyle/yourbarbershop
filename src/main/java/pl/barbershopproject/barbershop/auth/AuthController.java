package pl.barbershopproject.barbershop.auth;


import pl.barbershopproject.barbershop.validation.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.barbershopproject.barbershop.model.User;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/users")
public class AuthController {

    private final AuthService authService;

    private final ValidationService<User> validator;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register (@RequestBody User user){
        validator.validate(user);
        return  ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody AuthRequest request){
        return  ResponseEntity.ok(authService.authenticate(request));
    }

}
