package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Booking;
import com.electricitybusiness.api.model.BookingStatus;
import com.electricitybusiness.api.model.Terminal;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {
    private final BookingRepository bookingRepository;

    /**
     * Récupère toutes les réservations.
     * @return Une liste de toutes les réservations
     */
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Récupère une réservation par son ID.
     * @param id L'identifiant de la réservation à récupérer
     * @return Un Optional contenant la réservation si trouvée, sinon vide
     */
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }


    /**
     * Crée une nouvelle réservation.
     * @param booking La réservation à enregistrer
     * @return La réservation enregistrée
     */
    @Transactional
    public Booking saveBooking(Booking booking) {
        return bookingRepository.save(booking);
    }


    /**
     * Met à jour une réservation existante.
     * @param id L'identifiant de la réservation à mettre à jour
     * @param booking La réservation avec les nouvelles informations
     * @return La réservation mise à jour
     */
    public Booking updateBooking(Long id, Booking booking) {
        booking.setIdBooking(id);
        return bookingRepository.save(booking);
    }

    /**
     * Supprime une réservation.
     * @param id L'identifiant de la réservation à supprimer
     */
    public void deleteBookingById(Long id) {
        bookingRepository.deleteById(id);
    }

    /**
     * Vérifie si une réservation existe par son ID.
     * @param id L'identifiant de la réservation à vérifier
     * @return true si la réservation existe, false sinon
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return bookingRepository.existsById(id);
    }

    /**
     * Récupère les réservations par utilisateur.
     * @param user L'utilisateur associé aux réservations
     * @return Une liste de réservations correspondant à l'utilisateur
     */
    @Transactional(readOnly = true)
    public List<Booking> findByUser(User user) {
        return bookingRepository.findByUser(user);
    }

    /**
     * Récupère les réservations par borne.
     * @param terminal La borne associée aux réservations
     * @return Une liste de réservations correspondant à la borne
     */
    @Transactional(readOnly = true)
    public List<Booking> findByTerminal(Terminal terminal) {
        return bookingRepository.findByTerminal(terminal);
    }

    /**
     * Récupère les réservations par statut.
     * @param status Le statut des réservations à récupérer
     * @return Une liste de réservations correspondant au statut
     */
    @Transactional(readOnly = true)
    public List<Booking> findByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    /**
     * Récupère les réservations par utilisateur et statut.
     * @param user L'utilisateur associé aux réservations
     * @param status Le statut des réservations à récupérer
     * @return Une liste de réservations correspondant à l'utilisateur et au statut
     */
    @Transactional(readOnly = true)
    public List<Booking> findByUserAndStatus(User user, BookingStatus status) {
        return bookingRepository.findByUserAndStatus(user, status);
    }

    /**
     * Récupère les réservations par borne et statut.
     * @param terminal La borne associée aux réservations
     * @param status Le statut des réservations à récupérer
     * @return Une liste de réservations correspondant à la borne et au statut
     */
    @Transactional(readOnly = true)
    public List<Booking> findByTerminalAndStatus(Terminal terminal, BookingStatus status) {
        return bookingRepository.findByTerminalAndStatus(terminal, status);
    }

    /**
     * Récupère les réservations actives par borne.
     *  borne La borne associée aux réservations
     *  actif L'état actif des réservations à récupérer
     * @return Une liste de réservations actives correspondant à la borne
     */
/*    @Transactional(readOnly = true)
    public List<Booking> findByBorneAndActif(Borne borne, Boolean actif) {
        return bookingRepository.findByBorneAndActif(borne, actif);
    }*/


    @Transactional(readOnly = true)
    public List<Booking> findByBookingBetweenDate(LocalDateTime startingDate, LocalDateTime endingDate) {
        List<Booking> listBooking = bookingRepository.findAll();
        List<Booking> newlistBooking = new ArrayList<>();
        for (Booking Booking : listBooking) {
            if (startingDate.isBefore(Booking.getStartingDate()) && endingDate.isBefore(Booking.getEndingDate())) {
                newlistBooking.add(Booking);
            }
        };
        return newlistBooking;
    }
}
