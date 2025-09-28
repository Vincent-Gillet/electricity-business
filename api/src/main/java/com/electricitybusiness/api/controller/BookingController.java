package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.BookingDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Booking;
import com.electricitybusiness.api.model.BookingStatus;
import com.electricitybusiness.api.model.Terminal;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des réservations.
 * Expose les endpoints pour les opérations CRUD sur les réservations.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final EntityMapper mapper;

    /**
     * Récupère toutes les réservations.
     * GET /api/bookings
     *
     * @return Une liste de toutes les réservations
     */
    @GetMapping("/all")
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        List<BookingDTO> bookingDTO = bookings.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDTO);
    }
/*    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        try {
            List<Booking> Bookings = bookingService.getAllBookings();
            List<BookingDTO> dtos = Bookings.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception properly
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private BookingDTO convertToDto(Booking Booking) {
        BookingDTO dto = new BookingDTO();
        dto.setIdBooking(Booking.getIdBooking());
        dto.setIdUtilisateur(Booking.getIdUtilisateur());
        dto.setIdVehicule(Booking.getIdVehicule());
        dto.setIdBorne(Booking.getIdBorne());
        dto.setIdOption(Booking.getIdOption());
        dto.setNumBooking(Booking.getNumBooking());
        dto.setstatus(Booking.getstatus().toString());
        dto.setMontantPaye(Booking.getMontantPaye());
        dto.setDatePaiement(Booking.getDatePaiement());
        dto.setDateDebut(Booking.getDateDebut());
        dto.setDateFin(Booking.getDateFin());
        return dto;
    }*/



    /**
     * Récupère une réservation par son ID.
     * GET /api/bookings/{id}
     *
     * @param id L'identifiant de la réservation à récupérer
     * @return La réservation correspondante à l'ID, ou un status HTTP 404 Not Found si non trouvée
     */
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(Booking -> ResponseEntity.ok(mapper.toDTO(Booking)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée une nouvelle réservation.
     * POST /api/bookings
     *
     * @param bookingDTO La réservation à créer
     * @return La réservation créée avec un status HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody BookingDTO bookingDTO) {
        Booking booking = mapper.toEntity(bookingDTO);
        Booking savedBooking = bookingService.saveBooking(booking);
        BookingDTO savedDTO = mapper.toDTO(savedBooking);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }

    /**
     * Met à jour une réservation existante.
     * PUT /api/bookings/{id}
     * @param id L'identifiant de la réservation à mettre à jour
     * @param bookingDTO La réservation avec les nouvelles informations
     * @return La réservation mise à jour
     */
    @PutMapping("/{id}")
    public ResponseEntity<BookingDTO> updateBooking(@PathVariable Long id, @Valid @RequestBody BookingDTO bookingDTO) {
        if (!bookingService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Booking booking = mapper.toEntity(bookingDTO);
        Booking updatedBooking = bookingService.updateBooking(id, booking);
        BookingDTO updatedDTO = mapper.toDTO(updatedBooking);
        return ResponseEntity.ok(updatedDTO);
    }

    /**
     * Supprime une réservation.
     * DELETE /api/bookings/{id}
     * @param id L'identifiant de la réservation à supprimer
     * @return Un status HTTP 204 No Content si la suppression est réussie, ou 404 Not Found si l'ID n'existe pas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        if (!bookingService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        bookingService.deleteBookingById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère les réservations par utilisateur.
     * GET /api/bookings/user/{user}
     * @param user L'utilisateur associé aux réservations
     * @return Une liste de réservations correspondant à l'utilisateur
     */
    @GetMapping("/{user}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUser(@PathVariable User user) {
        List<Booking> bookings = bookingService.findByUser(user);
        List<BookingDTO> bookingDTO = bookings.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDTO);
    }

    /**
     * Récupère les réservations par borne.
     * GET /api/bookings/borne/{borne}
     * @param terminal La borne associée aux réservations
     * @return Une liste de réservations correspondant à la borne
     */
    @GetMapping("/{borne}")
    public ResponseEntity<List<BookingDTO>> getBookingsByTerminal(@PathVariable Terminal terminal) {
        List<Booking> bookings = bookingService.findByTerminal(terminal);
        List<BookingDTO> bookingDTO = bookings.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDTO);
    }

    /**
     * Récupère les réservations par status.
     * GET /api/bookings/status/{status}
     * @param status Le status des réservations à récupérer
     * @return Une liste de réservations correspondant au status
     */
    @GetMapping("/{status}")
    public ResponseEntity<List<BookingDTO>> getBookingsBystatus(@PathVariable BookingStatus status) {
        List<Booking> bookings = bookingService.findByStatusBooking(status);
        List<BookingDTO> bookingDTO = bookings.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDTO);
    }

    /**
     * Récupère les réservations par utilisateur et status.
     * GET /api/bookings/{utilisateur}/{status}
     * @param user L'utilisateur associé aux réservations
     * @param status Le status des réservations à récupérer
     * @return Une liste de réservations correspondant à l'utilisateur et au status
     */
    @GetMapping("/{user}/{status}")
    public ResponseEntity<List<BookingDTO>> getBookingsByUtilisateurAndstatus(@PathVariable User user, BookingStatus status) {
        List<Booking> bookings = bookingService.findByUserAndStatusBooking(user, status);
        List<BookingDTO> bookingDTO = bookings.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDTO);
    }

    /**
     * Récupère les réservations par borne et status.
     * GET /api/bookings/{borne}/{status}
     * @param terminal La borne associée aux réservations
     * @param status Le status des réservations à récupérer
     * @return Une liste de réservations correspondant à la borne et au status
     */
    @GetMapping("/{borne}/{status}")
    public ResponseEntity<List<BookingDTO>> getBookingsByBorneAndstatus(@PathVariable Terminal terminal, BookingStatus status) {
        List<Booking> Bookings = bookingService.findByTerminalAndStatusBooking(terminal, status);
        List<BookingDTO> BookingDTO = Bookings.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(BookingDTO);
    }

    /**
     * Récupère les réservations actives par borne.
     * GET /api/bookings/{borne}/{actif}
     *  borne La borne associée aux réservations
     *  actif L'état actif des réservations à récupérer
     * @return Une liste de réservations actives correspondant à la borne
     */
/*    @GetMapping("/{borne}/{actif}")
    public ResponseEntity<List<BookingDTO>> getBookingsByBorneAndActif(@PathVariable Borne borne, Boolean actif) {
        List<Booking> Bookings = bookingService.findByBorneAndActif(borne, actif);
        List<BookingDTO> BookingDTO = Bookings.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(BookingDTO);
    }*/


    @GetMapping("/search")
    public ResponseEntity<List<BookingDTO>> findByBookingBetweenDate(@RequestParam LocalDateTime startingDateBooking, @RequestParam LocalDateTime endingDateBooking) {
        List<Booking> bookings = bookingService.findByBookingBetweenDate(startingDateBooking, endingDateBooking);
        List<BookingDTO> bookingDTO = bookings.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingDTO);
    }
}
