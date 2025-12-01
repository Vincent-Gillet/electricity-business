package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.booking.BookingCreateDTO;
import com.electricitybusiness.api.dto.booking.BookingDTO;
import com.electricitybusiness.api.dto.booking.BookingStatusDTO;
import com.electricitybusiness.api.dto.car.CarCreateDTO;
import com.electricitybusiness.api.dto.car.CarDTO;
import com.electricitybusiness.api.dto.terminal.TerminalCreateDTO;
import com.electricitybusiness.api.dto.terminal.TerminalDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.*;
import com.electricitybusiness.api.service.BookingService;
import com.electricitybusiness.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.Document;
import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
    private final UserService userService;

    /**
     * Récupère toutes les réservations.
     * GET /api/bookings
     *
     * @return Une liste de toutes les réservations
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
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
    @PreAuthorize("hasAuthority('ADMIN')")
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("hasAuthority('ADMIN')")
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
    @PreAuthorize("isAuthenticated()")
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
    @GetMapping("/user/{user}")
    @PreAuthorize("isAuthenticated()")
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
    @GetMapping("borne/{borne}")
    @PreAuthorize("isAuthenticated()")
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
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
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

    // requête user

    /**
     * Récupère tous les Réservations d'un utilisateur.
     * GET /api/booking/user/{idUser}
     * @return Une liste de tous les véhicules
     */
    @GetMapping("/user/client")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingDTO>> getAllBookingsByUserClient(
            @RequestParam(required = false) LocalDateTime startingDate,
            @RequestParam(required = false) LocalDateTime endingDate,
            @RequestParam(required = false, defaultValue = "ASC") String orderBooking,
            @RequestParam(required = false) BookingStatus statusBooking
    ) {

        System.out.println("startingDate booking : " + startingDate);
        System.out.println("endingDate booking : " + endingDate);
        System.out.println("orderBooking booking : " + orderBooking);
        System.out.println("statusBooking booking : " + statusBooking);

        // Récupérer l'utilisateur authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long idUser = userService.getIdByEmailUser(email);
        User user = userService.getUserById(idUser);

        // Récupérer les voitures de l'utilisateur
/*
        List<Booking> bookings = bookingService.getBookingsByUserClient(user);
*/
        List<Booking> bookings = bookingService.getBookingsByUserClient(user, startingDate, endingDate, orderBooking, statusBooking);
        List<BookingDTO> bookingsDTO = bookings.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingsDTO);
    }

    /**
     * Récupère tous les Réservations d'un utilisateur.
     * GET /api/booking/user/{idUser}
     * @return Une liste de tous les véhicules
     */
    @GetMapping("/user/owner")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingDTO>> getAllBookingsByUserOwner() {
        // Récupérer l'utilisateur authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long idUser = userService.getIdByEmailUser(email);
        User user = userService.getUserById(idUser);

        // Récupérer les voitures de l'utilisateur
        List<Booking> bookings = bookingService.getBookingsByUserOwner(user);
        List<BookingDTO> bookingsDTO = bookings.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookingsDTO);
    }

    @PostMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDTO> saveBookingByToken(@Valid @RequestBody BookingCreateDTO bookingDTO) {
        OffsetDateTime now = OffsetDateTime.now();
        System.out.println("OffsetDateTime.now() : " + now);
        System.out.println("Base UTC time : " + now.withOffsetSameInstant(ZoneOffset.UTC));


        // Récupérer l'utilisateur authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long idUser = userService.getIdByEmailUser(email);

        Booking booking = mapper.toEntityCreate(bookingDTO, idUser, bookingDTO.getPublicIdTerminal(), bookingDTO.getPublicIdCar(), bookingDTO.getPublicIdOption());
        Booking savedBooking = bookingService.saveBooking(booking);

        BookingDTO savedDTO = mapper.toDTO(savedBooking);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }

    @DeleteMapping("publicId/{publicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID publicId) {
        if (!bookingService.existsByPublicId(publicId)) {
            return ResponseEntity.notFound().build();
        }
        bookingService.deleteBookingByPublicId(publicId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Met à jour une voiture existante.
     * PUT /api/cars/{id}
     * @param publicId L'identifiant de la voiture à mettre à jour
     * @param bookingDTO La voiture avec les nouvelles informations
     * @return La voiture mis à jour, ou un statut HTTP 404 Not Found si l'ID n'existe pas
     */
    @PutMapping("/publicId/{publicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDTO> updateBooking(
            @PathVariable UUID publicId,
            @Valid @RequestBody BookingCreateDTO bookingDTO
    ) {
        if (!bookingService.existsByPublicId(publicId)) {
            return ResponseEntity.notFound().build();
        }
        // Récupérer l'utilisateur authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long idUser = userService.getIdByEmailUser(email);

        // Mettre à jour la voiture
        Booking booking = mapper.toEntityCreate(bookingDTO, idUser, bookingDTO.getPublicIdTerminal(), bookingDTO.getPublicIdOption(), bookingDTO.getPublicIdCar());
        Booking updatedBooking = bookingService.updateBooking(publicId, booking);
        BookingDTO updatedDTO = mapper.toDTO(updatedBooking);
        return ResponseEntity.ok(updatedDTO);
    }

    /**
     * Met à jour une voiture existante.
     * PUT /api/cars/{id}
     * @param publicId L'identifiant de la voiture à mettre à jour
     * @param bookingDTO La voiture avec les nouvelles informations
     * @return La voiture mis à jour, ou un statut HTTP 404 Not Found si l'ID n'existe pas
     */
    @PutMapping("/publicId/{publicId}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingDTO> updateBookingStatus(
            @PathVariable UUID publicId,
            @Valid @RequestBody BookingStatusDTO bookingDTO
    ) {
        if (!bookingService.existsByPublicId(publicId)) {
            return ResponseEntity.notFound().build();
        }

        Booking updatedBooking = bookingService.updateBookingStatus(publicId, bookingDTO);
        BookingDTO updatedDTO = mapper.toDTO(updatedBooking);
        return ResponseEntity.ok(updatedDTO);
    }

    /**
     * Génère un PDF pour une réservation donnée.
     * GET /api/bookings/publicId/{id}/pdf
     *
     * @param id L'identifiant public de la réservation
     * @return Le PDF de la réservation en tant que tableau d'octets, ou un statut HTTP 500 Internal Server Error en cas d'erreur
     */
    @GetMapping("/publicId/{id}/pdf")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> generateBookingPdf(@PathVariable UUID id) {
        try {
            byte[] pdfBytes = bookingService.generateBookingPdf(id);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=facture-" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/excel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> generateBookingExcel() throws Exception {
        // Récupérer l'utilisateur authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long idUser = userService.getIdByEmailUser(email);
        User user = userService.getUserById(idUser);

        // Générer le fichier Excel
        byte[] excelBytes = bookingService.generateBookingExcel(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bookings.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }


    @GetMapping("/booking-status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingStatus>> getAllBookingStatuses() {
        List<BookingStatus> statuses = bookingService.getAllBookingStatus();
        return ResponseEntity.ok(statuses);
    }
}
