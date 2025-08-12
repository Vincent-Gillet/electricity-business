package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Booking;
import com.electricitybusiness.api.model.Place;
import com.electricitybusiness.api.model.Terminal;
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
     * @param Terminal La Terminal à enregistrer
     * @return La Terminal enregistrée
     */
    public Terminal saveTerminal(Terminal Terminal) {
        return terminalRepository.save(Terminal);
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
    public List<Terminal> findByStatus(Terminal statusTerminal) {
        return terminalRepository.findByStatusTerminal(statusTerminal);
    }

    /**
     * Récupère une Terminal par son occupation.
     * @param occupied La Terminal occupée à récupérer
     * @return Une liste de Terminals associées à cette occupation
     */
    @Transactional(readOnly = true)
    public List<Terminal> findByOccupied(Terminal occupied) {
        return terminalRepository.findByoccupied(occupied);
    }

    /**
     * Récupère une Terminal par son lieu et son état.
     * @param place Le lieu de la Terminal à récupérer
     * @param etatTerminal L'état de la Terminal à récupérer
     * @return Une liste de Terminals associées à ce lieu et cet état
     */
    @Transactional(readOnly = true)
    public List<Terminal> findByPlaceAndStatus(Place place, Terminal etatTerminal) {
        return terminalRepository.findByPlaceAndStatusTerminal(place, etatTerminal);
    }


    //V1

    @Transactional(readOnly = true)
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

    }

    //V2

    @Transactional(readOnly = true)
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

    }

    //V3

    @Transactional(readOnly = true)
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
    }




    public List<Terminal> findAvailableTerminals() {
        return terminalRepository.findAvailableTerminals();
    }


    public List<Terminal> findTerminalsAvailableInRadius(BigDecimal longitude, BigDecimal latitude, double radius, boolean occupied) {
        return terminalRepository.findTerminalsInRadius(longitude, latitude, radius, occupied);
    }

    // fonction non modulaire
    public List<Terminal> findTerminalsAvailableInPeriod(
            boolean occupied,
            LocalDateTime startingDate,
            LocalDateTime endingDate) {
        return terminalRepository.findTerminalsAvailableInPeriod(
                occupied, startingDate, endingDate);
    }


    // fonction non modulaire
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






    public List<Terminal> searchTerminalsWithCriteria(
            BigDecimal longitude,
            BigDecimal latitude,
            Double radius,
            Boolean occupied,
            LocalDateTime startingDate,
            LocalDateTime endingDate) {

        return terminalRepository.searchTerminalsWithCriteria(
                longitude, latitude, radius, occupied, startingDate, endingDate
        );
    }
}
