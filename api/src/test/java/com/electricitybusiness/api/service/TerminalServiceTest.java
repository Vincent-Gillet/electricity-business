package com.electricitybusiness.api.service;

import com.electricitybusiness.api.exception.ResourceNotFoundException;
import com.electricitybusiness.api.model.Place;
import com.electricitybusiness.api.model.Terminal;
import com.electricitybusiness.api.model.TerminalStatus;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.repository.BookingRepository;
import com.electricitybusiness.api.repository.TerminalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TerminalServiceTest {

    @Mock
    private TerminalRepository terminalRepository;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private TerminalService terminalService;

    // Objets de test courants
    private Terminal mockTerminal;
    private Place mockPlace;
    private UUID publicId;
    private Long id = 1L;

    @BeforeEach
    void setUp() {
        publicId = UUID.randomUUID();
        mockPlace = new Place(
                1L,
                UUID.randomUUID(),
                "Test Place",
                null,
                null,
                null
        );
        mockTerminal = new Terminal();
        mockTerminal.setIdTerminal(id);
        mockTerminal.setPublicId(publicId);
        mockTerminal.setNameTerminal("Test Terminal");
        mockTerminal.setStatusTerminal(TerminalStatus.LIBRE);
        mockTerminal.setOccupied(false);
        mockTerminal.setPlace(mockPlace);
    }

    /**
     * Tests pour la méthode getTerminalById
     */
    @Test
    void getTerminalById_shouldReturnTerminalWhenFound() {
        when(terminalRepository.findById(id)).thenReturn(Optional.of(mockTerminal));

        Optional<Terminal> result = terminalService.getTerminalById(id);

        assertThat(result).isPresent().contains(mockTerminal);
        verify(terminalRepository, times(1)).findById(id);
    }

    /**
     * Tests pour la méthode getTerminalById lorsque le terminal n'est pas trouvé
     */
    @Test
    void getTerminalById_shouldReturnEmptyWhenNotFound() {
        when(terminalRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Terminal> result = terminalService.getTerminalById(99L);

        assertThat(result).isEmpty();
        verify(terminalRepository, times(1)).findById(99L);
    }

    /**
     * Tests pour la méthode getAllTerminals pour vérifier qu'elle retourne tous les terminaux
     */
    @Test
    void getAllTerminals_shouldReturnAllTerminals() {
        List<Terminal> terminals = Arrays.asList(mockTerminal, new Terminal());
        when(terminalRepository.findAll()).thenReturn(terminals);

        List<Terminal> result = terminalService.getAllTerminals();

        assertThat(result).isEqualTo(terminals);
        verify(terminalRepository, times(1)).findAll();
    }

    /**
     * Tests pour la méthode updateTerminal par ID pour vérifier qu'elle met à jour et sauvegarde le terminal
     */
    @Test
    void updateTerminal_byId_shouldSetIdAndSave() {
        Terminal updatedTerminalDetails = new Terminal();
        updatedTerminalDetails.setNameTerminal("Updated Name");
        updatedTerminalDetails.setPublicId(UUID.randomUUID());
        updatedTerminalDetails.setStatusTerminal(TerminalStatus.OCCUPEE);

        when(terminalRepository.save(any(Terminal.class))).thenReturn(updatedTerminalDetails);

        Terminal result = terminalService.updateTerminal(id, updatedTerminalDetails);

        assertThat(updatedTerminalDetails.getIdTerminal()).isEqualTo(id);
        assertThat(result.getNameTerminal()).isEqualTo("Updated Name");
        verify(terminalRepository, times(1)).save(updatedTerminalDetails);
    }

    /**
     * Tests pour la méthode existsByPublicId pour vérifier qu'elle retourne true si le terminal est trouvé
     */
    @Test
    void existsByPublicId_shouldReturnTrueWhenFound() {
        when(terminalRepository.findByPublicId(publicId)).thenReturn(Optional.of(mockTerminal));

        boolean result = terminalService.existsByPublicId(publicId);

        assertThat(result).isTrue();
        verify(terminalRepository, times(1)).findByPublicId(publicId);
    }

    /**
     * Tests pour la méthode existsByPublicId pour vérifier qu'elle retourne false si le terminal n'est pas trouvé
     */
    @Test
    void existsByPublicId_shouldReturnFalseWhenNotFound() {
        when(terminalRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        boolean result = terminalService.existsByPublicId(UUID.randomUUID());

        assertThat(result).isFalse();
        verify(terminalRepository, times(1)).findByPublicId(any(UUID.class));
    }

    /**
     * Tests pour la méthode updateTerminal par publicId pour vérifier qu'elle met à jour et sauvegarde le terminal existant
     */
    @Test
    void updateTerminal_byPublicId_shouldUpdateExistingTerminalAndSave() {
        UUID existingPublicId = UUID.randomUUID();
        Long existingId = 10L;
        User existingUser = new User();
        existingUser.setIdUser(1L);

        Terminal existingTerminal = new Terminal();
        existingTerminal.setIdTerminal(existingId);
        existingTerminal.setPublicId(existingPublicId);
        existingTerminal.setNameTerminal("Old Name");
        existingTerminal.setUser(existingUser);
        existingTerminal.setStatusTerminal(TerminalStatus.LIBRE);

        Terminal updatedDetails = new Terminal();
        updatedDetails.setNameTerminal("New Name");
        updatedDetails.setStatusTerminal(TerminalStatus.OCCUPEE);
        updatedDetails.setPublicId(UUID.randomUUID());
        updatedDetails.setIdTerminal(99L);

        when(terminalRepository.findByPublicId(existingPublicId)).thenReturn(Optional.of(existingTerminal));
        when(terminalRepository.save(any(Terminal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Terminal result = terminalService.updateTerminal(existingPublicId, updatedDetails);

        ArgumentCaptor<Terminal> terminalCaptor = ArgumentCaptor.forClass(Terminal.class);
        verify(terminalRepository).save(terminalCaptor.capture());
        Terminal savedTerminal = terminalCaptor.getValue();

        assertThat(savedTerminal.getIdTerminal()).isEqualTo(existingId);
        assertThat(savedTerminal.getPublicId()).isEqualTo(existingPublicId);
        assertThat(savedTerminal.getNameTerminal()).isEqualTo("New Name");
        assertThat(savedTerminal.getUser()).isEqualTo(existingUser);

        assertThat(result).isEqualTo(savedTerminal);
    }

    /**
     * Tests pour la méthode updateTerminal par publicId pour vérifier qu'elle utilise l'utilisateur existant si aucun nouvel utilisateur n'est fourni
     */
    @Test
    void updateTerminal_byPublicId_shouldUseProvidedUserIfNotNull() {
        UUID existingPublicId = UUID.randomUUID();
        Long existingId = 10L;
        User existingUser = new User();
        existingUser.setIdUser(1L);
        User newUser = new User();
        newUser.setIdUser(2L);

        Terminal existingTerminal = new Terminal();
        existingTerminal.setIdTerminal(existingId);
        existingTerminal.setPublicId(existingPublicId);
        existingTerminal.setUser(existingUser);

        Terminal updatedDetails = new Terminal();
        updatedDetails.setUser(newUser);

        when(terminalRepository.findByPublicId(existingPublicId)).thenReturn(Optional.of(existingTerminal));
        when(terminalRepository.save(any(Terminal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        terminalService.updateTerminal(existingPublicId, updatedDetails);

        ArgumentCaptor<Terminal> terminalCaptor = ArgumentCaptor.forClass(Terminal.class);
        verify(terminalRepository).save(terminalCaptor.capture());
        Terminal savedTerminal = terminalCaptor.getValue();

        assertThat(savedTerminal.getUser()).isEqualTo(newUser);
    }

    /**
     * Tests pour la méthode updateTerminal par publicId pour vérifier qu'elle lance une exception si le terminal n'est pas trouvé
     */
    @Test
    void updateTerminal_byPublicId_shouldThrowExceptionWhenNotFound() {
        UUID nonExistentPublicId = UUID.randomUUID();
        when(terminalRepository.findByPublicId(nonExistentPublicId)).thenReturn(Optional.empty());

        Terminal updatedDetails = new Terminal();
        updatedDetails.setNameTerminal("New Name");

        assertThrows(IllegalArgumentException.class, () ->
                terminalService.updateTerminal(nonExistentPublicId, updatedDetails)
        );
        verify(terminalRepository, times(1)).findByPublicId(nonExistentPublicId);
        verify(terminalRepository, never()).save(any(Terminal.class));
    }

    /**
     * Tests pour la méthode setOccupiedByPublicId pour vérifier qu'elle met à jour le statut et l'occupation du terminal existant
     */
    @Test
    void setOccupiedByPublicId_shouldUpdateStatusAndOccupiedIfTerminalExists() {
        Terminal terminalToUpdate = new Terminal();
        terminalToUpdate.setIdTerminal(id);
        terminalToUpdate.setPublicId(publicId);
        terminalToUpdate.setStatusTerminal(TerminalStatus.LIBRE);
        terminalToUpdate.setOccupied(false);

        when(terminalRepository.findByPublicId(publicId)).thenReturn(Optional.of(terminalToUpdate));
        when(terminalRepository.save(any(Terminal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        terminalService.setOccupiedByPublicId(publicId, TerminalStatus.HORS_SERVICE, true);

        ArgumentCaptor<Terminal> terminalCaptor = ArgumentCaptor.forClass(Terminal.class);
        verify(terminalRepository, times(1)).findByPublicId(publicId);
        verify(terminalRepository, times(1)).save(terminalCaptor.capture());

        Terminal savedTerminal = terminalCaptor.getValue();
        assertThat(savedTerminal.getStatusTerminal()).isEqualTo(TerminalStatus.HORS_SERVICE);
        assertThat(savedTerminal.getOccupied()).isTrue();
    }

    /**
     * Tests pour la méthode setOccupiedByPublicId pour vérifier qu'elle ne fait rien si le terminal n'est pas trouvé
     */
    @Test
    void setOccupiedByPublicId_shouldDoNothingIfTerminalNotFound() {
        when(terminalRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        terminalService.setOccupiedByPublicId(UUID.randomUUID(), TerminalStatus.LIBRE, false);

        verify(terminalRepository, times(1)).findByPublicId(any(UUID.class));
        verify(terminalRepository, never()).save(any(Terminal.class)); // Assure que save n'a pas été appelé
    }

    /**
     * Tests pour la méthode getAllTerminalStatuses pour vérifier qu'elle retourne tous les statuts de terminal
     */
    @Test
    void getAllTerminalStatuses_shouldReturnAllEnumValues() {
        List<TerminalStatus> expectedStatuses = Arrays.asList(TerminalStatus.values());

        List<TerminalStatus> result = terminalService.getAllTerminalStatuses();

        assertThat(result).containsExactlyInAnyOrderElementsOf(expectedStatuses);
        assertThat(result).hasSize(TerminalStatus.values().length);
    }

    /**
     * Tests pour la méthode searchTerminals pour vérifier qu'elle retourne les terminaux filtrés
     */
    @Test
    void testSearchTerminals_shouldReturnFilteredTerminals() {
        // Arrange
        BigDecimal minPrice = BigDecimal.valueOf(10.00);
        BigDecimal maxPrice = BigDecimal.valueOf(100.00);
        double minRating = 3.0;
        boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        Terminal terminal1 = new Terminal();
        terminal1.setPrice(BigDecimal.valueOf(50.00));
        terminal1.setOccupied(false);

        Terminal terminal2 = new Terminal();
        terminal2.setPrice(BigDecimal.valueOf(150.00));
        terminal2.setOccupied(true);

        List<Terminal> expectedTerminals = List.of(terminal1);
        when(terminalRepository.searchTerminals(
                minPrice, maxPrice, minRating, occupied, startDate, endDate))
                .thenReturn(expectedTerminals);

        // Act
        List<Terminal> result = terminalService.searchTerminals(
                minPrice, maxPrice, minRating, occupied, startDate, endDate);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isBetween(minPrice, maxPrice);
        assertThat(result.get(0).getOccupied()).isEqualTo(occupied);
        verify(terminalRepository).searchTerminals(
                minPrice, maxPrice, minRating, occupied, startDate, endDate);
    }

    /**
     * Tests pour la méthode getTerminalByPublicId pour vérifier qu'elle retourne le terminal lorsqu'il est trouvé
     */
    @Test
    void testGetTerminalByPublicId_shouldReturnTerminalWhenFound() {
        // Arrange
        UUID publicId = UUID.randomUUID();
        Terminal expectedTerminal = new Terminal();
        expectedTerminal.setPublicId(publicId);
        when(terminalRepository.findByPublicId(publicId)).thenReturn(Optional.of(expectedTerminal));

        // Act
        Terminal result = terminalService.getTerminalByPublicId(publicId);

        // Assert
        assertThat(result).isEqualTo(expectedTerminal);
        verify(terminalRepository).findByPublicId(publicId);
    }

    /**
     * Tests pour la méthode getTerminalByPublicId pour vérifier qu'elle lance une exception lorsque le terminal n'est pas trouvé
     */
    @Test
    void testGetTerminalByPublicId_shouldThrowExceptionWhenNotFound() {
        // Arrange
        UUID publicId = UUID.randomUUID();
        when(terminalRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
                terminalService.getTerminalByPublicId(publicId)
        );
        verify(terminalRepository).findByPublicId(publicId);
    }

    /**
     * Tests pour la méthode saveTerminal pour vérifier qu'elle sauvegarde et retourne le terminal
     */
    @Test
    void testSaveTerminal_shouldSaveAndReturnTerminal() {
        // Arrange
        Terminal terminalToSave = new Terminal();
        terminalToSave.setNameTerminal("New Terminal");
        terminalToSave.setPublicId(UUID.randomUUID());

        Terminal savedTerminal = new Terminal();
        savedTerminal.setIdTerminal(1L);
        savedTerminal.setNameTerminal("New Terminal");
        savedTerminal.setPublicId(terminalToSave.getPublicId());

        when(terminalRepository.save(terminalToSave)).thenReturn(savedTerminal);

        // Act
        Terminal result = terminalService.saveTerminal(terminalToSave);

        // Assert
        assertThat(result).isEqualTo(savedTerminal);
        verify(terminalRepository).save(terminalToSave);
    }

    /**
     * Tests pour la méthode saveTerminal pour vérifier qu'elle lance une exception lorsque la sauvegarde échoue
     */
    @Test
    void testSaveTerminal_shouldThrowExceptionWhenSaveFails() {
        // Arrange
        Terminal terminalToSave = new Terminal();
        when(terminalRepository.save(terminalToSave)).thenThrow(new RuntimeException("Save failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                terminalService.saveTerminal(terminalToSave)
        );
        verify(terminalRepository).save(terminalToSave);
    }

    /**
     * Tests pour la méthode deleteTerminalByPublicId pour vérifier qu'elle supprime le terminal lorsqu'il est trouvé
     */
    @Test
    void testDeleteTerminalByPublicId_shouldNotThrowExceptionWhenTerminalNotFound() {
        // Arrange
        UUID publicId = UUID.randomUUID();
        doNothing().when(terminalRepository).deleteTerminalByPublicId(publicId);

        // Act & Assert
        assertDoesNotThrow(() -> terminalService.deleteTerminalByPublicId(publicId));
        verify(terminalRepository, times(1)).deleteTerminalByPublicId(publicId);
    }

    /**
     * Tests pour la méthode deleteTerminalByPublicId pour vérifier qu'elle lance une exception lorsque la suppression échoue
     */
    @Test
    void testFindByPlace_shouldReturnMatchingTerminals() {
        // Arrange
        Place place = new Place();
        List<Terminal> expectedTerminals = List.of(new Terminal(), new Terminal());
        when(terminalRepository.findByPlace(place)).thenReturn(expectedTerminals);

        // Act
        List<Terminal> result = terminalService.findByPlace(place);

        // Assert
        assertThat(result).hasSize(2);
        verify(terminalRepository).findByPlace(place);
    }

    /**
     * Tests pour la méthode findByStatus pour vérifier qu'elle retourne les terminaux avec le statut spécifié
     */
    @Test
    void testFindByStatus_shouldReturnMatchingTerminals() {
        // Arrange
        TerminalStatus status = TerminalStatus.LIBRE;
        List<Terminal> expectedTerminals = List.of(new Terminal(), new Terminal());
        when(terminalRepository.findByStatusTerminal(status)).thenReturn(expectedTerminals);

        // Act
        List<Terminal> result = terminalService.findByStatus(status);

        // Assert
        assertThat(result).hasSize(2);
        verify(terminalRepository).findByStatusTerminal(status);
    }

    /**
     * Tests pour la méthode findByOccupied pour vérifier qu'elle retourne les terminaux avec le statut d'occupation spécifié
     */
    @Test
    void testFindByOccupied_shouldReturnMatchingTerminals() {
        // Arrange
        boolean occupied = true;
        List<Terminal> expectedTerminals = List.of(new Terminal(), new Terminal());
        when(terminalRepository.findByOccupied(occupied)).thenReturn(expectedTerminals);

        // Act
        List<Terminal> result = terminalService.findByOccupied(occupied);

        // Assert
        assertThat(result).hasSize(2);
        verify(terminalRepository).findByOccupied(occupied);
    }

    /**
     * Tests pour la méthode getTerminalsByPlace pour vérifier qu'elle retourne les terminaux associés au lieu spécifié
     */
    @Test
    void testGetTerminalsByPlace_shouldReturnMatchingTerminals() {
        // Arrange
        UUID placeId = UUID.randomUUID();
        List<Terminal> expectedTerminals = List.of(new Terminal(), new Terminal());
        when(terminalRepository.findTerminalByPlace_PublicId(placeId)).thenReturn(expectedTerminals);

        // Act
        List<Terminal> result = terminalService.getTerminalsByPlace(placeId);

        // Assert
        assertThat(result).hasSize(2);
        verify(terminalRepository).findTerminalByPlace_PublicId(placeId);
    }

}
