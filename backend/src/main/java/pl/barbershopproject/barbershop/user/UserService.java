package pl.barbershopproject.barbershop.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.audit.enums.ActionType;
import pl.barbershopproject.barbershop.audit.enums.EntityType;
import pl.barbershopproject.barbershop.audit.event.AuditEvent;
import pl.barbershopproject.barbershop.exception.EmailAlreadyExistsException;
import pl.barbershopproject.barbershop.exception.SelfDeletionException;
import pl.barbershopproject.barbershop.security.AuthenticatedUser;
import pl.barbershopproject.barbershop.security.CurrentUserProvider;
import pl.barbershopproject.barbershop.user.dto.*;
import pl.barbershopproject.barbershop.user.mapper.UserCreationDTOMapper;
import pl.barbershopproject.barbershop.user.mapper.UserDTOMapper;
import pl.barbershopproject.barbershop.utils.SecurityUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static pl.barbershopproject.barbershop.utils.SecurityUtils.getActorEmailSafely;

@Service
@RequiredArgsConstructor
public class UserService {

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public UserResponseDTO addUser(UserCreationDTO userCreationDTO) {
        if (userRepository.existsByEmail(userCreationDTO.email())) {
            throw new EmailAlreadyExistsException("Użytkownik o podanym emailu istnieje!");
        }

        User user = UserCreationDTOMapper.toEntity(userCreationDTO);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        eventPublisher.publishEvent(new AuditEvent(
                getActorEmailSafely(),
                ActionType.USER_CREATED,
                EntityType.USER,
                String.valueOf(savedUser.getIdUser()),
                String.format("{\"email\":\"%s\", \"role\":\"%s\"}", savedUser.getEmail(), savedUser.getRole())
        ));

        return UserCreationDTOMapper.toResponseDTO(savedUser);
    }

    public CurrentUserResponseDTO getCurrentUser() {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        User user = userRepository.findById(authenticatedUser.userId())
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
        AuthenticatedUser authUser = currentUserProvider.getCurrentUser();
        validateUserAccess(authUser, idUser);

        return userRepository.findById(idUser)
                .map(UserDTOMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Użytkownik o podanym ID nie istnieje"));
    }

    @Transactional
    public UserDTO updateUser(UserProfileUpdateRequestDTO updatedUser, Long idUser) {
        AuthenticatedUser authUser = currentUserProvider.getCurrentUser();
        validateUserAccess(authUser, idUser);

        User existingUser = userRepository.findById(idUser)
                .orElseThrow(() -> new NoSuchElementException("Użytkownik o podanym ID nie istnieje"));

        updateUserFields(existingUser, updatedUser);

        User savedUser = userRepository.save(existingUser);

        eventPublisher.publishEvent(new AuditEvent(
                SecurityUtils.getActorEmailSafely(),
                ActionType.USER_UPDATED,
                EntityType.USER,
                String.valueOf(idUser),
                String.format("{\"email\":\"%s\", \"firstname\":\"%s\", \"lastname\":\"%s\"}",
                        savedUser.getEmail(), savedUser.getFirstname(), savedUser.getLastname())
        ));

        return UserDTOMapper.toDTO(savedUser);
    }

    @Transactional
    public void deleteUserById(Long idUser) {
        AuthenticatedUser authUser = currentUserProvider.getCurrentUser();

        if (Objects.equals(authUser.userId(), idUser)) {
            throw new SelfDeletionException("Nie można usunąć własnego konta");
        }

        if (!userRepository.existsById(idUser)) {
            throw new NoSuchElementException("Użytkownik o podanym ID nie istnieje");
        }

        userRepository.deleteById(idUser);

        eventPublisher.publishEvent(new AuditEvent(
                SecurityUtils.getActorEmailSafely(),
                ActionType.USER_DELETED,
                EntityType.USER,
                String.valueOf(idUser),
                null
        ));
    }

    private void validateUserAccess(AuthenticatedUser authenticatedUser, Long targetUserId) {
        if (!authenticatedUser.userId().equals(targetUserId)
                && authenticatedUser.role() != Role.ADMIN) {
            throw new AccessDeniedException("You are not allowed to access this user");
        }
    }

    private void updateUserFields(User existing, UserProfileUpdateRequestDTO updated) {
        existing.setFirstname(updated.firstname());
        existing.setLastname(updated.lastname());
        existing.setEmail(updated.email());
    }

}