package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Booking;
import com.electricitybusiness.api.model.BookingStatus;
import com.electricitybusiness.api.model.TerminalStatus;
import com.electricitybusiness.api.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class BookingSchedulerService {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final TerminalService terminalService;
    private final BookingRepository bookingRepository;

    private final Map<UUID, List<ScheduledFuture<?>>> futures = new HashMap<>();

    private final Map<UUID, ScheduledFuture<?>> autoValidationTasks = new HashMap<>();

    /**
     * Planifie les tâches pour occuper et libérer un terminal en fonction des dates
     * de début et de fin d'une réservation.
     * @param booking La réservation pour laquelle planifier les tâches.
     */
    public void scheduleBookingTasks(Booking booking) {
        if (booking == null || booking.getPublicId() == null) return;

        UUID bookingId = booking.getPublicId();
        UUID terminalPublicId = booking.getTerminal() != null ? booking.getTerminal().getPublicId() : null;
        if (terminalPublicId == null) return;

        // Annuler les tâches existantes pour cette réservation
        cancelBookingTasks(bookingId);

        Instant nowIf = Instant.now();
        Instant startDateIf = booking.getStartingDate().atZone(ZoneId.systemDefault()).toInstant();
        Instant endDateIf = booking.getEndingDate().atZone(ZoneId.systemDefault()).toInstant();

        // Vérifier si les dates sont dans le futur
        if (startDateIf.isBefore(nowIf) || endDateIf.isBefore(nowIf)) {
            return;
        }

        Date startDate = Date.from(booking.getStartingDate().atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(booking.getEndingDate().atZone(ZoneId.systemDefault()).toInstant());

        Runnable occupyTask = () -> {
            try {
                terminalService.setOccupiedByPublicId(terminalPublicId, TerminalStatus.OCCUPEE, true);
            } catch (Exception ignored) {}
        };

        Runnable freeTask = () -> {
            try {
                terminalService.setOccupiedByPublicId(terminalPublicId, TerminalStatus.LIBRE, false);
            } catch (Exception ignored) {}
        };

        ScheduledFuture<?> f1 = taskScheduler.schedule(occupyTask, startDate);
        ScheduledFuture<?> f2 = taskScheduler.schedule(freeTask, endDate);

            futures.put(bookingId, Arrays.asList(f1, f2));
    }

    /**
     * Planifie une tâche pour valider automatiquement une réservation si elle est toujours
     * en statut PENDING 30 minutes avant son démarrage.
     * @param bookingPublicId L'identifiant public de la réservation.
     * @param bookingStartInstant L'instant de début de la réservation (en UTC).
     */
    public void scheduleAutoValidationTask(UUID bookingPublicId, Instant bookingStartInstant) {
        Instant now = Instant.now();
        Instant scheduleTime = bookingStartInstant.minus(Duration.ofMinutes(30));

        // Ne pas planifier si l'heure est déjà passée
        if (scheduleTime.isBefore(now)) {
            return;
        }

        // La tâche à exécuter
        Runnable autoValidationRunnable = () -> {
            Optional<Booking> bookingOpt = bookingRepository.findByPublicId(bookingPublicId);
            bookingOpt.ifPresent(booking -> {
                // Vérifier si le statut est toujours EN_ATTENTE
                if (booking.getStatusBooking() == BookingStatus.EN_ATTENTE) {
                    booking.setStatusBooking(BookingStatus.ACCEPTEE);
                    bookingRepository.save(booking);

                    // Planifier les tâches d'occupation et de libération du terminal
                    scheduleBookingTasks(booking);
                } else {
                    System.out.println("La réservation " + bookingPublicId + " n'est plus en attente, l'auto-validation est ignorée.");
                }
            });

            // Retirer la tâche de la map après exécution
            autoValidationTasks.remove(bookingPublicId);
        };

        // Planifier la tâche
        ScheduledFuture<?> future = taskScheduler.schedule(autoValidationRunnable, scheduleTime);
        autoValidationTasks.put(bookingPublicId, future);
    }



    /**
     * Annule toutes les tâches planifiées pour une réservation spécifique.
     * Utile lors de la suppression ou de la modification d'une réservation.
     * @param bookingId L'identifiant public de la réservation.
     */
    public void cancelBookingTasks(UUID bookingId) {
        List<ScheduledFuture<?>> list = futures.remove(bookingId);
        if (list != null) {
            list.forEach(f -> { if (f != null) f.cancel(false); });
        }
    }

    /**
     * Annule la tâche d'auto-validation pour une réservation spécifique.
     * Utile si l'utilisateur valide manuellement la réservation avant l'heure prévue.
     * @param bookingPublicId L'identifiant public de la réservation.
     */
    public void cancelAutoValidationTask(UUID bookingPublicId) {
        ScheduledFuture<?> future = autoValidationTasks.remove(bookingPublicId);
        if (future != null) {
            future.cancel(false);
            System.out.println("Tâche d'auto-validation annulée pour la réservation : " + bookingPublicId);
        }
    }
}
