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
    @Query(value = "SELECT DISTINCT t.* FROM terminals t " +
            "WHERE t.occupied = 0 ",
            nativeQuery = true)
    List<Terminal> findAvailableTerminals();

    // Récupérer toutes les Terminals dans le rayon
    @Query(value = "SELECT * FROM terminals t " +
            "WHERE t.occupied = :occupied " +
            "AND (6371 * 2 * ASIN(SQRT(POWER(SIN((RADIANS(t.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "+ COS(RADIANS(:latitude)) * COS(RADIANS(t.latitude)) * POWER(SIN((RADIANS(t.longitude) - RADIANS(:longitude)) / 2), 2)))) <= :radius",
            nativeQuery = true)
    List<Terminal> findTerminalsInRadius(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") double radius,
            @Param("occupied") boolean occupied
    );

    // Rechercher fonctionne non modulaire
    @Query(value = "SELECT DISTINCT t.* FROM terminals t " +
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
            @Param("endingDate") LocalDateTime endingDate);

    // Rechercher fonctionne non modulaire
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







    @Query("SELECT t FROM Terminal t " +
            "WHERE (:occupied IS NULL OR t.occupied = :occupied) " +
            "AND (:latitude IS NULL OR :longitude IS NULL OR :radius IS NULL OR " +
            "(6371 * 2 * ASIN(SQRT(POWER(SIN((RADIANS(t.latitude) - RADIANS(:latitude)) / 2), 2) " +
            "+ COS(RADIANS(:latitude)) * COS(RADIANS(t.latitude)) * POWER(SIN((RADIANS(t.longitude) - RADIANS(:longitude)) / 2), 2)))) <= :radius) " +
            "AND (:startingDate IS NULL OR :endingDate IS NULL OR NOT EXISTS (" +
            "    SELECT 1 FROM Booking b " +
            "    WHERE b.terminal = t " +
            "    AND b.endingDate > :startingDate " +
            "    AND b.startingDate < :endingDate))")
    List<Terminal> searchTerminals(
            @Param("longitude") BigDecimal longitude,
            @Param("latitude") BigDecimal latitude,
            @Param("radius") Double radius,
            @Param("occupied") Boolean occupied,
            @Param("startingDate") LocalDateTime startingDate,
            @Param("endingDate") LocalDateTime endingDate
    );
    
    
}

