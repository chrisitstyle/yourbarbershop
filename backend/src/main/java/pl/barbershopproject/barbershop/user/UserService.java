package pl.barbershopproject.barbershop.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.exception.EmailAlreadyExistsException;
import pl.barbershopproject.barbershop.exception.SelfDeletionException;
import pl.barbershopproject.barbershop.user.dto.CurrentUserResponseDTO;
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
        if (userRepository.existsByEmail(userCreationDTO.email())) {
            throw new EmailAlreadyExistsException("Użytkownik o podanym emailu istnieje!");
        }

        User user = UserCreationDTOMapper.toEntity(userCreationDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        return UserCreationDTOMapper.toResponseDTO(savedUser);
    }

    public CurrentUserResponseDTO getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return CurrentUserResponseDTO.from(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAllWithOrders()
                .stream()
                .map(UserDTOMapper::toDTO)
                .toList();
    }

    public UserDTO getUserById(Long idUser) {
        User authUser = getAuthenticatedUser();
        validateUserAccess(authUser, idUser);

        return userRepository.findById(idUser)
                .map(UserDTOMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Użytkownik o podanym ID nie istnieje"));
    }

    @Transactional
    public User updateUser(User updatedUser, Long idUser) {
        User authUser = getAuthenticatedUser();
        validateUserAccess(authUser, idUser);
        User existingUser = userRepository.findById(idUser)
                .orElseThrow(() -> new NoSuchElementException("Użytkownik o podanym ID nie istnieje"));

        updateUserFields(existingUser, updatedUser);
        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUserById(Long idUser) {
        User authUser = getAuthenticatedUser();
        if (authUser == null) {
            throw new AccessDeniedException("Brak zalogowanego użytkownika");
        }
        if (authUser.getIdUser().equals(idUser)) {
            throw new SelfDeletionException("Nie można usunąć własnego konta");
        }

        if (!userRepository.existsById(idUser)) {
            throw new NoSuchElementException("Użytkownik o podanym ID nie istnieje");
        }

        userRepository.deleteById(idUser);
    }


    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new AccessDeniedException("Użytkownik nie jest poprawnie uwierzytelniony");
        }
        return (User) authentication.getPrincipal();
    }

    private void validateUserAccess(User authUser, Long targetUserId) {
        if (authUser == null) {
            throw new AccessDeniedException("Brak kontekstu użytkownika");
        }

        if (!authUser.getIdUser().equals(targetUserId) && authUser.getRole() != Role.ADMIN) {
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


