package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.user.UserCreateDTO;
import com.electricitybusiness.api.dto.user.UserDTO;
import com.electricitybusiness.api.dto.user.UserUpdateDTO;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    // @Mock // Uncomment if your UserService uses it directly for encoding
    // private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // Constants
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

    // --- Test Data ---
    private UserCreateDTO sampleUserCreateDTO;
    private User sampleUserEntityToSave;
    private User sampleUserEntitySaved;
    private UserDTO sampleExpectedUserDTO;
    private User sampleUserEntityToUpdate;
    private User sampleUserEntityAfterUpdate;
    private UserDTO sampleUserUpdateDTO;

    @BeforeEach
    void setUp() {
        // Initialize common DTOs/Entities
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
                .passwordUser(USER_PASSWORD_ENCODED) // Assume encoding happens in mapper/service
                .emailUser(USER_EMAIL)
                .dateOfBirth(USER_DOB)
                .phone(USER_PHONE)
                .iban(USER_IBAN)
                .role(UserRole.USER)
                .banished(false)
                .build();

        sampleUserEntitySaved = User.builder()
                .idUser(USER_ID) // ID is added by the repository
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

    }

    private UserUpdateDTO createSampleUserUpdateDTO() {
        return UserUpdateDTO.builder()
                .firstName("Jane")
                .surnameUser("Doe") // Assurez-vous d'utiliser le bon nom de champ si c'est `surnameUser` dans votre DTO
                .pseudo("janedoe")
                .emailUser("jane.doe@example.com")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .phone("987654321")
                .iban("IBAN987")
                .build();
    }

    @Test
    void saveUser_Success() {
        // Arrange: Configure mocks for this specific test.
        // These stubs must match the values of the instance variables initialized in setUp().

        // 1. Stub the mapper to convert the sample input DTO to a sample entity.
        when(entityMapper.toEntity(sampleUserCreateDTO)).thenReturn(sampleUserEntityToSave);

        // 2. Stub the repository to return the sample saved entity when the sample entity to save is passed.
        when(userRepository.save(sampleUserEntityToSave)).thenReturn(sampleUserEntitySaved);

        // 3. Stub the mapper to convert the sample saved entity to the sample expected DTO.
        when(entityMapper.toDTO(sampleUserEntitySaved)).thenReturn(sampleExpectedUserDTO);

        // Act: Call the service method using the sample input DTO.
        // Note: We are passing the instance variable here.
        UserDTO resultDTO = userService.saveUser(sampleUserCreateDTO);

        // Assert: Verify the returned DTO matches the expected one.
        assertNotNull(resultDTO);
        assertEquals(sampleExpectedUserDTO, resultDTO); // Assumes UserDTO implements equals() and hashCode()

        // Assert: Verify that the mocks were called correctly.
        verify(entityMapper, times(1)).toEntity(sampleUserCreateDTO);
        verify(userRepository, times(1)).save(sampleUserEntityToSave);
        verify(entityMapper, times(1)).toDTO(sampleUserEntitySaved);
    }

    @Test
    void saveUser_ValidInput_ReturnsUserDTO() {
        // Arrange: Configure mocks specific to this test.
        // Use cloned or specific data if needed, but here sample data works.
        UserCreateDTO userCreateDTO = sampleUserCreateDTO;
        User userEntityToSave = sampleUserEntityToSave; // Adjust if encoding is done in service
        User userEntitySaved = sampleUserEntitySaved;
        UserDTO expectedUserDTO = sampleExpectedUserDTO;

        // Stubbing the mapper and repository for this specific successful save operation.
        when(entityMapper.toEntity(userCreateDTO)).thenReturn(userEntityToSave);
        when(userRepository.save(userEntityToSave)).thenReturn(userEntitySaved);
        when(entityMapper.toDTO(userEntitySaved)).thenReturn(expectedUserDTO);

        // Act
        UserDTO resultDTO = userService.saveUser(userCreateDTO);

        // Assert - Check the returned value
        assertNotNull(resultDTO);
        assertEquals(expectedUserDTO, resultDTO);

        // Assert - Verify interactions
        verify(entityMapper, times(1)).toEntity(userCreateDTO);
        verify(userRepository, times(1)).save(userEntityToSave);
        verify(entityMapper, times(1)).toDTO(userEntitySaved);
    }

    @Test
    void getUserById_NotFound_ThrowsResourceNotFoundException() {
        // Arrange: Configurer le repository pour retourner Optional.empty() pour l'ID spécifié.
        Long nonExistentUserId = 999L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert: Utiliser assertThrows pour vérifier l'exception.
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(nonExistentUserId);
        }, "Appeler getUserById avec un ID inexistant devrait lever une ResourceNotFoundException.");

        // Optionnel : Vérifier le message de l'exception
        // assertTrue(exception.getMessage().contains("User not found with id: " + nonExistentUserId));

        // Assert: Vérifier que findById a été appelé correctement.
        verify(userRepository, times(1)).findById(nonExistentUserId);
        // Assurez-vous que le mapper n'est pas appelé dans ce cas d'erreur.
        verifyNoInteractions(entityMapper);
    }


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

    @Test
    void getUserById_Success() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(sampleUserEntitySaved));

        // Act
        User foundUser = userService.getUserById(USER_ID);

        // Assert
        assertNotNull(foundUser);
        assertEquals(sampleUserEntitySaved, foundUser);

        // Assert
        verify(userRepository, times(1)).findById(USER_ID);
        verifyNoInteractions(entityMapper);
    }

/*    @Test
    void updateUser_Success() {
        // Arrange
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(sampleUserEntityToUpdate));

        when(entityMapper.toEntity(any(UserUpdateDTO.class), eq(sampleUserEntityToUpdate))).thenReturn(sampleUserEntityAfterUpdate);

        when(userRepository.save(sampleUserEntityAfterUpdate)).thenReturn(sampleUserEntitySaved);

        when(entityMapper.toDTO(sampleUserEntitySaved)).thenReturn(sampleExpectedUserDTO);

        // Act
        User userEntityToUpdate = entityMapper.toEntity(sampleUserUpdateDTO);
        User updatedUserEntity = userService.updateUser(USER_ID, userEntityToUpdate);
        UserDTO resultDTO = entityMapper.toDTO(updatedUserEntity);

        // Assert
        assertNotNull(resultDTO);
        assertEquals(sampleExpectedUserDTO, resultDTO);

        // Assert
        verify(userRepository, times(1)).findById(USER_ID);
        verify(entityMapper, times(1)).toEntity(sampleUserUpdateDTO);
        verify(userRepository, times(1)).save(sampleUserEntityAfterUpdate);
        verify(entityMapper, times(1)).toDTO(sampleUserEntitySaved);
    }*/

    @Test
    void updateUser_Success() {
        // Arrange
        Long userId = USER_ID;
        UserUpdateDTO sampleUserUpdateDTO = createSampleUserUpdateDTO();

        User existingUserEntity = User.builder()
                .idUser(userId)
                .firstName("InitialFirstName")
                .surnameUser("InitialSurname")
                .pseudo("InitialPseudo")
                .emailUser("initial@example.com")
                .passwordUser(USER_PASSWORD_ENCODED) // Mot de passe encodé existant
                .dateOfBirth(LocalDate.of(1980, 1, 1)) // DOB différent du DTO
                .phone("1111111111")
                .iban("FR0000000000000000000000000")
                .role(UserRole.USER) // Rôle existant
                .banished(false) // Statut banni existant
                .build();

        when(userRepository.existsById(userId)).thenReturn(true);

        User userEntityPassedToService = User.builder()
                .idUser(userId) // L'ID de l'utilisateur qui est mis à jour
                .firstName(sampleUserUpdateDTO.getFirstName())
                .surnameUser(sampleUserUpdateDTO.getSurnameUser())
                .pseudo(sampleUserUpdateDTO.getPseudo())
                .emailUser(sampleUserUpdateDTO.getEmailUser())
                .passwordUser(existingUserEntity.getPasswordUser()) // Pour ce test, on conserve l'ancien mot de passe encodé
                .dateOfBirth(sampleUserUpdateDTO.getDateOfBirth())
                .phone(sampleUserUpdateDTO.getPhone())
                .iban(sampleUserUpdateDTO.getIban())
                .role(existingUserEntity.getRole())
                .build();

        // 3. Simuler le résultat de la sauvegarde
        // L'entité retournée par userRepository.save doit refléter les modifications.
        User savedUpdatedUserEntity = User.builder() // Nouvelle instance pour refléter le résultat de la sauvegarde
                .idUser(userId)
                .firstName(sampleUserUpdateDTO.getFirstName())
                .surnameUser(sampleUserUpdateDTO.getSurnameUser())
                .pseudo(sampleUserUpdateDTO.getPseudo())
                .emailUser(sampleUserUpdateDTO.getEmailUser())
                .passwordUser(existingUserEntity.getPasswordUser()) // Conservé
                .dateOfBirth(sampleUserUpdateDTO.getDateOfBirth())
                .phone(sampleUserUpdateDTO.getPhone())
                .iban(sampleUserUpdateDTO.getIban())
                .role(existingUserEntity.getRole()) // Conservé
                .build();
        when(userRepository.save(userEntityPassedToService)).thenReturn(savedUpdatedUserEntity);

        UserDTO expectedUserDTO = UserDTO.builder()
                .firstName(sampleUserUpdateDTO.getFirstName())
                .surnameUser(sampleUserUpdateDTO.getSurnameUser())
                .pseudo(sampleUserUpdateDTO.getPseudo())
                .emailUser(sampleUserUpdateDTO.getEmailUser())
                // Le mot de passe n'est généralement pas retourné dans un UserDTO.
                .dateOfBirth(sampleUserUpdateDTO.getDateOfBirth())
                .phone(sampleUserUpdateDTO.getPhone())
                .iban(sampleUserUpdateDTO.getIban())
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

    @Test
    void updateUser_NotFound_ThrowsResourceNotFoundException() {
        // Arrange
        // Simuler que l'utilisateur n'est pas trouvé.
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

}