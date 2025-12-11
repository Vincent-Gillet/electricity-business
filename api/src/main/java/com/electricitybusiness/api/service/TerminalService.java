package com.electricitybusiness.api.service;

import com.electricitybusiness.api.exception.ResourceNotFoundException;
import com.electricitybusiness.api.model.*;
import com.electricitybusiness.api.repository.BookingRepository;
import com.electricitybusiness.api.repository.TerminalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour gérer les opérations liées aux Terminals.
 * Fournit des méthodes pour récupérer, créer, mettre à jour et supprimer des Terminals.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TerminalService {

    private final TerminalRepository terminalRepository;
    private final BookingRepository bookingRepository;

    /**
     * Récupère tous les Terminals.
     * @return Une liste de toutes les Terminals
     */
    @Transactional(readOnly = true)
    public List<Terminal> getAllTerminals() {
        return terminalRepository.findAll();
    }

    /**
     * Récupère un vehicule par son ID.
     * @param id L'identifiant du vehicule à récupérer
     * @return Un Optional contenant le vehicule si trouvé, sinon vide
     */
    public Optional<Terminal> getTerminalById(Long id) {
        return terminalRepository.findById(id);
    }

    /**
     * Crée un nouveau vehicule.
     * @param terminal La Terminal à enregistrer
     * @return La Terminal enregistrée
     */
    public Terminal saveTerminal(Terminal terminal) {
        return terminalRepository.save(terminal);
    }

    /**
     * Met à jour un vehicule existant.
     * @param id L'identifiant du vehicule à mettre à jour
     * @param Terminal La Terminal avec les nouvelles informations
     * @return La Terminal mise à jour
     */
    public Terminal updateTerminal(Long id, Terminal Terminal) {
        Terminal.setIdTerminal(id);
        return terminalRepository.save(Terminal);
    }

    /**
     * Supprime un utilisateur.
     * @param id L'identifiant de la Terminal à supprimer
     */
    public void deleteTerminalById(Long id) {
        terminalRepository.deleteById(id);
    }

    /**
     * Vérifie si une Terminal existe.
     * @param id L'identifiant de la Terminal à vérifier
     * @return true si la Terminal existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return terminalRepository.existsById(id);
    }

    /**
     * Récupère une Terminal par son lieu.
     * @param place Le lieu de la Terminal à récupérer
     * @return Une liste de Terminals associées à ce lieu
     */
    @Transactional(readOnly = true)
    public List<Terminal> findByPlace(Place place) {
        return terminalRepository.findByPlace(place);
    }

    /**
     * Récupère une Terminal par son état.
     * @param statusTerminal L'état de la Terminal à récupérer
     * @return Une liste de Terminals associées à cet état
     */
    @Transactional(readOnly = true)
    public List<Terminal> findByStatus(TerminalStatus statusTerminal) {
        return terminalRepository.findByStatusTerminal(statusTerminal);
    }

    /**
     * Récupère une Terminal par son occupation.
     * @param occupied La Terminal occupée à récupérer
     * @return Une liste de Terminals associées à cette occupation
     */
    @Transactional(readOnly = true)
    public List<Terminal> findByOccupied(Boolean occupied) {
        return terminalRepository.findByOccupied(occupied);
    }

    /**
     * Récupère une Terminal par son lieu et son état.
     * @param place Le lieu de la Terminal à récupérer
     * @param etatTerminal L'état de la Terminal à récupérer
     * @return Une liste de Terminals associées à ce lieu et cet état
     */
    @Transactional(readOnly = true)
    public List<Terminal> findByPlaceAndStatus(Place place, TerminalStatus etatTerminal) {
        return terminalRepository.findByPlaceAndStatusTerminal(place, etatTerminal);
    }

    // Méthode 10
    public List<Terminal> searchTerminals(
            BigDecimal longitude,
            BigDecimal latitude,
            double radius,
            boolean occupied,
            LocalDateTime startingDate,
            LocalDateTime endingDate) {
        return terminalRepository.searchTerminals(
                longitude, latitude, radius, occupied, startingDate, endingDate);
    }

    @Transactional(readOnly = true)
    public List<Terminal> getTerminalsByPlace(UUID place) { return terminalRepository.findTerminalByPlace_PublicId(place); }

    /**
     * Supprime une Terminal.
     * @param publicId L'identifiant public de la Terminal à supprimer
     */
    public void deleteTerminalByPublicId(UUID publicId) {
        terminalRepository.deleteTerminalByPublicId(publicId);
    }

    /**
     * Vérifie si une Terminal existe.
     * @param publicId L'identifiant public de la Terminal à vérifier
     * @return true si la Terminal existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsByPublicId(UUID publicId) {
        return terminalRepository.findByPublicId(publicId).isPresent();
    }

    /**
     * Met à jour une Terminal existant.
     * @param publicId L'identifiant de la Terminal à mettre à jour
     * @param terminal La Terminal avec les nouvelles informations
     * @return La Terminal mis à jour
     */
    public Terminal updateTerminal(UUID publicId, Terminal terminal) {
        Terminal existing = terminalRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Terminal with publicId not found: " + publicId));

        terminal.setIdTerminal(existing.getIdTerminal());
        terminal.setPublicId(existing.getPublicId());

        User existingUser = existing.getUser();
        if (terminal.getUser() == null) {
            terminal.setUser(existingUser);
        }
        return terminalRepository.save(terminal);
    }

    /**
     * Récupère tous les statuts de Terminal.
     * @return Une liste de tous les statuts de Terminal
     */
    public List<TerminalStatus> getAllTerminalStatuses() {
        List<TerminalStatus> statuses = new ArrayList<>();
        for (TerminalStatus status : TerminalStatus.values()) {
            statuses.add(status);
        }
        return statuses;
    }

    /**
     * Met à jour le statut et l'occupation d'une Terminal par son identifiant public.
     * @param publicId L'identifiant public de la Terminal à mettre à jour
     * @param status Le nouveau statut de la Terminal
     * @param occupied Le nouvel état d'occupation de la Terminal
     */
    public void setOccupiedByPublicId(UUID publicId, TerminalStatus status, Boolean occupied) {
        terminalRepository.findByPublicId(publicId).ifPresent(terminal -> {
            terminal.setStatusTerminal(status);
            terminal.setOccupied(occupied);
            terminalRepository.save(terminal);
        });
    }

    /**
     * Récupère une Terminal par son identifiant public.
     * @param publicId L'identifiant public de la Terminal à récupérer
     * @return La Terminal correspondante
     */
    @Transactional(readOnly = true)
    public Terminal getTerminalByPublicId(UUID publicId) {
        return terminalRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Terminal with publicId not found: " + publicId));
    }
}
