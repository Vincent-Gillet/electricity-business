package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.booking.BookingStatusDTO;
import com.electricitybusiness.api.model.*;
import com.electricitybusiness.api.repository.BookingRepository;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingSchedulerService bookingSchedulerService;

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private Clock clock;

    private User testUser;
    private Terminal testTerminal;
    private Booking testBooking;
    private Booking validBooking;
    private UUID testPublicId;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setIdUser(1L);
        testUser.setFirstName("John");
        testUser.setSurnameUser("Doe");
        testUser.setEmailUser("john.doe@example.com");

        Address address = new Address();
        address.setIdAddress(1L);
        address.setAddress("123 Main St");

        Place testPlace = new Place();
        testPlace.setIdPlace(1L);
        testPlace.setUser(testUser);
        testPlace.setAddress(address);

        testTerminal = new Terminal();
        testTerminal.setIdTerminal(10L);
        testTerminal.setNameTerminal("Terminal_A");
        testTerminal.setPublicId(UUID.randomUUID());
        testTerminal.setUser(testUser);
        testTerminal.setPlace(testPlace);

        testPublicId = UUID.randomUUID();
        testBooking = new Booking(
                1L,
                testPublicId,
                testUser,
                null,
                testTerminal,
                null,
                "123456789",
                BookingStatus.EN_ATTENTE,
                BigDecimal.valueOf(50.00),
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(3)
        );

        validBooking = new Booking(
                1L,
                UUID.randomUUID(),
                testUser,
                null,
                testTerminal,
                null,
                "123456789",
                BookingStatus.EN_ATTENTE,
                BigDecimal.valueOf(50.00),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(8)
        );
    }

    /**
     * Tests pour la classe BookingService
     */
    @Test
    void shouldGetAllBookings() {
        // Préparation (Arrange)
        Booking booking2 = new Booking(2L, UUID.randomUUID(), testUser, null, testTerminal, null, "987654321", BookingStatus.ACCEPTEE, BigDecimal.valueOf(75.00), LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), LocalDateTime.now());
        List<Booking> expectedBookings = Arrays.asList(testBooking, booking2);
        when(bookingRepository.findAll()).thenReturn(expectedBookings);

        // Exécution (Act)
        List<Booking> actualBookings = bookingService.getAllBookings();

        // Vérification (Assert)
        assertNotNull(actualBookings);
        assertEquals(2, actualBookings.size());
        assertEquals(expectedBookings, actualBookings);
        verify(bookingRepository, times(1)).findAll();
    }

    /**
     * Tests pour la méthode getBookingById
     */
    @Test
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

    /**
     * Tests pour la méthode getBookingById lorsque la réservation n'existe pas
     */
    @Test
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

    /**
     * Tests pour la méthode saveBooking lorsque la date de début est loin dans le futur
     */
    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void shouldSaveBookingWithPendingStatusWhenStartDateIsFarInFuture() {
        // Préparation
        ZoneId serviceProcessingZone = ZoneId.of("Europe/Paris");

        LocalDateTime testExecutionBaseNow = LocalDateTime.now();

        Instant fixedMockInstant = testExecutionBaseNow
                .atZone(serviceProcessingZone)
                .toInstant();

        when(clock.instant()).thenReturn(fixedMockInstant);
        when(clock.getZone()).thenReturn(serviceProcessingZone);

        LocalDateTime servicePerceivedNow = LocalDateTime.ofInstant(fixedMockInstant, serviceProcessingZone);

        LocalDateTime paymentDate = servicePerceivedNow;
        LocalDateTime startDate = servicePerceivedNow.plusDays(1);
        LocalDateTime endDate = servicePerceivedNow.plusDays(1).plusHours(3);

        Booking initialBooking = new Booking(
                1L,
                UUID.randomUUID(),
                testUser,
                null,
                testTerminal,
                null,
                "123456789",
                BookingStatus.EN_ATTENTE,
                BigDecimal.valueOf(50.00),
                paymentDate,
                startDate,
                endDate
        );

        // Mock du BookingSchedulerService
        doNothing().when(bookingSchedulerService).scheduleAutoValidationTask(any(UUID.class), any(Instant.class));
        doNothing().when(bookingSchedulerService).scheduleBookingTasks(any(Booking.class));

        when(bookingRepository.save(any(Booking.class))).thenReturn(initialBooking);
        when(bookingRepository.findOverlappingBookings(any(), any(), any())).thenReturn(Collections.emptyList());

        // Exécution
        Booking savedBooking = bookingService.saveBooking(initialBooking);

        // Vérification
        assertEquals(initialBooking, savedBooking);
        assertEquals(BookingStatus.EN_ATTENTE, savedBooking.getStatusBooking());

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository, times(1)).save(bookingCaptor.capture());
        Booking capturedBooking = bookingCaptor.getValue();

        assertEquals(BookingStatus.EN_ATTENTE, capturedBooking.getStatusBooking());

        verify(bookingSchedulerService, times(1)).scheduleAutoValidationTask(any(UUID.class), any(Instant.class));
        verify(bookingSchedulerService, times(1)).scheduleBookingTasks(initialBooking);
    }

    /**
     * Tests pour la méthode updateBooking
     */
    @Test
    void shouldUpdateBookingById() {
        // Préparation
        Booking updatedInfo = new Booking();
        updatedInfo.setStatusBooking(BookingStatus.ACCEPTEE);
        updatedInfo.setTotalAmount(BigDecimal.valueOf(60.00));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking bookingArg = invocation.getArgument(0);
            assertEquals(testBooking.getIdBooking(), bookingArg.getIdBooking());
            bookingArg.setStatusBooking(updatedInfo.getStatusBooking());
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

    /**
     * Tests pour la méthode deleteBookingById
     */
    @Test
    void shouldDeleteBookingById() {
        // Préparation (on ne teste pas la logique de recherche ici, juste l'appel à deleteById)
        doNothing().when(bookingRepository).deleteById(testBooking.getIdBooking());

        // Exécution
        bookingService.deleteBookingById(testBooking.getIdBooking());

        // Vérification
        verify(bookingRepository, times(1)).deleteById(testBooking.getIdBooking());
    }

    /**
     * Tests pour la méthode existsById
     */
    @Test
    void shouldCheckExistenceById() {
        // Préparation
        when(bookingRepository.existsById(testBooking.getIdBooking())).thenReturn(true);

        // Exécution
        boolean exists = bookingService.existsById(testBooking.getIdBooking());

        // Vérification
        assertTrue(exists);
        verify(bookingRepository, times(1)).existsById(testBooking.getIdBooking());
    }

    /**
     * Tests pour la méthode findByUser
     */
    @Test
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

    /**
     * Tests pour la méthode findByTerminal
     */
    @Test
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

    /**
     * Tests pour la méthode findByStatusBooking
     */
    @Test
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

    /**
     * Tests pour la méthode getBookingByPublicId
     */
    @Test
    void shouldDeleteBookingByPublicId() {
        // Préparation
        doNothing().when(bookingRepository).deleteBookingByPublicId(testPublicId);

        // Exécution
        bookingService.deleteBookingByPublicId(testPublicId);

        // Vérification
        verify(bookingRepository, times(1)).deleteBookingByPublicId(testPublicId);
    }

    /**
     * Tests pour la méthode existsByPublicId
     */
    @Test
    void shouldCheckExistenceByPublicId() {
        // Préparation
        when(bookingRepository.findByPublicId(testPublicId)).thenReturn(Optional.of(testBooking));

        // Exécution
        boolean exists = bookingService.existsByPublicId(testPublicId);

        // Vérification
        assertTrue(exists);
        verify(bookingRepository, times(1)).findByPublicId(testPublicId);
    }

    /**
     * Tests pour la méthode existsByPublicId lorsque la réservation n'existe pas
     */
    @Test
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

    /**
     * Tests pour la méthode updateBooking par publicId
     */
    @Test
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
            return savedBooking;
        });

        Booking actualResult = bookingService.updateBooking(testPublicId, updatedBookingPayload);

        assertNotNull(actualResult);
        assertEquals(BookingStatus.ACCEPTEE, actualResult.getStatusBooking());
        assertEquals(fullyPopulatedExistingUser, actualResult.getUser());
    }

    /**
     * Tests pour la méthode updateBooking par publicId lorsque la réservation n'existe pas
     */
    @Test
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

    /**
     * Tests pour la méthode updateBookingStatus
     */
    @Test
    void shouldUpdateBookingStatus() {
        // Préparation
        BookingStatusDTO dto = new BookingStatusDTO(BookingStatus.REFUSEE);
        Booking bookingToUpdate = new Booking(testBooking.getIdBooking(), testPublicId, testUser, null, testTerminal, null, "123", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(50), LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDateTime.now());

        when(bookingRepository.findByPublicId(testPublicId)).thenReturn(Optional.of(bookingToUpdate));
        when(bookingRepository.saveAndFlush(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            assertEquals(BookingStatus.REFUSEE, savedBooking.getStatusBooking());
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

    /**
     * Tests pour la méthode updateBookingStatus lorsque la réservation n'existe pas
     */
    @Test
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

    /**
     * Tests pour la méthode getAllBookingStatus
     */
    @Test
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
    }

    /**
     * Tests pour la méthode getBookingsByUserClient avec filtres
     */
    @Test
    void shouldGetBookingsByUserClientWithFilters() {
        // Préparation
        LocalDateTime startFilter = LocalDateTime.now().minusDays(1);
        LocalDateTime endFilter = LocalDateTime.now().plusDays(1);
        String orderBooking = "ASC";
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

    /**
     * Tests pour la méthode getBookingsByUserOwner
     */
    @Test
    void shouldGetBookingsByUserOwner() {
        // Préparation
        Booking ownerBooking1 = new Booking(2L, UUID.randomUUID(), new User(), null, testTerminal, null, "OWNER1", BookingStatus.ACCEPTEE, BigDecimal.valueOf(100), LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDateTime.now());
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

    /**
     * Tests pour la méthode getBookingsByUserAndStatusBooking
     */
    @Test
    void shouldGetBookingsByUserAndStatusBooking() {
        // Préparation
        List<Booking> expectedBookings = Arrays.asList(testBooking);
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

    /**
     * Tests pour la méthode saveBooking avec une réservation valide
     */
    @Test
    void testSaveBooking_ValidBooking() {
        ZoneId serviceProcessingZone = ZoneId.of("Europe/Paris");
        Instant fixedInstantForTest = LocalDateTime.now()
                .plusMinutes(10)
                .atZone(serviceProcessingZone)
                .toInstant();

        when(clock.instant()).thenReturn(fixedInstantForTest);

        LocalDateTime servicePerceivedNow = LocalDateTime.ofInstant(fixedInstantForTest, serviceProcessingZone);
        LocalDateTime paymentDate = servicePerceivedNow;
        LocalDateTime futureStartDate = servicePerceivedNow.plusHours(1);
        LocalDateTime futureEndDate = futureStartDate.plusHours(3);

        validBooking.setPaymentDate(paymentDate);
        validBooking.setStartingDate(futureStartDate);
        validBooking.setEndingDate(futureEndDate);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());
        doNothing().when(bookingSchedulerService).scheduleAutoValidationTask(any(UUID.class), any(Instant.class));

        Booking savedBooking = bookingService.saveBooking(validBooking);

        assertNotNull(savedBooking);
        assertEquals(BookingStatus.EN_ATTENTE, savedBooking.getStatusBooking());
        assertEquals(validBooking.getIdBooking(), savedBooking.getIdBooking());
        assertEquals(validBooking.getTerminal(), savedBooking.getTerminal());

        verify(bookingRepository, times(1)).save(argThat(booking ->
                booking.getStartingDate().equals(futureStartDate) &&
                        booking.getEndingDate().equals(futureEndDate) &&
                        booking.getStatusBooking().equals(BookingStatus.EN_ATTENTE)
        ));

        verify(bookingSchedulerService, times(1)).scheduleAutoValidationTask(
                eq(savedBooking.getPublicId()),
                eq(futureStartDate.atZone(serviceProcessingZone).toInstant())
        );
    }

    /**
     * Tests pour la méthode generateBookingPdf avec une réservation valide
     */
    @Test
    void testGenerateBookingPdf_ValidBooking() throws Exception {
        when(bookingRepository.findByPublicId(validBooking.getPublicId())).thenReturn(Optional.of(validBooking));

        byte[] pdf = bookingService.generateBookingPdf(validBooking.getPublicId());

        assertThat(pdf).isNotNull();
        assertThat(pdf.length).isGreaterThan(0);
        verify(bookingRepository).findByPublicId(validBooking.getPublicId());
    }

    /**
     * Tests pour la méthode generateBookingExcel avec des réservations pour un utilisateur
     */
    @Test
    void testGenerateBookingExcel_WithBookings() throws Exception {
        User user = new User();
        List<Booking> bookings = List.of(validBooking, validBooking);
        when(bookingRepository.findByUser(user)).thenReturn(bookings);

        byte[] excel = bookingService.generateBookingExcel(user);

        assertThat(excel).isNotNull();
        assertThat(excel.length).isGreaterThan(0);
    }

    /**
     * Tests pour la méthode createCell (méthode privée) utilisée dans la génération de PDF
     */
    @Test
    void testCreateCell_WithParameters() throws Exception {
        PdfFont mockFont = mock(PdfFont.class);

        Cell cell = (Cell) ReflectionTestUtils.invokeMethod(
                bookingService,
                "createCell",
                "Test",
                mockFont,
                12f
        );

        assertNotNull(cell);
        assertFalse(cell.getChildren().isEmpty());

        // First child should be a Paragraph
        IElement firstChild = cell.getChildren().get(0);
        assertTrue(firstChild instanceof Paragraph);
        Paragraph paragraph = (Paragraph) firstChild;

        // Paragraph should have children (Text elements)
        assertFalse(paragraph.getChildren().isEmpty());

        // Collect text from Text children and assert it contains "Test"
        StringBuilder combined = new StringBuilder();
        for (IElement element : paragraph.getChildren()) {
            if (element instanceof Text) {
                combined.append(((Text) element).getText());
            } else {
                combined.append(element.toString());
            }
        }

        assertThat(combined.toString()).contains("Test");
    }
}
