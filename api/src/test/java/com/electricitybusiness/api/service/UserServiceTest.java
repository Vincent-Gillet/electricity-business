package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.user.UserCreateDTO;
import com.electricitybusiness.api.dto.user.UserDTO;
import com.electricitybusiness.api.dto.user.UserUpdateDTO;
import com.electricitybusiness.api.exception.ConflictException;
import com.electricitybusiness.api.exception.ResourceNotFoundException;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.model.UserRole;
import com.electricitybusiness.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private UserService userService;

    private static final Long USER_ID = 1L;
    private static final String USER_EMAIL = "john.doe@example.com";
    private static final String USER_PASSWORD_RAW = "password123";
    private static final String USER_PASSWORD_ENCODED = "encoded_password_123"; // Simulate encoded password
    private static final String USER_FIRST_NAME = "John";
    private static final String USER_SURNAME = "Doe";
    private static final String USER_PSEUDO = "johndoe";
    private static final LocalDate USER_DOB = LocalDate.of(1990, 5, 15);
    private static final String USER_PHONE = "0123456789";
    private static final String USER_IBAN = "FR7612345678901234567890123"; // Example IBAN

    private UserCreateDTO sampleUserCreateDTO;
    private User sampleUserEntityToSave;
    private User sampleUserEntitySaved;
    private UserDTO sampleExpectedUserDTO;
    private User sampleUserEntityToUpdate;
    private UserUpdateDTO createSampleUserUpdateDTO;

    @BeforeEach
    void setUp() {
        sampleUserCreateDTO = UserCreateDTO.builder()
                .firstName(USER_FIRST_NAME)
                .surnameUser(USER_SURNAME)
                .pseudo(USER_PSEUDO)
                .emailUser(USER_EMAIL)
                .passwordUser(USER_PASSWORD_RAW)
                .dateOfBirth(USER_DOB)
                .phone(USER_PHONE)
                .build();

        sampleUserEntityToSave = User.builder()
                .firstName(USER_FIRST_NAME)
                .surnameUser(USER_SURNAME)
                .pseudo(USER_PSEUDO)
                .passwordUser(USER_PASSWORD_ENCODED)
                .emailUser(USER_EMAIL)
                .dateOfBirth(USER_DOB)
                .phone(USER_PHONE)
                .iban(USER_IBAN)
                .role(UserRole.USER)
                .banished(false)
                .build();

        sampleUserEntitySaved = User.builder()
                .idUser(USER_ID)
                .firstName(USER_FIRST_NAME)
                .surnameUser(USER_SURNAME)
                .pseudo(USER_PSEUDO)
                .passwordUser(USER_PASSWORD_ENCODED)
                .emailUser(USER_EMAIL)
                .dateOfBirth(USER_DOB)
                .phone(USER_PHONE)
                .iban(USER_IBAN)
                .role(UserRole.USER)
                .banished(false)
                .build();

        sampleExpectedUserDTO = UserDTO.builder()
                .firstName(USER_FIRST_NAME)
                .surnameUser(USER_SURNAME)
                .pseudo(USER_PSEUDO)
                .emailUser(USER_EMAIL)
                .dateOfBirth(USER_DOB)
                .phone(USER_PHONE)
                .iban(USER_IBAN)
                .role(UserRole.USER)
                .banished(false)
                .build();

        sampleUserEntityToUpdate = User.builder()
                .idUser(USER_ID)
                .firstName("OldFirstName")
                .surnameUser("OldSurname")
                .pseudo("OldPseudo")
                .passwordUser(USER_PASSWORD_ENCODED)
                .emailUser(USER_EMAIL)
                .dateOfBirth(USER_DOB)
                .phone(USER_PHONE)
                .iban(USER_IBAN)
                .role(UserRole.USER)
                .banished(false)
                .build();

        createSampleUserUpdateDTO = UserUpdateDTO.builder()
                .firstName("Jane")
                .surnameUser("Doe")
                .pseudo("janedoe")
                .emailUser("jane.doe@example.com")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .phone("987654321")
                .iban("IBAN987")
                .build();
    }

    /**
     * Test de la méthode saveUser du UserService.
     * Vérifie que l'utilisateur est correctement mappé, sauvegardé et renvoyé en DTO.
     */
    @Test
    void saveUser_Success() {
        // Arrange
        when(entityMapper.toEntity(sampleUserCreateDTO)).thenReturn(sampleUserEntityToSave);
        when(userRepository.save(sampleUserEntityToSave)).thenReturn(sampleUserEntitySaved);
        when(entityMapper.toDTO(sampleUserEntitySaved)).thenReturn(sampleExpectedUserDTO);
        // Act
        UserDTO resultDTO = userService.saveUser(sampleUserCreateDTO);

        // Assert
        assertNotNull(resultDTO);
        assertEquals(sampleExpectedUserDTO, resultDTO);

        // Assert: Verify that the mocks were called correctly.
        verify(entityMapper, times(1)).toEntity(sampleUserCreateDTO);
        verify(userRepository, times(1)).save(sampleUserEntityToSave);
        verify(entityMapper, times(1)).toDTO(sampleUserEntitySaved);
    }

    /**
     * Test de la méthode saveUser du UserService avec des entrées valides.
     * Vérifie que l'utilisateur est correctement mappé, sauvegardé et renvoyé en DTO.
     */
    @Test
    void saveUser_ValidInput_ReturnsUserDTO() {
        // Arrange
        UserCreateDTO userCreateDTO = sampleUserCreateDTO;
        User userEntityToSave = sampleUserEntityToSave; // Adjust if encoding is done in service
        User userEntitySaved = sampleUserEntitySaved;
        UserDTO expectedUserDTO = sampleExpectedUserDTO;

        when(entityMapper.toEntity(userCreateDTO)).thenReturn(userEntityToSave);
        when(userRepository.save(userEntityToSave)).thenReturn(userEntitySaved);
        when(entityMapper.toDTO(userEntitySaved)).thenReturn(expectedUserDTO);

        // Act
        UserDTO resultDTO = userService.saveUser(userCreateDTO);

        // Assert
        assertNotNull(resultDTO);
        assertEquals(expectedUserDTO, resultDTO);

        verify(entityMapper, times(1)).toEntity(userCreateDTO);
        verify(userRepository, times(1)).save(userEntityToSave);
        verify(entityMapper, times(1)).toDTO(userEntitySaved);
    }

    /**
     * Test de la méthode getUserById du UserService lorsque l'utilisateur n'existe pas.
     * Vérifie que la ResourceNotFoundException est levée.
     */
    @Test
    void getUserById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Long nonExistentUserId = 999L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(nonExistentUserId);
        }, "Appeler getUserById avec un ID inexistant devrait lever une ResourceNotFoundException.");

        // Assert
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verifyNoInteractions(entityMapper);
    }

    /**
     * Test de la méthode deleteUserById du UserService.
     * Vérifie que l'utilisateur est supprimé correctement.
     */
    @Test
    void deleteUser_Success() {
        // Arrange
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        doNothing().when(userRepository).deleteById(USER_ID);

        // Act
        userService.deleteUserById(USER_ID);

        verify(userRepository, times(1)).existsById(USER_ID);
        verify(userRepository, times(1)).deleteById(USER_ID);
    }

    /**
     * Test de la méthode deleteUserById du UserService lorsque l'utilisateur n'existe pas.
     * Vérifie que la ResourceNotFoundException est levée.
     */
    @Test
    void deleteUser_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUserById(USER_ID);
        });

        // Assert
        verify(userRepository, times(1)).existsById(USER_ID);
        verify(userRepository, never()).deleteById(anyLong());
    }

    /**
     * Test de la méthode getUserById du UserService.
     * Vérifie que l'utilisateur est récupéré correctement.
     */
    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(sampleUserEntitySaved));

        // Act
        User foundUser = userService.getUserById(USER_ID);

        // Assert
        assertNotNull(foundUser);
        assertEquals(sampleUserEntitySaved, foundUser);

        verify(userRepository, times(1)).findById(USER_ID);
        verifyNoInteractions(entityMapper);
    }

    /**
     * Test de la méthode updateUser du UserService.
     * Vérifie que l'utilisateur est mis à jour correctement.
     */
    @Test
    void updateUser_Success() {
        // Arrange
        Long userId = USER_ID;

        User existingUserEntity = User.builder()
                .idUser(userId)
                .firstName("InitialFirstName")
                .surnameUser("InitialSurname")
                .pseudo("InitialPseudo")
                .emailUser("initial@example.com")
                .passwordUser(USER_PASSWORD_ENCODED)
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .phone("1111111111")
                .iban("FR0000000000000000000000000")
                .role(UserRole.USER)
                .banished(false)
                .build();

        when(userRepository.existsById(userId)).thenReturn(true);

        User userEntityPassedToService = User.builder()
                .idUser(userId)
                .firstName(createSampleUserUpdateDTO.getFirstName())
                .surnameUser(createSampleUserUpdateDTO.getSurnameUser())
                .pseudo(createSampleUserUpdateDTO.getPseudo())
                .emailUser(createSampleUserUpdateDTO.getEmailUser())
                .passwordUser(existingUserEntity.getPasswordUser())
                .dateOfBirth(createSampleUserUpdateDTO.getDateOfBirth())
                .phone(createSampleUserUpdateDTO.getPhone())
                .iban(createSampleUserUpdateDTO.getIban())
                .role(existingUserEntity.getRole())
                .build();

        User savedUpdatedUserEntity = User.builder()
                .idUser(userId)
                .firstName(createSampleUserUpdateDTO.getFirstName())
                .surnameUser(createSampleUserUpdateDTO.getSurnameUser())
                .pseudo(createSampleUserUpdateDTO.getPseudo())
                .emailUser(createSampleUserUpdateDTO.getEmailUser())
                .passwordUser(existingUserEntity.getPasswordUser())
                .dateOfBirth(createSampleUserUpdateDTO.getDateOfBirth())
                .phone(createSampleUserUpdateDTO.getPhone())
                .iban(createSampleUserUpdateDTO.getIban())
                .role(existingUserEntity.getRole())
                .build();
        when(userRepository.save(userEntityPassedToService)).thenReturn(savedUpdatedUserEntity);

        UserDTO expectedUserDTO = UserDTO.builder()
                .firstName(createSampleUserUpdateDTO.getFirstName())
                .surnameUser(createSampleUserUpdateDTO.getSurnameUser())
                .pseudo(createSampleUserUpdateDTO.getPseudo())
                .emailUser(createSampleUserUpdateDTO.getEmailUser())
                .dateOfBirth(createSampleUserUpdateDTO.getDateOfBirth())
                .phone(createSampleUserUpdateDTO.getPhone())
                .iban(createSampleUserUpdateDTO.getIban())
                .role(existingUserEntity.getRole())
                .build();
        when(entityMapper.toDTO(savedUpdatedUserEntity)).thenReturn(expectedUserDTO);

        // Act
        User resultUserEntity = userService.updateUser(userId, userEntityPassedToService);

        UserDTO resultDTO = entityMapper.toDTO(resultUserEntity);

        // Assert
        assertNotNull(resultDTO);
        assertEquals(expectedUserDTO, resultDTO);

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).save(userEntityPassedToService);
        verify(entityMapper, times(1)).toDTO(savedUpdatedUserEntity);
    }

    /**
     * Test de la méthode updateUser du UserService lorsque l'utilisateur n'existe pas.
     * Vérifie que la ResourceNotFoundException est levée.
     */
    @Test
    void updateUser_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            User dummyUserEntity = User.builder().idUser(USER_ID).build();
            userService.updateUser(USER_ID, dummyUserEntity);
        });

        // Assert
        assertEquals("User not found with id: " + USER_ID, exception.getMessage());

        verify(userRepository, times(1)).existsById(USER_ID);
        verify(userRepository, never()).findById(USER_ID);
        verifyNoInteractions(entityMapper);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test de la méthode updateUserToken du UserService.
     * Vérifie que l'utilisateur est mis à jour correctement.
     */
    @Test
    void updateUserToken_WithNewEmail_UpdatesEmail() {
        // Arrange
        User existingUser = new User();
        existingUser.setIdUser(1L);
        existingUser.setEmailUser("old@email.com");

        User updatedUser = new User();
        updatedUser.setEmailUser("new@email.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailUser("new@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // Act
        User result = userService.updateUserToken(1L, updatedUser);

        // Assert
        assertThat(result.getEmailUser()).isEqualTo("new@email.com");
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmailUser("new@email.com");
        verify(userRepository, times(1)).save(existingUser);
    }

    /**
     * Test de la méthode updateUserToken du UserService lorsque l'email existe déjà.
     * Vérifie que la ConflictException est levée.
     */
    @Test
    void updateUserToken_WithExistingEmail_ThrowsConflictException() {
        // Arrange
        User existingUser = new User();
        existingUser.setIdUser(1L);
        existingUser.setEmailUser("old@email.com");

        User updatedUser = new User();
        updatedUser.setEmailUser("existing@email.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmailUser("existing@email.com")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> userService.updateUserToken(1L, updatedUser));
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmailUser("existing@email.com");
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test de la méthode updateUserToken du UserService lorsque l'utilisateur n'existe pas.
     * Vérifie que la ResourceNotFoundException est levée.
     */
    @Test
    void updateUserToken_WithNonExistentUser_ThrowsResourceNotFoundException() {
        // Arrange
        User updatedUser = new User();
        updatedUser.setEmailUser("new@email.com");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserToken(999L, updatedUser));
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).existsByEmailUser(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Test de la méthode getAllUsers du UserService.
     * Vérifie que la liste des utilisateurs est récupérée correctement.
     */
    @Test
    void getAllUsers_ReturnsListOfUserDTOs() {
        // Arrange
        List<User> mockUsers = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(mockUsers);

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        verify(userRepository, times(1)).findAll();
    }

    /**
     * Test de la méthode getAllUsers du UserService lorsque la liste est vide.
     * Vérifie que la liste vide est renvoyée correctement.
     */
    @Test
    void getAllUsers_WithEmptyList_ReturnsEmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(List.of());

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(0);
        verify(userRepository, times(1)).findAll();
    }

    /**
     * Test de la méthode getIdByEmailUser du UserService.
     * Vérifie que l'ID de l'utilisateur est récupéré correctement par email.
     */
    @Test
    void getIdByEmailUser_WithExistingEmail_ReturnsId() {
        // Arrange
        User mockUser = new User();
        mockUser.setIdUser(1L);
        mockUser.setEmailUser("test@email.com");

        when(userRepository.findByEmailUser("test@email.com")).thenReturn(Optional.of(mockUser));

        // Act
        Long result = userService.getIdByEmailUser("test@email.com");

        // Assert
        assertThat(result).isEqualTo(1L);
        verify(userRepository, times(1)).findByEmailUser("test@email.com");
    }

    /**
     * Test de la méthode getIdByEmailUser du UserService lorsque l'email n'existe pas.
     * Vérifie que la ResourceNotFoundException est levée.
     */
    @Test
    void getIdByEmailUser_WithNonExistentEmail_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmailUser("nonexistent@email.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getIdByEmailUser("nonexistent@email.com"));
        verify(userRepository, times(1)).findByEmailUser("nonexistent@email.com");
    }

    /**
     * Test de la méthode getUserByEmail du UserService.
     * Vérifie que l'utilisateur est récupéré correctement par email.
     */
    @Test
    void getUserByEmail_WithExistingEmail_ReturnsUser() {
        // Arrange
        User mockUser = new User();
        mockUser.setEmailUser("test@email.com");

        when(userRepository.findByEmailUser("test@email.com")).thenReturn(Optional.of(mockUser));

        // Act
        User result = userService.getUserByEmail("test@email.com");

        // Assert
        assertThat(result).isEqualTo(mockUser);
        verify(userRepository, times(1)).findByEmailUser("test@email.com");
    }

    /**
     * Test de la méthode getUserByEmail du UserService lorsque l'email n'existe pas.
     * Vérifie que la ResourceNotFoundException est levée.
     */
    @Test
    void getUserByEmail_WithNonExistentEmail_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmailUser("nonexistent@email.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByEmail("nonexistent@email.com"));
        verify(userRepository, times(1)).findByEmailUser("nonexistent@email.com");
    }

    /**
     * Test de la méthode findByPseudo du UserService.
     * Vérifie que l'utilisateur est récupéré correctement par pseudo.
     */
    @Test
    void findByPseudo_WithExistingPseudo_ReturnsUser() {
        // Arrange
        User mockUser = new User();
        mockUser.setPseudo("testPseudo");

        when(userRepository.findByPseudo("testPseudo")).thenReturn(Optional.of(mockUser));

        // Act
        Optional<User> result = userService.findByPseudo("testPseudo");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(mockUser);
        verify(userRepository, times(1)).findByPseudo("testPseudo");
    }

    /**
     * Test de la méthode findByPseudo du UserService lorsque le pseudo n'existe pas.
     * Vérifie que l'Optional vide est renvoyé.
     */
    @Test
    void findByPseudo_WithNonExistentPseudo_ReturnsEmptyOptional() {
        // Arrange
        when(userRepository.findByPseudo("nonexistentPseudo")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByPseudo("nonexistentPseudo");

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findByPseudo("nonexistentPseudo");
    }

    /**
     * Test de la méthode existsById du UserService.
     * Vérifie si un utilisateur existe par son ID.
     */
    @Test
    void existsById_WithExistingId_ReturnsTrue() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = userService.existsById(1L);

        // Assert
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsById(1L);
    }

    /**
     * Test de la méthode existsById du UserService lorsque l'ID n'existe pas.
     * Vérifie que la méthode renvoie false.
     */
    @Test
    void existsById_WithNonExistentId_ReturnsFalse() {
        // Arrange
        when(userRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = userService.existsById(999L);

        // Assert
        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsById(999L);
    }

    /**
     * Test de la méthode existsByPseudo du UserService.
     * Vérifie si un utilisateur existe par son pseudo.
     */
    @Test
    void existsByPseudo_WithExistingPseudo_ReturnsTrue() {
        // Arrange
        when(userRepository.existsByPseudo("existingPseudo")).thenReturn(true);

        // Act
        boolean result = userService.existsByPseudo("existingPseudo");

        // Assert
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsByPseudo("existingPseudo");
    }

    /**
     * Test de la méthode existsByPseudo du UserService lorsque le pseudo n'existe pas.
     * Vérifie que la méthode renvoie false.
     */
    @Test
    void existsByPseudo_WithNonExistentPseudo_ReturnsFalse() {
        // Arrange
        when(userRepository.existsByPseudo("nonexistentPseudo")).thenReturn(false);

        // Act
        boolean result = userService.existsByPseudo("nonexistentPseudo");

        // Assert
        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsByPseudo("nonexistentPseudo");
    }

    /**
     * Test de la méthode existsByEmailUser du UserService.
     * Vérifie si un utilisateur existe par son email.
     */
    @Test
    void existsByEmailUser_WithExistingEmail_ReturnsTrue() {
        // Arrange
        when(userRepository.existsByEmailUser("existing@email.com")).thenReturn(true);

        // Act
        boolean result = userService.existsByEmailUser("existing@email.com");

        // Assert
        assertThat(result).isTrue();
        verify(userRepository, times(1)).existsByEmailUser("existing@email.com");
    }

    /**
     * Test de la méthode existsByEmailUser du UserService lorsque l'email n'existe pas.
     * Vérifie que la méthode renvoie false.
     */
    @Test
    void existsByEmailUser_WithNonExistentEmail_ReturnsFalse() {
        // Arrange
        when(userRepository.existsByEmailUser("nonexistent@email.com")).thenReturn(false);

        // Act
        boolean result = userService.existsByEmailUser("nonexistent@email.com");

        // Assert
        assertThat(result).isFalse();
        verify(userRepository, times(1)).existsByEmailUser("nonexistent@email.com");
    }
}