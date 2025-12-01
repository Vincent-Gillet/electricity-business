package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    /** Méthodes basiques de recherche
     *
     * @param user
     * @return
     */
    List<Booking> findByUser(User user);

    /** Méthodes basiques de recherche
     *
     * @param terminal
     * @return
     */
    List<Booking> findByTerminal(Terminal terminal);

    /** Méthodes basiques de recherche
     *
     * @param status
     * @return
     */
    List<Booking> findByStatusBooking(BookingStatus status);

    /** Méthodes basiques de recherche
     *
     * @param user
     * @param status
     * @return
     */
    List<Booking> findByUserAndStatusBooking(User user, BookingStatus status);

    /** Méthodes basiques de recherche
     *
     * @param terminal
     * @param status
     * @return
     */
    List<Booking> findByTerminalAndStatusBooking(Terminal terminal, BookingStatus status);

    // Méthodes user

    /** Recherche des réservations pour un utilisateur spécifique avec des filtres optionnels.
     *
     * @param user          L'utilisateur pour lequel les réservations doivent être trouvées.
     * @param startingDate  La date de début minimale des réservations (optionnelle).
     * @param endingDate    La date de fin maximale des réservations (optionnelle).
     * @param orderBooking  L'ordre de tri des réservations par date de début ('ASC' ou 'DESC').
     * @param statusBooking Le statut des réservations à rechercher (optionnelle).
     * @return Une liste de réservations correspondant aux critères spécifiés.
     */
    @Query("SELECT b FROM Booking b " +
            "WHERE b.user = :user " +
            "AND (:startingDate IS NULL OR b.startingDate >= :startingDate)" +
            "AND (:endingDate IS NULL OR b.endingDate <= :endingDate) " +
            "AND (:statusBooking IS NULL OR b.statusBooking = :statusBooking) " +
            "ORDER BY " +
                "CASE WHEN :orderBooking = 'DESC' THEN b.startingDate END DESC, " +
                "CASE WHEN :orderBooking = 'ASC' THEN b.startingDate END ASC"
    )
    List<Booking> findBookingsByUserMyBookings(
        @Param("user") User user,
        @Param("startingDate") LocalDateTime startingDate,
        @Param("endingDate") LocalDateTime endingDate,
        @Param("orderBooking") String orderBooking,
        @Param("statusBooking") BookingStatus statusBooking
    );

    /** Recherche des réservations pour les lieux appartenant à un utilisateur spécifique.
     *
     * @param user L'utilisateur propriétaire des lieux.
     * @return Une liste de réservations associées aux lieux de l'utilisateur.
     */
    @Query("SELECT b FROM Booking b JOIN b.terminal t JOIN t.place p JOIN p.user WHERE p.user = :user")
    List<Booking> findBookingsByUserOwner(User user);

    /** Recherche des réservations d'un utilisateur avec un statut spécifique.
     *
     * @param user   L'utilisateur pour lequel les réservations doivent être trouvées.
     * @param status Le statut des réservations à rechercher.
     * @return Une liste de réservations correspondant aux critères spécifiés.
     */
    List<Booking> findBookingsByUserAndStatusBooking(User user, BookingStatus status);

    /** Supprime une réservation par son identifiant public.
     *
     * @param publicId L'identifiant public de la réservation à supprimer.
     */
    void deleteBookingByPublicId(UUID publicId);

    /** Vérifie l'existence d'une réservation par son identifiant public.
     *
     * @param publicId L'identifiant public de la réservation à vérifier.
     * @return true si la réservation existe, sinon false.
     */
    boolean existsByPublicId(UUID publicId);

    /** Recherche une réservation par son identifiant public.
     *
     * @param publicId L'identifiant public de la réservation à rechercher.
     * @return Un Optional contenant la réservation si elle existe, sinon vide.
     */
    Optional<Booking> findByPublicId(UUID publicId);

    // Méthodes changement status automatiques

    /** Recherche des réservations avec un statut spécifique dont la date de fin est passée.
     *
     * @param status   Le statut des réservations à rechercher.
     * @param dateTime La date et l'heure actuelles pour comparer avec la date de fin des réservations.
     * @return Une liste de réservations correspondant aux critères spécifiés.
     */
    List<Booking> findAllByStatusBookingAndEndingDateAfter(BookingStatus status, LocalDateTime dateTime);

    /** Recherche des réservations qui se chevauchent avec une nouvelle plage de dates pour un terminal donné.
     *
     * @param terminal          Le terminal pour lequel vérifier les chevauchements.
     * @param newStartingDate   La date de début de la nouvelle réservation.
     * @param newEndingDate     La date de fin de la nouvelle réservation.
     * @return Une liste de réservations qui se chevauchent avec la nouvelle plage de dates.
     */
    @Query("SELECT b FROM Booking b " +
            "WHERE b.terminal = :terminal " +
            "AND b.statusBooking IN ('EN_ATTENTE', 'ACCEPTEE') " +
            "AND ( " +
            "    (:newStartingDate < b.endingDate AND :newEndingDate > b.startingDate) " + // Conditions de chevauchement
            ")")
    List<Booking> findOverlappingBookings(
            @Param("terminal") Terminal terminal,
            @Param("newStartingDate") LocalDateTime newStartingDate,
            @Param("newEndingDate") LocalDateTime newEndingDate
    );
}
