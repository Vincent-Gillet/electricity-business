package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Option;
import com.electricitybusiness.api.model.Place;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.model.UserRole;
import com.electricitybusiness.api.repository.OptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OptionServiceTest {
    @Mock
    private OptionRepository optionRepository;

    @InjectMocks
    private OptionService optionService;

    // Données de test communes
    private User testUser;
    private Place testPlace;
    private UUID testTerminalPublicId;
    private Option option1;
    private Option option2;
    private UUID publicId1;
    private UUID publicId2;
    private Long id1 = 1L;
    private Long id2 = 2L;

    @BeforeEach
    void setUp() {
        // Initialisation de l'utilisateur de test
        testUser = new User(
                10L, "Alice", "Smith", "asmith",
                "alice.smith@example.com", "password",
                UserRole.USER, LocalDate.of(1990, 5, 15),
                "0123456789", "FR12345678901234567890123",
                false, null, null, null, null, null
        );

        // Initialisation de la place de test
        testPlace = new Place(
                20L,
                UUID.randomUUID(),
                "Charging Station A",
                testUser,
                null,
                null
        );

        // Initialisation des IDs publics
        publicId1 = UUID.randomUUID();
        publicId2 = UUID.randomUUID();
        testTerminalPublicId = UUID.randomUUID();

        // Initialisation des options de test
        option1 = new Option(id1, publicId1, "Fast Charging", BigDecimal.valueOf(0.50), "Provides faster charging speeds", null, testPlace);
        option2 = new Option(id2, publicId2, "Parking Reservation", BigDecimal.valueOf(2.00), "Reserves a parking spot", null, testPlace);
    }

    /**
     * Test de la méthode getAllOptions pour vérifier qu'elle retourne la liste correcte des options.
     */
    @Test
    void getAllOptions_shouldReturnListOfOptions() {
        // Préparation du mock
        List<Option> options = Arrays.asList(option1, option2);
        when(optionRepository.findAll()).thenReturn(options);

        // Exécution
        List<Option> result = optionService.getAllOptions();

        // Vérification
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(option1, result.get(0));
        assertEquals(option2, result.get(1));
        verify(optionRepository, times(1)).findAll();
    }

    /**
     * Test de la méthode getAllOptions pour vérifier qu'elle retourne une liste vide lorsque
     * aucune option n'existe.
     */
    @Test
    void getAllOptions_shouldReturnEmptyList_whenNoOptionsExist() {
        // Préparation du mock
        when(optionRepository.findAll()).thenReturn(Collections.emptyList());

        // Exécution
        List<Option> result = optionService.getAllOptions();

        // Vérification
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(optionRepository, times(1)).findAll();
    }

    /**
     * Test de la méthode getOptionById pour vérifier qu'elle retourne l'option correcte
     * lorsqu'elle est trouvée.
     */
    @Test
    void getOptionById_shouldReturnOption_whenFound() {
        // Préparation du mock
        when(optionRepository.findById(id1)).thenReturn(Optional.of(option1));

        // Exécution
        Optional<Option> result = optionService.getOptionById(id1);

        // Vérification
        assertTrue(result.isPresent());
        assertEquals(option1, result.get());
        verify(optionRepository, times(1)).findById(id1);
    }

    /**
     * Test de la méthode getOptionById pour vérifier qu'elle retourne un Optional vide
     * lorsque l'option n'est pas trouvée.
     */
    @Test
    void getOptionById_shouldReturnEmptyOptional_whenNotFound() {
        // Préparation du mock
        when(optionRepository.findById(99L)).thenReturn(Optional.empty());

        // Exécution
        Optional<Option> result = optionService.getOptionById(99L);

        // Vérification
        assertFalse(result.isPresent());
        verify(optionRepository, times(1)).findById(99L);
    }

    /**
     * Test de la méthode saveOption pour vérifier qu'elle sauvegarde et retourne l'option correctement.
     */
    @Test
    void saveOption_shouldReturnSavedOption() {
        Option newOption = new Option(
                null,
                UUID.randomUUID(),
                "Premium Support",
                BigDecimal.valueOf(5.00),
                "24/7 technical support",
                null,
                testPlace);
        when(optionRepository.save(any(Option.class))).thenReturn(new Option(3L, newOption.getPublicId(), newOption.getNameOption(), newOption.getPriceOption(), newOption.getDescriptionOption(), newOption.getMedias(), newOption.getPlace()));

        // Exécution
        Option savedOption = optionService.saveOption(newOption);

        // Vérification
        assertNotNull(savedOption.getIdOption());
        assertEquals("Premium Support", savedOption.getNameOption());
        verify(optionRepository, times(1)).save(any(Option.class));
    }

    /**
     * Test de la méthode updateOption pour vérifier qu'elle met à jour et retourne l'option correctement.
     */
    @Test
    void updateOption_byId_shouldReturnUpdatedOption() {
        Option updatedOptionDetails = new Option(
                id1,
                publicId1,
                "Super Fast Charging",
                BigDecimal.valueOf(1.00),
                "Ultra-fast charging speeds",
                null,
                testPlace);

        when(optionRepository.save(any(Option.class))).thenReturn(updatedOptionDetails);

        Option result = optionService.updateOption(id1, updatedOptionDetails);

        assertNotNull(result);
        assertEquals(id1, result.getIdOption());
        assertEquals("Super Fast Charging", result.getNameOption());
        assertEquals(BigDecimal.valueOf(1.00), result.getPriceOption());
        verify(optionRepository, times(1)).save(any(Option.class));
    }

    /**
     * Test de la méthode deleteOptionById pour vérifier qu'elle appelle correctement le repository.
     */
    @Test
    void deleteOptionById_shouldCallRepositoryDelete() {
        // Exécution
        optionService.deleteOptionById(id1);

        // Vérification
        verify(optionRepository, times(1)).deleteById(id1);
    }

    /**
     * Test de la méthode existsById pour vérifier qu'elle retourne true lorsque l'option existe.
     */
    @Test
    void existsById_shouldReturnTrue_whenExists() {
        when(optionRepository.existsById(id1)).thenReturn(true);

        boolean exists = optionService.existsById(id1);

        assertTrue(exists);
        verify(optionRepository, times(1)).existsById(id1);
    }

    /**
     * Test de la méthode existsById pour vérifier qu'elle retourne false lorsque l'option n'existe pas.
     */
    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        when(optionRepository.existsById(99L)).thenReturn(false);

        boolean exists = optionService.existsById(99L);

        assertFalse(exists);
        verify(optionRepository, times(1)).existsById(99L);
    }

    /**
     * Test de la méthode getOptionsByPlace pour vérifier qu'elle retourne la liste correcte des options pour une place donnée.
     */
    @Test
    void getOptionsByPlace_shouldReturnListOfOptions_whenFound() {
        List<Option> optionsForPlace = Arrays.asList(option1, option2);
        when(optionRepository.findOptionsByPlace_PublicId(testPlace.getPublicId())).thenReturn(optionsForPlace);

        List<Option> result = optionService.getOptionsByPlace(testPlace.getPublicId());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(option1, result.get(0));
        assertEquals(option2, result.get(1));
        verify(optionRepository, times(1)).findOptionsByPlace_PublicId(testPlace.getPublicId());
    }

    /**
     * Test de la méthode getOptionsByPlace pour vérifier qu'elle retourne une liste vide lorsque
     * aucune option n'existe pour la place donnée.
     */
    @Test
    void getOptionsByPlace_shouldReturnEmptyList_whenNoOptionsForPlace() {
        UUID nonExistentPublicId = UUID.randomUUID();

        when(optionRepository.findOptionsByPlace_PublicId(nonExistentPublicId)).thenReturn(Collections.emptyList());

        List<Option> result = optionService.getOptionsByPlace(nonExistentPublicId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(optionRepository, times(1)).findOptionsByPlace_PublicId(any(UUID.class));
    }

    /**
     * Test de la méthode getOptionsByUser pour vérifier qu'elle retourne la liste correcte des options pour un utilisateur donné.
     */
    @Test
    void getOptionsByUser_shouldReturnListOfOptions_whenFound() {
        List<Option> optionsForUser = Arrays.asList(option1, option2);
        when(optionRepository.findOptionByPlace_User(testUser)).thenReturn(optionsForUser);

        List<Option> result = optionService.getOptionsByUser(testUser);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(option1, result.get(0));
        assertEquals(option2, result.get(1));
        verify(optionRepository, times(1)).findOptionByPlace_User(testUser);
    }

    /**
     * Test de la méthode getOptionsByUser pour vérifier qu'elle retourne une liste vide lorsque
     * aucune option n'existe pour l'utilisateur donné.
     */
    @Test
    void getOptionsByUser_shouldReturnEmptyList_whenNoOptionsForUser() {
        User anotherUser = new User(11L, "Bob", "Client", "bobc", "bob.c@example.com", "pass", UserRole.USER, LocalDate.now(), "0987654321", "FR", false, null, null, null, null, null);
        when(optionRepository.findOptionByPlace_User(anotherUser)).thenReturn(Collections.emptyList());

        List<Option> result = optionService.getOptionsByUser(anotherUser);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(optionRepository, times(1)).findOptionByPlace_User(anotherUser);
    }

    /**
     * Test de la méthode getOptionsByTerminal pour vérifier qu'elle retourne la liste correcte des options pour un terminal donné.
     */
    @Test
    void getOptionsByTerminal_shouldReturnListOfOptions_whenFound() {
        List<Option> optionsForTerminal = Collections.singletonList(option1);
        when(optionRepository.findByTerminalPublicId(testTerminalPublicId)).thenReturn(optionsForTerminal);

        List<Option> result = optionService.getOptionsByTerminal(testTerminalPublicId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(option1, result.get(0));
        verify(optionRepository, times(1)).findByTerminalPublicId(testTerminalPublicId);
    }

    /**
     * Test de la méthode getOptionsByTerminal pour vérifier qu'elle retourne une liste vide lorsque
     * aucune option n'existe pour le terminal donné.
     */
    @Test
    void getOptionsByTerminal_shouldReturnEmptyList_whenNoOptionsForTerminal() {
        UUID nonExistentTerminalId = UUID.randomUUID();
        when(optionRepository.findByTerminalPublicId(nonExistentTerminalId)).thenReturn(Collections.emptyList());

        List<Option> result = optionService.getOptionsByTerminal(nonExistentTerminalId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(optionRepository, times(1)).findByTerminalPublicId(nonExistentTerminalId);
    }

    /**
     * Test de la méthode getOptionByPublicId pour vérifier qu'elle retourne l'option correcte
     * lorsqu'elle est trouvée.
     */
    @Test
    void deleteOptionByPublicId_shouldCallRepositoryDelete() {
        // Exécution
        optionService.deleteOptionByPublicId(publicId1);

        // Vérification
        verify(optionRepository, times(1)).deleteOptionByPublicId(publicId1);
    }

    /**
     * Test de la méthode existsByPublicId pour vérifier qu'elle retourne true lorsque l'option existe.
     */
    @Test
    void existsByPublicId_shouldReturnTrue_whenExists() {
        when(optionRepository.findByPublicId(publicId1)).thenReturn(Optional.of(option1));

        boolean exists = optionService.existsByPublicId(publicId1);

        assertTrue(exists);
        verify(optionRepository, times(1)).findByPublicId(publicId1);
    }

    /**
     * Test de la méthode existsByPublicId pour vérifier qu'elle retourne false lorsque l'option n'existe pas.
     */
    @Test
    void existsByPublicId_shouldReturnFalse_whenNotExists() {
        UUID nonExistentPublicId = UUID.randomUUID();

        when(optionRepository.findByPublicId(nonExistentPublicId)).thenReturn(Optional.empty());

        boolean exists = optionService.existsByPublicId(nonExistentPublicId);

        assertFalse(exists);
        verify(optionRepository, times(1)).findByPublicId(any(UUID.class));
    }

    /**
     * Test de la méthode updateOption pour vérifier qu'elle met à jour et retourne l'option correctement en utilisant le publicId.
     */
    @Test
    void updateOption_byPublicId_shouldReturnUpdatedOption() {
        // Préparation du mock
        Option existingOption = new Option(
                id1,
                publicId1,
                "Original Name",
                BigDecimal.valueOf(10.0),
                "Original description",
                null,
                testPlace);
        Option updatedDetails = new Option(
                null,
                null,
                "Updated Name",
                BigDecimal.valueOf(12.5),
                "New description",
                null,
                null);

        when(optionRepository.findByPublicId(publicId1)).thenReturn(Optional.of(existingOption));
        when(optionRepository.save(any(Option.class))).thenReturn(
                new Option(existingOption.getIdOption(), existingOption.getPublicId(),
                        updatedDetails.getNameOption(), updatedDetails.getPriceOption(),
                        updatedDetails.getDescriptionOption(), existingOption.getMedias(), existingOption.getPlace())
        );

        // Exécution
        Option result = optionService.updateOption(publicId1, updatedDetails);

        // Vérification
        assertNotNull(result);
        assertEquals(id1, result.getIdOption());
        assertEquals(publicId1, result.getPublicId());
        assertEquals("Updated Name", result.getNameOption());
        assertEquals(BigDecimal.valueOf(12.5), result.getPriceOption());
        assertEquals("New description", result.getDescriptionOption());
        assertEquals(testPlace, result.getPlace());

        verify(optionRepository, times(1)).findByPublicId(publicId1);
        verify(optionRepository, times(1)).save(any(Option.class));
    }

    /**
     * Test de la méthode updateOption pour vérifier qu'elle lance une exception lorsque l'option n'est pas trouvée en utilisant le publicId.
     */
    @Test
    void updateOption_byPublicId_shouldThrowException_whenNotFound() {
        UUID nonExistentPublicId = UUID.randomUUID();
        Option anyOptionDetails = new Option(null, null, "Name", BigDecimal.ONE, "Desc", null, null);

        when(optionRepository.findByPublicId(nonExistentPublicId)).thenReturn(Optional.empty());

        // Exécution et Vérification
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            optionService.updateOption(nonExistentPublicId, anyOptionDetails);
        });

        assertTrue(thrown.getMessage().contains("Option with publicId not found: " + nonExistentPublicId));
        verify(optionRepository, times(1)).findByPublicId(nonExistentPublicId);
        verify(optionRepository, never()).save(any(Option.class));
    }
}
