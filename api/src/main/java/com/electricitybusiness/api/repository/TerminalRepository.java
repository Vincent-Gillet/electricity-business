package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.Place;
import com.electricitybusiness.api.model.Terminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface de gestion des opérations CRUD pour les Terminals.
 * Hérite de JpaRepository pour les opérations de base de données.
 */
@Repository
public interface TerminalRepository extends JpaRepository<Terminal,Long>, TerminalRepositoryCustom {
    List<Terminal> findByPlace(Place place);

    List<Terminal> findByStatusTerminal(Terminal statusTerminal);

    List<Terminal> findByoccupied(Terminal occupied);

    List<Terminal> findByPlaceAndStatusTerminal(Place place, Terminal statusTerminal);

    // Récupérer toutes les Terminals disponibles (occupieds = 0)
    @Query(value = "SELECT DISTINCT b.* FROM Terminals b " +
            "WHERE b.occupied = 0 ",
            nativeQuery = true)
    List<Terminal> findAvailableTerminals();

    // Récupérer toutes les Terminals dans le rayon
    @Query(value = "SELECT * FROM Terminals b " +
            "WHERE b.occupied = :occupied " +
            "AND (6371 * 2 * ASIN(SQRT(POWER(SIN((RADIANS(b.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "+ COS(RADIANS(:latitude)) * COS(RADIANS(b.latitude)) * POWER(SIN((RADIANS(b.longitude) - RADIANS(:longitude)) / 2), 2)))) <= :radius",
            nativeQuery = true)
    List<Terminal> findTerminalsInRadius(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") double radius,
            @Param("occupied") boolean occupied
    );

    // Rechercher fonctionne non modulaire
    @Query(value = "SELECT DISTINCT b.* FROM Terminals b " +
            "WHERE b.occupied = :occupied " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM reservations r " +
            "    WHERE r.id_Terminal = b.id_Terminal " +
            "    AND r.ending_date > :startingDate " +
            "    AND r.starting_date < :endingDate" +
            ")",
            nativeQuery = true)
    List<Terminal> findTerminalsAvailableInPeriod(
            @Param("occupied") boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate);

    // Rechercher fonctionne non modulaire
    @Query(value = "SELECT DISTINCT b.* FROM Terminals b " +
            "WHERE b.occupied = :occupied " +
            "AND (6371 * 2 * ASIN(SQRT(POWER(SIN((RADIANS(b.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "+ COS(RADIANS(:latitude)) * COS(RADIANS(b.latitude)) * POWER(SIN((RADIANS(b.longitude) - RADIANS(:longitude)) / 2), 2)))) <= :radius " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM reservations r " +
            "    WHERE r.id_Terminal = b.id_Terminal " +
            "    AND r.ending_date > :startingDate " +
            "    AND r.starting_date < :endingDate" +
            ")",
            nativeQuery = true)
    List<Terminal> findTerminalsAvailableInRadiusAndPeriod(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") double radius,
            @Param("occupied") boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate);







    @Query("SELECT b FROM Terminal b " +
            "WHERE (:occupied IS NULL OR b.occupied = :occupied) " +
            "AND (:latitude IS NULL OR :longitude IS NULL OR :radius IS NULL OR " +
            "(6371 * 2 * ASIN(SQRT(POWER(SIN((RADIANS(b.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "+ COS(RADIANS(:latitude)) * COS(RADIANS(b.latitude)) * POWER(SIN((RADIANS(b.longitude) - RADIANS(:longitude)) / 2), 2)))) <= :radius) " +
            "AND (:startingDate IS NULL OR :endingDate IS NULL OR NOT EXISTS (" +
            "    SELECT 1 FROM Reservation r " +
            "    WHERE r.Terminal = b " +
            "    AND r.endingDate > :startingDate " +
            "    AND r.startingDate < :endingDate))")
    List<Terminal> searchTerminals(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") Double radius,
            @Param("occupied") Boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate
    );
    
    
}

