package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.dto.terminal.TerminalDTO;
import com.electricitybusiness.api.model.Place;
import com.electricitybusiness.api.model.Terminal;
import com.electricitybusiness.api.model.TerminalStatus;
import com.electricitybusiness.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de gestion des opérations CRUD pour les Terminals.
 * Hérite de JpaRepository pour les opérations de base de données.
 */
@Repository
public interface TerminalRepository extends JpaRepository<Terminal,Long> {
    /** Trouve les terminaux associés à un lieu donné.
     *
     * @param place Le lieu pour lequel les terminaux doivent être trouvés.
     * @return Une liste de terminaux associés au lieu spécifié.
     */
    List<Terminal> findByPlace(Place place);

    /** Trouve les terminaux en fonction de leur statut.
     *
     * @param statusTerminal Le statut des terminaux à rechercher.
     * @return Une liste de terminaux correspondant au statut spécifié.
     */
    List<Terminal> findByStatusTerminal(TerminalStatus statusTerminal);

    /** Trouve les terminaux en fonction de leur statut d'occupation.
     *
     * @param occupied Le statut d'occupation des terminaux à rechercher.
     * @return Une liste de terminaux correspondant au statut d'occupation spécifié.
     */
    List<Terminal> findByOccupied(Boolean occupied);

    /** Trouve les terminaux associés à un lieu donné et à un statut spécifique.
     *
     * @param place Le lieu pour lequel les terminaux doivent être trouvés.
     * @param statusTerminal Le statut des terminaux à rechercher.
     * @return Une liste de terminaux associés au lieu et au statut spécifiés.
     */
    List<Terminal> findByPlaceAndStatusTerminal(Place place, TerminalStatus statusTerminal);

    /**
     * Recherche des terminaux en fonction de plusieurs critères.
     *
     * @param longitude    La longitude du point central pour la recherche de proximité.
     * @param latitude     La latitude du point central pour la recherche de proximité.
     * @param radius       Le rayon de recherche en kilomètres.
     * @param occupied     Le statut d'occupation du terminal (true pour occupé, false pour libre).
     * @param startingDate La date de début pour vérifier la disponibilité.
     * @param endingDate   La date de fin pour vérifier la disponibilité.
     * @return Une liste de terminaux correspondant aux critères de recherche.
     */
    @Query("""
    SELECT t FROM Terminal t
    WHERE t.statusTerminal = 'LIBRE'
          AND
        (:occupied IS NULL OR t.occupied = :occupied)
          AND (
              :latitude IS NULL OR :longitude IS NULL OR :radius IS NULL OR
              (6371 * 2 * ASIN(SQRT(
                  POWER(SIN((RADIANS(t.latitude) - RADIANS(:latitude)) / 2), 2)
                  + COS(RADIANS(:latitude)) * COS(RADIANS(t.latitude))
                  * POWER(SIN((RADIANS(t.longitude) - RADIANS(:longitude)) / 2), 2)
              ))) <= :radius
          )
          AND (
              :startingDate IS NULL OR :endingDate IS NULL OR
              NOT EXISTS (
                  SELECT 1 FROM Booking b
                  WHERE b.terminal = t
                  AND NOT (
                      b.endingDate <= :startingDate
                      OR b.startingDate >= :endingDate
                  )
              )
          )
    """)
    List<Terminal> searchTerminals(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") Double radius,
            @Param("occupied") Boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate
    );

    /**
     * Trouve les terminaux associés à un identifiant public de lieu donné.
     *
     * @param publicId L'identifiant public du lieu.
     * @return Une liste de terminaux associés au lieu spécifié.
     */
    List<Terminal> findTerminalByPlace_PublicId(UUID publicId);

    /**
     * Supprime un terminal en fonction de son identifiant public.
     *
     * @param publicId L'identifiant public du terminal à supprimer.
     */
    void deleteTerminalByPublicId(UUID publicId);

    /**
     * Vérifie si un terminal existe en fonction de son identifiant public.
     *
     * @param publicId L'identifiant public du terminal à vérifier.
     * @return true si le terminal existe, sinon false.
     */
    Optional<Terminal> findByPublicId(UUID publicId);
}

