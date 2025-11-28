package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.terminal.TerminalCreateDTO;
import com.electricitybusiness.api.dto.terminal.TerminalSearchDTO;
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
import java.util.stream.Collectors;

import static java.lang.Math.cos;
import static java.lang.Math.sqrt;

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


    //V1
    // Surement à supprimer plus tard

/*    @Transactional(readOnly = true)
    public List<Terminal> getNearbyTerminals(BigDecimal longitude, BigDecimal latitude, double radius) {
        List<Terminal> liste_Terminal = terminalRepository.findAll();
        List<Terminal> liste_Terminal_resultat = new ArrayList<>();

        for (Terminal Terminal : liste_Terminal) {
            double longitude_Terminal = Terminal.getLongitude().doubleValue();
            double latitude_Terminal = Terminal.getLatitude().doubleValue();

            double x = (longitude.doubleValue() - longitude_Terminal) * cos((latitude.doubleValue() + latitude_Terminal) / 2);
            double y = latitude_Terminal - latitude.doubleValue();
            double z = sqrt((x * x) + (y * y));
            double d = 1.852 * 60 * z;

            if (d <= radius) {

                liste_Terminal_resultat.add(Terminal);

            }
        }

        return liste_Terminal_resultat;

    }*/

    //V2
    // Surement à supprimer plus tard

/*    @Transactional(readOnly = true)
    public List<Terminal> getNearbyAndAvaibleTerminals(BigDecimal longitude, BigDecimal latitude, double radius, boolean occupied) {
        List<Terminal> liste_Terminal = terminalRepository.findAll();

        if (occupied) {
            liste_Terminal.removeIf(Terminal -> !Boolean.TRUE.equals(Terminal.getOccupied()));
        }
        List<Terminal> liste_Terminal_resultat = new ArrayList<>();

        for (Terminal Terminal : liste_Terminal) {
            double longitude_Terminal = Terminal.getLongitude().doubleValue();
            double latitude_Terminal = Terminal.getLatitude().doubleValue();

            double x = (longitude.doubleValue() - longitude_Terminal) * cos((latitude.doubleValue() + latitude_Terminal) / 2);
            double y = latitude_Terminal - latitude.doubleValue();
            double z = sqrt((x * x) + (y * y));
            double d = 1.852 * 60 * z;

            if (d <= radius) {

                liste_Terminal_resultat.add(Terminal);

            }
        }

        return liste_Terminal_resultat;

    }*/

    //V3
    // Surement à supprimer plus tard

/*    @Transactional(readOnly = true)
    public List<Terminal> getNearbyAndAvaibleInPeriodTerminals(BigDecimal longitude, BigDecimal latitude, double radius, boolean occupied, LocalDateTime startingDate, LocalDateTime endingDate) {
        List<Terminal> liste_terminal = terminalRepository.findAll();
        List<Terminal> liste_terminal_resultat = new ArrayList<>();

        for (Terminal terminal : liste_terminal) {
            double longitude_terminal = terminal.getLongitude().doubleValue();
            double latitude_terminal = terminal.getLatitude().doubleValue();

            double x = (longitude.doubleValue() - longitude_terminal) * cos(Math.toRadians((latitude.doubleValue() + latitude_terminal) / 2));
            double y = latitude_terminal - latitude.doubleValue();
            double z = sqrt((x * x) + (y * y));
            double d = 1.852 * 60 * z;  // distance en km

            if (d <= radius) {
                boolean isOccupiedOnPeriod = false;

                List<Booking> bookings = bookingRepository.findByTerminal(terminal);
                if (startingDate != null && endingDate != null) {
                    for (Booking booking : bookings) {
                        LocalDateTime resStart = booking.getStartingDate();
                        LocalDateTime resEnd = booking.getEndingDate();

                        // Vérifie si chevauchement avec la période demandée
                        if (!(endingDate.isBefore(resStart) || startingDate.isAfter(resEnd))) {
                            isOccupiedOnPeriod = true;
                            break;
                        }
                    }
                } else {
                    // Pas de période donnée : considère la borne occupée si une réservation est en cours maintenant
                    LocalDateTime now = LocalDateTime.now();
                    for (Booking booking : bookings) {
                        if (!now.isBefore(booking.getStartingDate()) && !now.isAfter(booking.getEndingDate())) {
                            isOccupiedOnPeriod = true;
                            break;
                        }
                    }
                }

                // Ajoute la borne si son occupation correspond à ce qu'on cherche (occupee ou libre)
                if (isOccupiedOnPeriod == occupied) {
                    liste_terminal_resultat.add(terminal);
                }
            }
        }
        return liste_terminal_resultat;
    }*/


    // Surement à supprimer plus tard


/*
    public List<Terminal> findAvailableTerminals() {
        return terminalRepository.findAvailableTerminals();
    }
*/

    // Surement à supprimer plus tard

/*    public List<Terminal> findTerminalsAvailableInRadius(BigDecimal longitude, BigDecimal latitude, double radius, boolean occupied) {
        return terminalRepository.findTerminalsInRadius(longitude, latitude, radius, occupied);
    }*/

    // fonction non modulaire
    // Surement à supprimer plus tard

/*    public List<Terminal> findTerminalsAvailableInPeriod(
            boolean occupied,
            LocalDateTime startingDate,
            LocalDateTime endingDate) {
        return terminalRepository.findTerminalsAvailableInPeriod(
                occupied, startingDate, endingDate);
    }*/


    // fonction non modulaire
    // Surement à supprimer plus tard

/*
    public List<Terminal> findTerminalsAvailableInRadiusAndPeriod(
            BigDecimal longitude,
            BigDecimal latitude,
            double radius,
            boolean occupied,
            LocalDateTime startingDate,
            LocalDateTime endingDate) {
        return terminalRepository.findTerminalsAvailableInRadiusAndPeriod(
                longitude, latitude, radius, occupied, startingDate, endingDate);
    }
*/


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





/*    @Transactional(readOnly = true)
    public List<Terminal> searchTerminals(TerminalSearchDTO searchDTO) {

        // Validation métier
        if (!searchDTO.hasAnyCriteria()) {
            throw new IllegalArgumentException(
                    "Au moins un critère de recherche doit être fourni"
            );
        }

        if (!searchDTO.isValidDateRange()) {
            throw new IllegalArgumentException(
                    "La date de début doit être antérieure à la date de fin"
            );
        }

        // Recherche géographique + disponibilité
        if (searchDTO.hasGeographicCriteria() && searchDTO.hasAvailabilityCriteria()) {
            return terminalRepository.searchTerminals(
                    searchDTO.getLongitude(),
                    searchDTO.getLatitude(),
                    searchDTO.getRadiusOrDefault(),
                    searchDTO.getOccupied(),
                    searchDTO.getStartingDate(),
                    searchDTO.getEndingDate()
            );
        }

        // Recherche géographique seule
        if (searchDTO.hasGeographicCriteria()) {
            List<Terminal> terminals = terminalRepository.findWithinRadius(
                    searchDTO.getLatitude(),
                    searchDTO.getLongitude(),
                    searchDTO.getRadiusOrDefault()
            );

            // Filtrage optionnel par état d'occupation
            if (searchDTO.getOccupied() != null) {
                return terminals.stream()
                        .filter(t -> t.getOccupied().equals(searchDTO.getOccupied()))
                        .collect(Collectors.toList());
            }

            return terminals;
        }

        // Recherche par disponibilité seule
        if (searchDTO.hasAvailabilityCriteria()) {
            return terminalRepository.findAvailableTerminals(
                    searchDTO.getStartingDate(),
                    searchDTO.getEndingDate()
            );
        }

        // Filtrage par état d'occupation seul
        if (searchDTO.getOccupied() != null) {
            return terminalRepository.findByOccupied(searchDTO.getOccupied());
        }

        // Ne devrait jamais arriver (déjà vérifié par hasAnyCriteria())
        return List.of();
    }*/




    // Surement à supprimer plus tard

/*    public List<Terminal> searchTerminalsWithCriteria(
            BigDecimal longitude,
            BigDecimal latitude,
            Double radius,
            Boolean occupied,
            LocalDateTime startingDate,
            LocalDateTime endingDate) {

        return terminalRepository.searchTerminalsWithCriteria(
                longitude, latitude, radius, occupied, startingDate, endingDate
        );
    }*/

    @Transactional(readOnly = true)
    public List<Terminal> getTerminalsByPlace(UUID place) { return terminalRepository.findTerminalByPlace_PublicId(place); }

    public void deleteTerminalByPublicId(UUID publicId) {
        terminalRepository.deleteTerminalByPublicId(publicId);
    }

    @Transactional(readOnly = true)
    public boolean existsByPublicId(UUID publicId) {
        return terminalRepository.findByPublicId(publicId).isPresent();
    }

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

    public List<TerminalStatus> getAllTerminalStatuses() {
        List<TerminalStatus> statuses = new ArrayList<>();
        for (TerminalStatus status : TerminalStatus.values()) {
            statuses.add(status);
        }
        return statuses;
    }

    @Transactional
    public void setOccupiedByPublicId(UUID publicId, TerminalStatus status, Boolean occupied) {
        terminalRepository.findByPublicId(publicId).ifPresent(terminal -> {
            terminal.setStatusTerminal(status);
            terminal.setOccupied(occupied);
            terminalRepository.save(terminal);
        });
    }

    @Transactional(readOnly = true)
    public Terminal getTerminalByPublicId(UUID publicId) {
        return terminalRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Terminal with publicId not found: " + publicId));
    }
}
