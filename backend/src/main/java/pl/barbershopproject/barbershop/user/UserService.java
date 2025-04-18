package pl.barbershopproject.barbershop.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.barbershopproject.barbershop.user.dto.UserDTO;
import pl.barbershopproject.barbershop.user.mapper.UserDTOMapper;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public ResponseEntity<User> addUser(User user) {
        Optional<User> userFromDatabase = userRepository.findByEmail(user.getEmail());

        if (userFromDatabase.isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Użytkownik o podanym e-mail istnieje");
        }

        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAllWithOrders()
                .stream()
                .map(UserDTOMapper::toDTO)
                .toList();
    }

    public UserDTO getSingleUser(long idUser) {
        User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long loggedUserId = authUser.getIdUser();
        if (loggedUserId == idUser || authUser.getRole().equals(Role.ADMIN)) {
            User user =  userRepository.findById(idUser).orElseThrow(NoSuchElementException::new);
            return UserDTOMapper.toDTO(user);
        }else {
            return null;
        }
    }

    @Transactional
    public User updateUser(User updatedUser, long idUser) {
        return userRepository.findById(idUser)
                .map(user -> {
                    user.setFirstname(updatedUser.getFirstname());
                    user.setLastname(updatedUser.getLastname());
                    user.setEmail(updatedUser.getEmail());
                    user.setRole(updatedUser.getRole());

                    if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                        user.setPassword(updatedUser.getPassword());
                    }
                    return userRepository.save(user);
                }).orElseThrow(NoSuchElementException::new);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteUserById(long idUser) {
        User authUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        long loggedUserId = authUser.getIdUser();
        Optional<User> userExists = userRepository.findById(idUser);

            if (userExists.isPresent()) {
                if( loggedUserId != idUser) {
                    userRepository.deleteById(idUser);
                } else{ throw new IllegalArgumentException("Nie można usunąć obecnie zalogowanego użytkownika.");}

            } else {
                throw new NoSuchElementException("Użytkownik o podanym ID nie istnieje");
            }
    }



}
