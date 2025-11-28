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
public interface TerminalRepository extends JpaRepository<Terminal,Long>, TerminalRepositoryCustom {
    List<Terminal> findByPlace(Place place);

    List<Terminal> findByStatusTerminal(TerminalStatus statusTerminal);

    List<Terminal> findByOccupied(Boolean occupied);

    List<Terminal> findByPlaceAndStatusTerminal(Place place, TerminalStatus statusTerminal);

/*
    Terminal findByPublicId(UUID publicId);
*/

    // Récupérer toutes les Terminals disponibles (occupieds = 0)
/*    @Query(value = "SELECT DISTINCT t.* FROM terminals t " +
            "WHERE t.occupied = 0 ",
            nativeQuery = true)
    List<Terminal> findAvailableTerminals();*/

    // Récupérer toutes les Terminals dans le rayon
/*    @Query(value = "SELECT * FROM terminals t " +
            "WHERE t.occupied = :occupied " +
            "AND (6371 * 2 * ASIN(SQRT(POWER(SIN((RADIANS(t.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "+ COS(RADIANS(:latitude)) * COS(RADIANS(t.latitude)) * POWER(SIN((RADIANS(t.longitude) - RADIANS(:longitude)) / 2), 2)))) <= :radius",
            nativeQuery = true)
    List<Terminal> findTerminalsInRadius(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") double radius,
            @Param("occupied") boolean occupied
    );*/

    // Rechercher fonctionne non modulaire
/*    @Query(value = "SELECT DISTINCT t.* FROM terminals t " +
            "WHERE t.occupied = :occupied " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM bookings b " +
            "    WHERE b.id_terminal = t.id_terminal " +
            "    AND b.ending_date > :startingDate " +
            "    AND b.starting_date < :endingDate" +
            ")",
            nativeQuery = true)
    List<Terminal> findTerminalsAvailableInPeriod(
            @Param("occupied") boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate);*/

    // Rechercher fonctionne non modulaire
/*
    @Query(value = "SELECT DISTINCT t.* FROM terminals t " +
            "WHERE t.occupied = :occupied " +
            "AND (6371 * 2 * ASIN(SQRT(POWER(SIN((RADIANS(t.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "+ COS(RADIANS(:latitude)) * COS(RADIANS(t.latitude)) * POWER(SIN((RADIANS(t.longitude) - RADIANS(:longitude)) / 2), 2)))) <= :radius " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM bookings b " +
            "    WHERE b.id_terminal = t.id_terminal " +
            "    AND b.ending_date > :startingDate " +
            "    AND b.starting_date < :endingDate" +
            ")",
            nativeQuery = true)
    List<Terminal> findTerminalsAvailableInRadiusAndPeriod(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") double radius,
            @Param("occupied") boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate);
*/







/*    @Query("SELECT t FROM Terminal t " +
            "WHERE (:occupied IS NULL OR t.occupied = :occupied) " +
            "AND (" +
            "   :latitude IS NULL OR :longitude IS NULL OR :radius IS NULL OR " +
            "   (6371 * 2 * ASIN(SQRT(" +
            "       POWER(SIN((RADIANS(t.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "       + COS(RADIANS(:latitude)) * COS(RADIANS(t.latitude)) * POWER(SIN((RADIANS(t.longitude) - RADIANS(:longitude)) / 2), 2)" +
            "))) <= :radius) " +
            "AND (" +
            "   :startingDate IS NULL OR :endingDate IS NULL OR " +
            "   NOT EXISTS (" +
            "       SELECT 1 FROM Booking b " +
            "       WHERE b.terminal = t " +
            "       AND (" +
            "           (:startingDate BETWEEN b.startingDate AND b.endingDate) OR " +
            "           (:endingDate BETWEEN b.startingDate AND b.endingDate) OR " +
            "           (b.startingDate BETWEEN :startingDate AND :endingDate) OR " +
            "           (b.endingDate BETWEEN :startingDate AND :endingDate)" +            "       )" +
                ")" +
            ")"
            )
    List<Terminal> searchTerminals(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") Double radius,
            @Param("occupied") Boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate
    );*/

/*    @Query("SELECT t FROM Terminal t " +
            "WHERE (:occupied IS NULL OR t.occupied = :occupied) " +
            "AND (" +
            "   :latitude IS NULL OR :longitude IS NULL OR :radius IS NULL OR " +
            "   (6371 * 2 * ASIN(SQRT(" +
            "       POWER(SIN((RADIANS(t.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "       + COS(RADIANS(:latitude)) * COS(RADIANS(t.latitude)) * POWER(SIN((RADIANS(t.longitude) - RADIANS(:longitude)) / 2), 2)" +
            "))) <= :radius) " +
            "AND (" +
            "   :startingDate IS NULL OR :endingDate IS NULL OR " +
            "   NOT EXISTS (" +
            "       SELECT 1 FROM Booking b " +
            "       WHERE b.terminal.idTerminal = t.idTerminal AND " +
            "       (" +
            "           (b.startingDate <= :endingDate AND b.endingDate >= :startingDate)" +
            "       )" +
            "   )" +
            ")")
    List<Terminal> searchTerminals(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") Double radius,
            @Param("occupied") Boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate
    );*/

/*    @Query(value =
            "SELECT t.* FROM terminals t " +
                    "WHERE (:occupied IS NULL OR t.occupied = :occupied) " +
                    "AND (:latitude IS NULL OR :longitude IS NULL OR :radius IS NULL OR " +
                    "   (6371 * 2 * ASIN(SQRT(" +
                    "       POWER(SIN((RADIANS(t.latitude) - RADIANS(:latitude)) / 2), 2) " +
                    "       + COS(RADIANS(:latitude)) * COS(RADIANS(t.latitude)) * POWER(SIN((RADIANS(t.longitude) - RADIANS(:longitude)) / 2), 2)" +
                    "))) <= :radius) " +
                    "AND (:startingDate IS NULL OR :endingDate IS NULL OR " +
                    "   NOT EXISTS (" +
                    "       SELECT 1 FROM bookings b " +
                    "       WHERE b.terminal = t.id_terminal AND " +  // Utilise le bon nom de colonne
                    "       (" +
                    "           (b.starting_date <= :endingDate AND b.ending_date >= :startingDate)" +
                    "       )" +
                    "   ))", nativeQuery = true)
    List<Terminal> searchTerminals(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") Double radius,
            @Param("occupied") Boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate
    );*/

/*    @Query("SELECT t FROM Terminal t LEFT JOIN FETCH t.bookings b " +
            "WHERE (:occupied IS NULL OR t.occupied = :occupied) " +
            "AND (:latitude IS NULL OR :longitude IS NULL OR :radius IS NULL OR " +
            "   (6371 * 2 * ASIN(SQRT(" +
            "       POWER(SIN((RADIANS(t.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "       + COS(RADIANS(:latitude)) * COS(RADIANS(t.latitude)) * POWER(SIN((RADIANS(t.longitude) - RADIANS(:longitude)) / 2), 2)" +
            "))) <= :radius) " +
            "AND (:startingDate IS NULL OR :endingDate IS NULL OR b IS NULL OR " +
            "   NOT (b.startingDate <= :endingDate AND b.endingDate >= :startingDate))")
    List<Terminal> searchTerminals(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") Double radius,
            @Param("occupied") Boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate
    );*/

/*
    @Query("SELECT t FROM Terminal t " +
            "WHERE (:occupied IS NULL OR t.occupied = :occupied) " +
            "AND (:latitude IS NULL OR :longitude IS NULL OR :radius IS NULL OR " +
            "   (6371 * 2 * ASIN(SQRT(" +
            "       POWER(SIN((RADIANS(t.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "       + COS(RADIANS(:latitude)) * COS(RADIANS(t.latitude)) * POWER(SIN((RADIANS(t.longitude) - RADIANS(:longitude)) / 2), 2)" +
            "))) <= :radius) " +
            "AND (:startingDate IS NULL OR :endingDate IS NULL OR " +
            "   NOT EXISTS (" +
            "       SELECT 1 FROM Booking b " +
            "       WHERE b.terminal.idTerminal = t.idTerminal " +
            "       AND b.startingDate < :endingDate " +
            "       AND b.endingDate > :startingDate" +
            "   ))")
    List<Terminal> searchTerminals(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") Double radius,
            @Param("occupied") Boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate
    );
*/


/*    @Query("SELECT t FROM Terminal t " +
            "WHERE (:occupied IS NULL OR t.occupied = :occupied) " +
            "AND (:latitude IS NULL OR :longitude IS NULL OR :radius IS NULL OR " +
            "   (6371 * 2 * ASIN(SQRT(" +
            "       POWER(SIN((RADIANS(t.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "       + COS(RADIANS(:latitude)) * COS(RADIANS(t.latitude)) * " +
            "       POWER(SIN((RADIANS(t.longitude) - RADIANS(:longitude)) / 2), 2)" +
            "))) <= :radius) " +
            "AND ((:startingDate IS NULL OR :endingDate IS NULL) OR " +
            "   t.idTerminal NOT IN (" +
            "       SELECT b.terminal.idTerminal FROM Booking b " +
            "       WHERE b.startingDate < :endingDate " +
            "       AND b.endingDate > :startingDate" +
            "   ))")
    List<Terminal> searchTerminals(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") Double radius,
            @Param("occupied") Boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate
    );*/


    // Méthode 10
    @Query("""
    SELECT t FROM Terminal t
    WHERE (:occupied IS NULL OR t.occupied = :occupied)
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


/*    @Query(value = """
        SELECT t.*
        FROM terminals t
        WHERE (
            6371 * 2 * ASIN(SQRT(
                POWER(SIN((RADIANS(t.latitude) - RADIANS(:lat)) / 2), 2)
                + COS(RADIANS(:lat)) * COS(RADIANS(t.latitude))
                * POWER(SIN((RADIANS(t.longitude) - RADIANS(:lon)) / 2), 2)
            ))
        ) <= :radiusKm
        ORDER BY (
            6371 * 2 * ASIN(SQRT(
                POWER(SIN((RADIANS(t.latitude) - RADIANS(:lat)) / 2), 2)
                + COS(RADIANS(:lat)) * COS(RADIANS(t.latitude))
                * POWER(SIN((RADIANS(t.longitude) - RADIANS(:lon)) / 2), 2)
            ))
        ) ASC
    """, nativeQuery = true)
    List<Terminal> findWithinRadius(
            @Param("lat") BigDecimal latitude,
            @Param("lon") BigDecimal longitude,
            @Param("radiusKm") Double radiusKm
    );

    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM Booking b
        WHERE b.terminal.idTerminal = :terminalId
        AND b.statusBooking != 'REFUSEE'
        AND NOT (b.endingDate <= :start OR b.startingDate >= :end)
    """)
    boolean isTerminalBookedBetween(
            @Param("terminalId") Long terminalId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Terminal> findByOccupied(Boolean occupied);

    @Query("""
        SELECT t FROM Terminal t
        WHERE NOT EXISTS (
            SELECT 1 FROM Booking b
            WHERE b.terminal.idTerminal = t.idTerminal
            AND b.statusBooking != com.electricitybusiness.api.model.BookingStatus.REFUSEE
            AND NOT (b.endingDate <= :start OR b.startingDate >= :end)
        )
    """)
    List<Terminal> findAvailableTerminals(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );*/


/*    @Query("SELECT DISTINCT t FROM Terminal t " +
            "LEFT JOIN Booking b ON b.terminal = t " +
            "   AND b.startingDate < :endingDate " +
            "   AND b.endingDate > :startingDate " +
            "WHERE (:occupied IS NULL OR t.occupied = :occupied) " +
            "AND (:latitude IS NULL OR :longitude IS NULL OR :radius IS NULL OR " +
            "   (6371 * 2 * ASIN(SQRT(" +
            "       POWER(SIN((RADIANS(t.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "       + COS(RADIANS(:latitude)) * COS(RADIANS(t.latitude)) * " +
            "       POWER(SIN((RADIANS(t.longitude) - RADIANS(:longitude)) / 2), 2)" +
            "))) <= :radius) " +
            "AND (:startingDate IS NULL OR :endingDate IS NULL OR b IS NULL)")
    List<Terminal> searchTerminals(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") Double radius,
            @Param("occupied") Boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate
    );*/


/*    @Query("SELECT t FROM Terminal t " +
            "WHERE (:occupied IS NULL OR t.occupied = :occupied) " +
            "AND (:latitude IS NULL OR :longitude IS NULL OR :radius IS NULL OR " +
            "(6371 * 2 * ASIN(SQRT(POWER(SIN((RADIANS(t.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "+ COS(RADIANS(:latitude)) * COS(RADIANS(t.latitude)) * POWER(SIN((RADIANS(t.longitude) - RADIANS(:longitude)) / 2), 2)))) <= :radius) " +
            "AND (:startingDate IS NULL OR :endingDate IS NULL OR NOT EXISTS (" +
            "    SELECT 1 FROM Booking b " +
            "    WHERE b.terminal = t " +
            "    AND b.endingDate < :startingDate AND b.endingDate < :endingDate " +
            "    OR b.startingDate > :endingDate AND b.startingDate > :startingDate" +
            "))")
    List<Terminal> searchTerminals(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") Double radius,
            @Param("occupied") Boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate
    );*/


    
    // Méthode publicId

/*
    List<Terminal> findTerminalsByPlace(UUID publicId);
*/
    List<Terminal> findTerminalByPlace_PublicId(UUID publicId);


    void deleteTerminalByPublicId(UUID publicId);

    Optional<Terminal> findByPublicId(UUID publicId);
}

