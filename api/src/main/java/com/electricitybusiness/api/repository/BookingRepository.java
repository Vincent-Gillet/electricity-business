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
    List<Booking> findByUser(User user);

    List<Booking> findByTerminal(Terminal terminal);

    List<Booking> findByStatusBooking(BookingStatus status);

    List<Booking> findByUserAndStatusBooking(User user, BookingStatus status);

    List<Booking> findByTerminalAndStatusBooking(Terminal terminal, BookingStatus status);

    // Méthodes user

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

    // Réservations pour les places appartenant à l'utilisateur
    @Query("SELECT b FROM Booking b JOIN b.terminal t JOIN t.place p JOIN p.user WHERE p.user = :user")
    List<Booking> findBookingsByUserOwner(User user);

    List<Booking> findBookingsByUserAndStatusBooking(User user, BookingStatus status);

    void deleteBookingByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    Optional<Booking> findByPublicId(UUID publicId);

    // Méthodes changement status automatiques

    List<Booking> findAllByStatusBookingAndEndingDateAfter(BookingStatus status, LocalDateTime dateTime);
}
