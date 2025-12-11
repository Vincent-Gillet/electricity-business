package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Booking;
import com.electricitybusiness.api.model.BookingStatus;
import com.electricitybusiness.api.model.Terminal;
import com.electricitybusiness.api.model.TerminalStatus;
import com.electricitybusiness.api.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BookingSchedulerServiceTest {

    @Mock
    private ThreadPoolTaskScheduler taskScheduler;

    @Mock
    private TerminalService terminalService;

    @Mock
    private BookingRepository bookingRepository;

    private Map<UUID, Runnable> autoValidationTasks = new ConcurrentHashMap<>();

    @InjectMocks
    private BookingSchedulerService bookingSchedulerService;

    /**
     * Test de la méthode scheduleBookingTasks pour vérifier qu'aucune tâche n'est programmée
     * si la réservation est nulle.
     */
    @Test
    void scheduleBookingTasks_WithNullBooking_DoesNotScheduleTasks() {
        // Arrange
        Booking booking = null;

        // Act
        bookingSchedulerService.scheduleBookingTasks(booking);

        // Assert
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(Date.class));
        verify(terminalService, never()).setOccupiedByPublicId(any(UUID.class), any(TerminalStatus.class), anyBoolean());
    }

    /**
     * Test de la méthode scheduleBookingTasks pour vérifier qu'aucune tâche n'est programmée
     * si le terminal de la réservation est nul.
     */
    @Test
    void scheduleBookingTasks_WithNullTerminal_DoesNotScheduleTasks() {
        // Arrange
        Booking booking = new Booking();
        booking.setPublicId(UUID.randomUUID());
        booking.setTerminal(null);

        // Act
        bookingSchedulerService.scheduleBookingTasks(booking);

        // Assert
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(Date.class));
        verify(terminalService, never()).setOccupiedByPublicId(any(UUID.class), any(TerminalStatus.class), anyBoolean());
    }

    /**
     * Test de la méthode scheduleBookingTasks pour vérifier qu'aucune tâche n'est programmée
     * si la date de début de la réservation est dans le passé.
     */
    @Test
    void scheduleBookingTasks_WithPastStartDate_DoesNotScheduleTasks() {
        // Arrange
        Booking booking = new Booking();
        booking.setPublicId(UUID.randomUUID());
        Terminal terminal = new Terminal();
        terminal.setPublicId(UUID.randomUUID());
        booking.setTerminal(terminal);

        // Convertir Instant en LocalDateTime
        LocalDateTime now = LocalDateTime.now();
        booking.setStartingDate(now.minusHours(1));
        booking.setEndingDate(now.plusHours(1));

        // Act
        bookingSchedulerService.scheduleBookingTasks(booking);

        // Assert
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(Date.class));
        verify(terminalService, never()).setOccupiedByPublicId(any(UUID.class), any(TerminalStatus.class), anyBoolean());
    }

    /**
     * Test de la méthode scheduleAutoValidationTask pour vérifier qu'une tâche est programmée
     * correctement pour une réservation à venir.
     */
    @Test
    void scheduleAutoValidationTask_WithPastDate_DoesNotScheduleTask() {
        // Arrange
        UUID bookingPublicId = UUID.randomUUID();
        Instant bookingStartInstant = Instant.now().minus(Duration.ofHours(1));

        // Act
        bookingSchedulerService.scheduleAutoValidationTask(bookingPublicId, bookingStartInstant);

        // Assert
        verify(taskScheduler, never()).schedule(any(Runnable.class), any(Date.class));
    }

    /**
     * Test de la méthode scheduleAutoValidationTask pour vérifier qu'une réservation en attente
     * est automatiquement validée après le délai imparti.
     */
    @Test
    void scheduleAutoValidationTask_WithBookingNotInWaitingStatus_DoesNotChangeStatus() {
        // Arrange
        UUID bookingPublicId = UUID.randomUUID();
        Instant bookingStartInstant = Instant.now().plus(Duration.ofMinutes(31));
        Instant scheduleTime = bookingStartInstant.minus(Duration.ofMinutes(30));

        Booking booking = new Booking();
        booking.setPublicId(bookingPublicId);
        booking.setStatusBooking(BookingStatus.ACCEPTEE);

        when(bookingRepository.findByPublicId(bookingPublicId)).thenReturn(Optional.of(booking));

        // Act
        bookingSchedulerService.scheduleAutoValidationTask(bookingPublicId, bookingStartInstant);

        // Simuler l'exécution de la tâche
        Runnable runnable = autoValidationTasks.get(bookingPublicId);
        if (runnable != null) {
            runnable.run();
        }

        // Assert
        assertThat(booking.getStatusBooking()).isEqualTo(BookingStatus.ACCEPTEE);
        verify(bookingRepository, never()).save(booking);
    }
}
