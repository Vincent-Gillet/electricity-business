package com.electricitybusiness.api.service;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TerminalServiceTest {

    @Mock
    private TerminalRepository terminalRepository;
    @Mock
    private BookingRepository bookingRepository; // Même si pas toutes les méthodes l'utilisent, c'est une dépendance

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
        // Assurez-vous d'initialiser d'autres champs si nécessaire
    }

    @Test
    void getTerminalById_shouldReturnTerminalWhenFound() {
        when(terminalRepository.findById(id)).thenReturn(Optional.of(mockTerminal));

        Optional<Terminal> result = terminalService.getTerminalById(id);

        assertThat(result).isPresent().contains(mockTerminal);
        verify(terminalRepository, times(1)).findById(id);
    }

    @Test
    void getTerminalById_shouldReturnEmptyWhenNotFound() {
        when(terminalRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Terminal> result = terminalService.getTerminalById(99L);

        assertThat(result).isEmpty();
        verify(terminalRepository, times(1)).findById(99L);
    }

    @Test
    void getAllTerminals_shouldReturnAllTerminals() {
        List<Terminal> terminals = Arrays.asList(mockTerminal, new Terminal());
        when(terminalRepository.findAll()).thenReturn(terminals);

        List<Terminal> result = terminalService.getAllTerminals();

        assertThat(result).isEqualTo(terminals);
        verify(terminalRepository, times(1)).findAll();
    }

    @Test
    void updateTerminal_byId_shouldSetIdAndSave() {
        Terminal updatedTerminalDetails = new Terminal();
        updatedTerminalDetails.setNameTerminal("Updated Name");
        updatedTerminalDetails.setPublicId(UUID.randomUUID()); // L'ID public ne devrait pas être affecté ici
        updatedTerminalDetails.setStatusTerminal(TerminalStatus.OCCUPEE);

        when(terminalRepository.save(any(Terminal.class))).thenReturn(updatedTerminalDetails);

        Terminal result = terminalService.updateTerminal(id, updatedTerminalDetails);

        // Capture de l'argument passé à save()
        // ArgumentCaptor<Terminal> terminalCaptor = ArgumentCaptor.forClass(Terminal.class);
        // verify(terminalRepository).save(terminalCaptor.capture());
        // Terminal savedTerminal = terminalCaptor.getValue();

        // On peut aussi directement vérifier l'objet retourné s'il est le même que celui sauvegardé
        // ou vérifier directement l'ID sur l'objet original si on sait qu'il est modifié en place
        assertThat(updatedTerminalDetails.getIdTerminal()).isEqualTo(id);
        assertThat(result.getNameTerminal()).isEqualTo("Updated Name");
        verify(terminalRepository, times(1)).save(updatedTerminalDetails);
    }

    @Test
    void existsByPublicId_shouldReturnTrueWhenFound() {
        when(terminalRepository.findByPublicId(publicId)).thenReturn(Optional.of(mockTerminal));

        boolean result = terminalService.existsByPublicId(publicId);

        assertThat(result).isTrue();
        verify(terminalRepository, times(1)).findByPublicId(publicId);
    }

    @Test
    void existsByPublicId_shouldReturnFalseWhenNotFound() {
        when(terminalRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        boolean result = terminalService.existsByPublicId(UUID.randomUUID());

        assertThat(result).isFalse();
        verify(terminalRepository, times(1)).findByPublicId(any(UUID.class));
    }

    @Test
    void updateTerminal_byPublicId_shouldUpdateExistingTerminalAndSave() {
        UUID existingPublicId = UUID.randomUUID();
        Long existingId = 10L;
        User existingUser = new User(); // Imaginez un utilisateur existant
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
        updatedDetails.setPublicId(UUID.randomUUID()); // Ceci ne devrait pas être utilisé
        updatedDetails.setIdTerminal(99L); // Ceci ne devrait pas être utilisé

        when(terminalRepository.findByPublicId(existingPublicId)).thenReturn(Optional.of(existingTerminal));
        when(terminalRepository.save(any(Terminal.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Retourne l'objet capturé

        Terminal result = terminalService.updateTerminal(existingPublicId, updatedDetails);

        // Capture de l'argument passé à save()
        ArgumentCaptor<Terminal> terminalCaptor = ArgumentCaptor.forClass(Terminal.class);
        verify(terminalRepository).save(terminalCaptor.capture());
        Terminal savedTerminal = terminalCaptor.getValue();

        assertThat(savedTerminal.getIdTerminal()).isEqualTo(existingId); // L'ID technique de l'existant est conservé
        assertThat(savedTerminal.getPublicId()).isEqualTo(existingPublicId); // L'ID public de l'existant est conservé
        assertThat(savedTerminal.getNameTerminal()).isEqualTo("New Name"); // Le nouveau nom est appliqué
        assertThat(savedTerminal.getUser()).isEqualTo(existingUser); // L'utilisateur existant est conservé car non fourni

        assertThat(result).isEqualTo(savedTerminal);
    }

    @Test
    void updateTerminal_byPublicId_shouldUseProvidedUserIfNotNull() {
        UUID existingPublicId = UUID.randomUUID();
        Long existingId = 10L;
        User existingUser = new User(); // Imaginez un utilisateur existant
        existingUser.setIdUser(1L);
        User newUser = new User(); // Imaginez un nouvel utilisateur
        newUser.setIdUser(2L);

        Terminal existingTerminal = new Terminal();
        existingTerminal.setIdTerminal(existingId);
        existingTerminal.setPublicId(existingPublicId);
        existingTerminal.setUser(existingUser);

        Terminal updatedDetails = new Terminal();
        updatedDetails.setUser(newUser); // Nouveau user fourni

        when(terminalRepository.findByPublicId(existingPublicId)).thenReturn(Optional.of(existingTerminal));
        when(terminalRepository.save(any(Terminal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        terminalService.updateTerminal(existingPublicId, updatedDetails);

        ArgumentCaptor<Terminal> terminalCaptor = ArgumentCaptor.forClass(Terminal.class);
        verify(terminalRepository).save(terminalCaptor.capture());
        Terminal savedTerminal = terminalCaptor.getValue();

        assertThat(savedTerminal.getUser()).isEqualTo(newUser); // Le nouvel utilisateur est appliqué
    }

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
        verify(terminalRepository, never()).save(any(Terminal.class)); // S'assurer que save n'est pas appelé
    }

    @Test
    void setOccupiedByPublicId_shouldUpdateStatusAndOccupiedIfTerminalExists() {
        Terminal terminalToUpdate = new Terminal();
        terminalToUpdate.setIdTerminal(id);
        terminalToUpdate.setPublicId(publicId);
        terminalToUpdate.setStatusTerminal(TerminalStatus.LIBRE);
        terminalToUpdate.setOccupied(false);

        when(terminalRepository.findByPublicId(publicId)).thenReturn(Optional.of(terminalToUpdate));
        when(terminalRepository.save(any(Terminal.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Pour retourner le terminal modifié

        terminalService.setOccupiedByPublicId(publicId, TerminalStatus.HORS_SERVICE, true);

        ArgumentCaptor<Terminal> terminalCaptor = ArgumentCaptor.forClass(Terminal.class);
        verify(terminalRepository, times(1)).findByPublicId(publicId);
        verify(terminalRepository, times(1)).save(terminalCaptor.capture());

        Terminal savedTerminal = terminalCaptor.getValue();
        assertThat(savedTerminal.getStatusTerminal()).isEqualTo(TerminalStatus.HORS_SERVICE);
        assertThat(savedTerminal.getOccupied()).isTrue();
    }

    @Test
    void setOccupiedByPublicId_shouldDoNothingIfTerminalNotFound() {
        when(terminalRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        terminalService.setOccupiedByPublicId(UUID.randomUUID(), TerminalStatus.LIBRE, false);

        verify(terminalRepository, times(1)).findByPublicId(any(UUID.class));
        verify(terminalRepository, never()).save(any(Terminal.class)); // Assure que save n'a pas été appelé
    }

    @Test
    void getAllTerminalStatuses_shouldReturnAllEnumValues() {
        List<TerminalStatus> expectedStatuses = Arrays.asList(TerminalStatus.values());

        List<TerminalStatus> result = terminalService.getAllTerminalStatuses();

        assertThat(result).containsExactlyInAnyOrderElementsOf(expectedStatuses);
        assertThat(result).hasSize(TerminalStatus.values().length);
    }
}
