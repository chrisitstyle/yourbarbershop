package pl.barbershopproject.barbershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.barbershopproject.barbershop.model.User;
import pl.barbershopproject.barbershop.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    //create
    public ResponseEntity<User> addUser(User user) {
        Optional<User> userFromDatabase = userRepository.findByEmail(user.getEmail());

        if (userFromDatabase.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Użytkownik o podanym e-mail istnieje");
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
    //read
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public User getSingleUser(long id_user) {

        return userRepository.findById(id_user).orElseThrow(NoSuchElementException::new);

    }
    //update
    @Transactional
    public User updateUser(User updatedUser, long id_user) {
        return userRepository.findById(id_user)
                .map(user -> {
                    user.setFirstname(updatedUser.getFirstname());
                    user.setLastname(updatedUser.getLastname());
                    user.setEmail(updatedUser.getEmail());
                    user.setPassword(updatedUser.getPassword());
                    return userRepository.save(user);
                }).orElseThrow(NoSuchElementException::new);
    }
    //delete
    @Transactional
    public void deleteUserById(long id_user) {
        Optional<User> userExists = userRepository.findById(id_user);

        if (userExists.isPresent()) {
            userRepository.deleteById(id_user);
        } else {
            throw new NoSuchElementException("Użytkownik o podanym ID nie istnieje");
        }
    }


}
