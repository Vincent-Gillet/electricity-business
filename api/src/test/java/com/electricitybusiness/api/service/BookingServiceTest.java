package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.booking.BookingStatusDTO;
import com.electricitybusiness.api.model.*;
import com.electricitybusiness.api.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock // Créera un mock de BookingRepository
    private BookingRepository bookingRepository;

    @InjectMocks // Injectera les mocks dans BookingService
    private BookingService bookingService;

    // Objets de test communs
    private User testUser;
    private Terminal testTerminal;
    private Booking testBooking;
    private UUID testPublicId;

    @BeforeEach
        // Cette méthode s'exécutera avant chaque test
    void setUp() {
        // Initialisation de l'utilisateur de test
        testUser = new User();
        testUser.setIdUser(1L);
        testUser.setFirstName("John");
        testUser.setSurnameUser("Doe");
        testUser.setEmailUser("john.doe@example.com");

        // Initialisation du terminal de test (simplifié)
        testTerminal = new Terminal();
        testTerminal.setIdTerminal(10L);
        testTerminal.setNameTerminal("Terminal_A");
        testTerminal.setPublicId(UUID.randomUUID());
        testTerminal.setUser(testUser); // Le terminal appartient à cet utilisateur (propriétaire)

        // Initialisation de la réservation de test
        testPublicId = UUID.randomUUID();
        testBooking = new Booking(
                1L, // idBooking
                testPublicId, // publicId
                testUser, // user (client qui réserve)
                null, // car (si applicable)
                testTerminal, // terminal réservé
                null, // options (si applicable)
                "123456789", // bookingCode
                BookingStatus.EN_ATTENTE, // statusBooking
                BigDecimal.valueOf(50.00), // totalAmount
                LocalDateTime.now().plusHours(1), // startingDate
                LocalDateTime.now().plusHours(3), // endingDate
                LocalDateTime.now() // createdAt
        );
    }

    @Test
    @DisplayName("Devrait récupérer toutes les réservations")
    void shouldGetAllBookings() {
        // Préparation (Arrange)
        Booking booking2 = new Booking(2L, UUID.randomUUID(), testUser, null, testTerminal, null, "987654321", BookingStatus.ACCEPTEE, BigDecimal.valueOf(75.00), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), LocalDateTime.now());
        List<Booking> expectedBookings = Arrays.asList(testBooking, booking2);
        when(bookingRepository.findAll()).thenReturn(expectedBookings); // Quand findAll est appelé, retourner notre liste

        // Exécution (Act)
        List<Booking> actualBookings = bookingService.getAllBookings();

        // Vérification (Assert)
        assertNotNull(actualBookings);
        assertEquals(2, actualBookings.size());
        assertEquals(expectedBookings, actualBookings);
        verify(bookingRepository, times(1)).findAll(); // Vérifier que findAll a été appelé une fois
    }

    @Test
    @DisplayName("Devrait récupérer une réservation par ID existant")
    void shouldGetBookingByIdWhenExists() {
        // Préparation
        when(bookingRepository.findById(testBooking.getIdBooking())).thenReturn(Optional.of(testBooking));

        // Exécution
        Optional<Booking> foundBooking = bookingService.getBookingById(testBooking.getIdBooking());

        // Vérification
        assertTrue(foundBooking.isPresent());
        assertEquals(testBooking, foundBooking.get());
        verify(bookingRepository, times(1)).findById(testBooking.getIdBooking());
    }

    @Test
    @DisplayName("Ne devrait pas récupérer de réservation pour un ID non existant")
    void shouldNotGetBookingByIdWhenNotExists() {
        // Préparation
        Long nonExistentId = 99L;
        when(bookingRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Exécution
        Optional<Booking> foundBooking = bookingService.getBookingById(nonExistentId);

        // Vérification
        assertFalse(foundBooking.isPresent());
        verify(bookingRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Devrait sauvegarder une nouvelle réservation")
    void shouldSaveBooking() {
        // Préparation
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        // Exécution
        Booking savedBooking = bookingService.saveBooking(testBooking);

        // Vérification
        assertNotNull(savedBooking);
        assertEquals(testBooking, savedBooking);
        verify(bookingRepository, times(1)).save(testBooking);
    }

    @Test
    @DisplayName("Devrait mettre à jour une réservation existante par ID")
    void shouldUpdateBookingById() {
        // Préparation
        Booking updatedInfo = new Booking();
        updatedInfo.setStatusBooking(BookingStatus.ACCEPTEE);
        updatedInfo.setTotalAmount(BigDecimal.valueOf(60.00));

        // Simuler le comportement de save : si on passe un booking avec ID, il le retourne.
        // Ici, bookingService.updateBooking modifie l'ID sur l'objet 'booking' passé en paramètre
        // puis appelle save. On simule donc save retournant un objet modifié.
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking bookingArg = invocation.getArgument(0);
            assertEquals(testBooking.getIdBooking(), bookingArg.getIdBooking()); // S'assurer que l'ID est bien mis à jour
            bookingArg.setStatusBooking(updatedInfo.getStatusBooking()); // Appliquer les modifications simulées
            bookingArg.setTotalAmount(updatedInfo.getTotalAmount());
            return bookingArg;
        });

        // Exécution
        Booking result = bookingService.updateBooking(testBooking.getIdBooking(), updatedInfo);

        // Vérification
        assertNotNull(result);
        assertEquals(testBooking.getIdBooking(), result.getIdBooking());
        assertEquals(BookingStatus.ACCEPTEE, result.getStatusBooking());
        assertEquals(BigDecimal.valueOf(60.00), result.getTotalAmount());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Devrait supprimer une réservation par ID")
    void shouldDeleteBookingById() {
        // Préparation (on ne teste pas la logique de recherche ici, juste l'appel à deleteById)
        doNothing().when(bookingRepository).deleteById(testBooking.getIdBooking());

        // Exécution
        bookingService.deleteBookingById(testBooking.getIdBooking());

        // Vérification
        verify(bookingRepository, times(1)).deleteById(testBooking.getIdBooking());
    }

    @Test
    @DisplayName("Devrait vérifier l'existence d'une réservation par ID")
    void shouldCheckExistenceById() {
        // Préparation
        when(bookingRepository.existsById(testBooking.getIdBooking())).thenReturn(true);

        // Exécution
        boolean exists = bookingService.existsById(testBooking.getIdBooking());

        // Vérification
        assertTrue(exists);
        verify(bookingRepository, times(1)).existsById(testBooking.getIdBooking());
    }

    @Test
    @DisplayName("Devrait trouver les réservations par utilisateur")
    void shouldFindByUser() {
        // Préparation
        List<Booking> userBookings = Arrays.asList(testBooking);
        when(bookingRepository.findByUser(testUser)).thenReturn(userBookings);

        // Exécution
        List<Booking> actualBookings = bookingService.findByUser(testUser);

        // Vérification
        assertNotNull(actualBookings);
        assertFalse(actualBookings.isEmpty());
        assertEquals(1, actualBookings.size());
        assertEquals(testBooking, actualBookings.get(0));
        verify(bookingRepository, times(1)).findByUser(testUser);
    }

    @Test
    @DisplayName("Devrait trouver les réservations par terminal")
    void shouldFindByTerminal() {
        // Préparation
        List<Booking> terminalBookings = Arrays.asList(testBooking);
        when(bookingRepository.findByTerminal(testTerminal)).thenReturn(terminalBookings);

        // Exécution
        List<Booking> actualBookings = bookingService.findByTerminal(testTerminal);

        // Vérification
        assertNotNull(actualBookings);
        assertFalse(actualBookings.isEmpty());
        assertEquals(1, actualBookings.size());
        assertEquals(testBooking, actualBookings.get(0));
        verify(bookingRepository, times(1)).findByTerminal(testTerminal);
    }

    @Test
    @DisplayName("Devrait trouver les réservations par statut")
    void shouldFindByStatusBooking() {
        // Préparation
        List<Booking> pendingBookings = Arrays.asList(testBooking);
        when(bookingRepository.findByStatusBooking(BookingStatus.EN_ATTENTE)).thenReturn(pendingBookings);

        // Exécution
        List<Booking> actualBookings = bookingService.findByStatusBooking(BookingStatus.EN_ATTENTE);

        // Vérification
        assertNotNull(actualBookings);
        assertFalse(actualBookings.isEmpty());
        assertEquals(1, actualBookings.size());
        assertEquals(testBooking, actualBookings.get(0));
        verify(bookingRepository, times(1)).findByStatusBooking(BookingStatus.EN_ATTENTE);
    }

    // --- Méthodes avec PublicId ---

    @Test
    @DisplayName("Devrait supprimer une réservation par publicId")
    void shouldDeleteBookingByPublicId() {
        // Préparation
        doNothing().when(bookingRepository).deleteBookingByPublicId(testPublicId);

        // Exécution
        bookingService.deleteBookingByPublicId(testPublicId);

        // Vérification
        verify(bookingRepository, times(1)).deleteBookingByPublicId(testPublicId);
    }

    @Test
    @DisplayName("Devrait vérifier l'existence d'une réservation par publicId")
    void shouldCheckExistenceByPublicId() {
        // Préparation
        when(bookingRepository.findByPublicId(testPublicId)).thenReturn(Optional.of(testBooking));

        // Exécution
        boolean exists = bookingService.existsByPublicId(testPublicId);

        // Vérification
        assertTrue(exists);
        verify(bookingRepository, times(1)).findByPublicId(testPublicId);
    }

    @Test
    @DisplayName("Ne devrait pas vérifier l'existence d'une réservation pour un publicId non existant")
    void shouldNotCheckExistenceByPublicIdWhenNotExists() {
        // Préparation
        UUID nonExistentPublicId = UUID.randomUUID();
        when(bookingRepository.findByPublicId(nonExistentPublicId)).thenReturn(Optional.empty());

        // Exécution
        boolean exists = bookingService.existsByPublicId(nonExistentPublicId);

        // Vérification
        assertFalse(exists);
        verify(bookingRepository, times(1)).findByPublicId(nonExistentPublicId);
    }

    @Test
    @DisplayName("Devrait mettre à jour une réservation existante par publicId")
    void shouldUpdateBookingByPublicId() {
        Booking existingBooking = new Booking();
        existingBooking.setPublicId(testPublicId);
        existingBooking.setIdBooking(1L);
        User fullyPopulatedExistingUser = new User();
        fullyPopulatedExistingUser.setIdUser(1L);
        fullyPopulatedExistingUser.setSurnameUser("Doe");
        fullyPopulatedExistingUser.setFirstName("John");
        fullyPopulatedExistingUser.setEmailUser("john.doe@example.com");
        fullyPopulatedExistingUser.setRole(UserRole.USER);
        fullyPopulatedExistingUser.setBanished(false);
        existingBooking.setUser(fullyPopulatedExistingUser);

        when(bookingRepository.findByPublicId(testPublicId)).thenReturn(Optional.of(existingBooking));

        Booking updatedBookingPayload = new Booking();
        updatedBookingPayload.setStatusBooking(BookingStatus.ACCEPTEE);

        updatedBookingPayload.setUser(null);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);

            if (savedBooking.getUser() == null || savedBooking.getUser().getIdUser() == null) {
                savedBooking.setUser(existingBooking.getUser());
            }
            return savedBooking; // Retourne le booking potentiellement modifié
        });

        Booking actualResult = bookingService.updateBooking(testPublicId, updatedBookingPayload);

        assertNotNull(actualResult);
        assertEquals(BookingStatus.ACCEPTEE, actualResult.getStatusBooking()); // Vérifiez les champs mis à jour
        assertEquals(fullyPopulatedExistingUser, actualResult.getUser());
    }

    @Test
    @DisplayName("Devrait lancer une exception lors de la mise à jour par publicId si la réservation n'existe pas")
    void shouldThrowExceptionWhenUpdateBookingByPublicIdNotFound() {
        // Préparation
        UUID nonExistentPublicId = UUID.randomUUID();
        when(bookingRepository.findByPublicId(nonExistentPublicId)).thenReturn(Optional.empty());
        Booking updatedInfo = new Booking();

        // Exécution et Vérification
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.updateBooking(nonExistentPublicId, updatedInfo);
        });

        assertEquals("Booking with publicId not found: " + nonExistentPublicId, thrown.getMessage());
        verify(bookingRepository, times(1)).findByPublicId(nonExistentPublicId);
        verify(bookingRepository, never()).save(any(Booking.class)); // S'assurer que save n'est jamais appelé
    }

    @Test
    @DisplayName("Devrait mettre à jour le statut d'une réservation par publicId")
    void shouldUpdateBookingStatus() {
        // Préparation
        BookingStatusDTO dto = new BookingStatusDTO(BookingStatus.REFUSEE);
        Booking bookingToUpdate = new Booking(testBooking.getIdBooking(), testPublicId, testUser, null, testTerminal, null, "123", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(50), LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDateTime.now());

        when(bookingRepository.findByPublicId(testPublicId)).thenReturn(Optional.of(bookingToUpdate));
        when(bookingRepository.saveAndFlush(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            assertEquals(BookingStatus.REFUSEE, savedBooking.getStatusBooking()); // Vérifier que le statut est mis à jour
            return savedBooking;
        });

        // Exécution
        Booking result = bookingService.updateBookingStatus(testPublicId, dto);

        // Vérification
        assertNotNull(result);
        assertEquals(BookingStatus.REFUSEE, result.getStatusBooking());
        verify(bookingRepository, times(1)).findByPublicId(testPublicId);
        verify(bookingRepository, times(1)).saveAndFlush(any(Booking.class));
    }

    @Test
    @DisplayName("Devrait lancer une exception lors de la mise à jour du statut si la réservation n'existe pas")
    void shouldThrowExceptionWhenUpdateBookingStatusNotFound() {
        // Préparation
        UUID nonExistentPublicId = UUID.randomUUID();
        when(bookingRepository.findByPublicId(nonExistentPublicId)).thenReturn(Optional.empty());
        BookingStatusDTO dto = new BookingStatusDTO(BookingStatus.REFUSEE);

        // Exécution et Vérification
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.updateBookingStatus(nonExistentPublicId, dto);
        });

        assertEquals("Booking not found: " + nonExistentPublicId, thrown.getMessage());
        verify(bookingRepository, times(1)).findByPublicId(nonExistentPublicId);
        verify(bookingRepository, never()).saveAndFlush(any(Booking.class));
    }

    @Test
    @DisplayName("Devrait récupérer les statuts de réservation disponibles")
    void shouldGetAllBookingStatus() {
        // Exécution
        List<BookingStatus> statuses = bookingService.getAllBookingStatus();

        // Vérification
        assertNotNull(statuses);
        assertFalse(statuses.isEmpty());
        assertEquals(BookingStatus.values().length, statuses.size());
        assertTrue(statuses.contains(BookingStatus.EN_ATTENTE));
        assertTrue(statuses.contains(BookingStatus.ACCEPTEE));
        assertTrue(statuses.contains(BookingStatus.REFUSEE));
        // Ajoutez d'autres statuts si votre enum en contient plus
    }

    @Test
    @DisplayName("Devrait récupérer les réservations d'un client avec filtres")
    void shouldGetBookingsByUserClientWithFilters() {
        // Préparation
        LocalDateTime startFilter = LocalDateTime.now().minusDays(1);
        LocalDateTime endFilter = LocalDateTime.now().plusDays(1);
        String orderBooking = "startingDateAsc";
        BookingStatus statusFilter = BookingStatus.EN_ATTENTE;

        List<Booking> expectedBookings = Arrays.asList(testBooking);
        when(bookingRepository.findBookingsByUserMyBookings(testUser, startFilter, endFilter, orderBooking, statusFilter))
                .thenReturn(expectedBookings);

        // Exécution
        List<Booking> actualBookings = bookingService.getBookingsByUserClient(testUser, startFilter, endFilter, orderBooking, statusFilter);

        // Vérification
        assertNotNull(actualBookings);
        assertEquals(1, actualBookings.size());
        assertEquals(expectedBookings, actualBookings);
        verify(bookingRepository, times(1)).findBookingsByUserMyBookings(testUser, startFilter, endFilter, orderBooking, statusFilter);
    }

    @Test
    @DisplayName("Devrait récupérer les réservations d'un propriétaire")
    void shouldGetBookingsByUserOwner() {
        // Préparation
        Booking ownerBooking1 = new Booking(2L, UUID.randomUUID(), new User(), null, testTerminal, null, "OWNER1", BookingStatus.ACCEPTEE, BigDecimal.valueOf(100), LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDateTime.now());
        // testTerminal est lié à testUser en tant que propriétaire
        List<Booking> expectedBookings = Arrays.asList(ownerBooking1);
        when(bookingRepository.findBookingsByUserOwner(testUser)).thenReturn(expectedBookings);

        // Exécution
        List<Booking> actualBookings = bookingService.getBookingsByUserOwner(testUser);

        // Vérification
        assertNotNull(actualBookings);
        assertEquals(1, actualBookings.size());
        assertEquals(expectedBookings, actualBookings);
        verify(bookingRepository, times(1)).findBookingsByUserOwner(testUser);
    }

    @Test
    @DisplayName("Devrait récupérer les réservations par utilisateur et statut")
    void shouldGetBookingsByUserAndStatusBooking() {
        // Préparation
        List<Booking> expectedBookings = Arrays.asList(testBooking); // testBooking est EN_ATTENTE
        when(bookingRepository.findBookingsByUserAndStatusBooking(testUser, BookingStatus.EN_ATTENTE))
                .thenReturn(expectedBookings);

        // Exécution
        List<Booking> actualBookings = bookingService.getBookingsByUserAndStatusBooking(testUser, BookingStatus.EN_ATTENTE);

        // Vérification
        assertNotNull(actualBookings);
        assertEquals(1, actualBookings.size());
        assertEquals(expectedBookings, actualBookings);
        verify(bookingRepository, times(1)).findBookingsByUserAndStatusBooking(testUser, BookingStatus.EN_ATTENTE);
    }
}
