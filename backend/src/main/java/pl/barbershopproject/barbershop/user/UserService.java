package pl.barbershopproject.barbershop.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.exception.EmailAlreadyExistsException;
import pl.barbershopproject.barbershop.exception.SelfDeletionException;
import pl.barbershopproject.barbershop.user.dto.UserCreationDTO;
import pl.barbershopproject.barbershop.user.dto.UserDTO;
import pl.barbershopproject.barbershop.user.dto.UserResponseDTO;
import pl.barbershopproject.barbershop.user.mapper.UserCreationDTOMapper;
import pl.barbershopproject.barbershop.user.mapper.UserDTOMapper;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO addUser(UserCreationDTO userCreationDTO) {
        userRepository.findByEmail(userCreationDTO.email())
                .ifPresent(u -> {
                    throw new EmailAlreadyExistsException("Użytkownik o podanym emailu istnieje!");
                });

        User user = UserCreationDTOMapper.toEntity(userCreationDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        return UserCreationDTOMapper.toResponseDTO(savedUser);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAllWithOrders()
                .stream()
                .map(UserDTOMapper::toDTO)
                .toList();
    }

    public UserDTO getUserById(long idUser) {
        User authUser = getAuthenticatedUser();
        validateUserAccess(authUser, idUser);

        return userRepository.findById(idUser)
                .map(UserDTOMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Użytkownik o podanym ID nie istnieje"));
    }

    @Transactional
    public User updateUser(User updatedUser, long idUser) {
        User authUser = getAuthenticatedUser();
        validateUserAccess(authUser, idUser);
        User existingUser = userRepository.findById(idUser)
                .orElseThrow(() -> new NoSuchElementException("Użytkownik o podanym ID nie istnieje"));

        updateUserFields(existingUser, updatedUser);
        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUserById(long idUser) {
        User authUser = getAuthenticatedUser();
        if (authUser.getIdUser() == idUser) {
            throw new SelfDeletionException("Nie można usunąć własnego konta");
        }

        if (!userRepository.existsById(idUser)) {
            throw new NoSuchElementException("Użytkownik o podanym ID nie istnieje");
        }

        userRepository.deleteById(idUser);
    }


    private User getAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private void validateUserAccess(User authUser, long targetUserId) {
        if (authUser.getIdUser() != targetUserId && !authUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Brak uprawnień");
        }
    }

    private void updateUserFields(User existing, User updated) {
        existing.setFirstname(updated.getFirstname());
        existing.setLastname(updated.getLastname());
        existing.setEmail(updated.getEmail());

        if (updated.getPassword() != null) {
            existing.setPassword(passwordEncoder.encode(updated.getPassword()));
        }
    }
}


