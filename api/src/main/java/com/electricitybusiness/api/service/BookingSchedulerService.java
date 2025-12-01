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

        // cancel any existing tasks for this booking
        cancelBookingTasks(bookingId);

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
        Instant scheduleTime = bookingStartInstant.minus(Duration.ofMinutes(30));

        // Ne pas planifier si l'heure est déjà passée (ce cas est géré par la création immédiate)
        if (scheduleTime.isBefore(Instant.now())) {
            System.out.println("L'heure de planification de l'auto-validation est déjà passée pour la réservation " + bookingPublicId + ". Aucune action nécessaire.");
            return;
        }

        // La tâche à exécuter
        Runnable autoValidationRunnable = () -> {
            bookingRepository.findByPublicId(bookingPublicId).ifPresent(booking -> {
                // Vérifier si le statut est toujours EN_ATTENTE_VALIDATION
                if (booking.getStatusBooking() == BookingStatus.EN_ATTENTE) {
                    System.out.println("Validation automatique de la réservation : " + bookingPublicId + " (30 min avant le début).");
                    booking.setStatusBooking(BookingStatus.ACCEPTEE);
                    bookingRepository.save(booking); // Sauvegarder le nouveau statut
                    // Optionnel: si le changement de statut impacte d'autres tâches, vous pouvez les replanifier ici.
                    // scheduleBookingTasks(booking);
                } else {
                    System.out.println("La réservation " + bookingPublicId + " n'est plus en attente, l'auto-validation est ignorée.");
                }
            });
            autoValidationTasks.remove(bookingPublicId); // La tâche est exécutée, on peut la retirer de la map
        };

        // Planifier la tâche
        ScheduledFuture<?> future = taskScheduler.schedule(autoValidationRunnable, scheduleTime);
        autoValidationTasks.put(bookingPublicId, future); // Stocker la référence pour une éventuelle annulation
        System.out.println("Tâche d'auto-validation planifiée pour la réservation " + bookingPublicId + " à : " + scheduleTime);
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
            future.cancel(false); // false signifie ne pas interrompre si elle est déjà en cours (peu probable ici)
            System.out.println("Tâche d'auto-validation annulée pour la réservation : " + bookingPublicId);
        }
    }
}
