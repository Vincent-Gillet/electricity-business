package com.electricitybusiness.api.config;

import com.electricitybusiness.api.model.Booking;
import com.electricitybusiness.api.model.BookingStatus;
import com.electricitybusiness.api.repository.BookingRepository;
import com.electricitybusiness.api.service.BookingSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class Scheduler {
    private final BookingRepository bookingRepository;
    private final BookingSchedulerService bookingSchedulerService;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        // Au démarrage de l'application, reprogrammer les tâches pour les réservations confirmées futures
        LocalDateTime now = LocalDateTime.now();
        List<Booking> futureConfirmed = bookingRepository.findAllByStatusBookingAndEndingDateAfter(BookingStatus.ACCEPTEE, now);
        // Adapte la chaîne de statut à votre énumération/valeurs (par exemple "ACCEPTEE" ou énumération)
        for (Booking booking : futureConfirmed) {
            bookingSchedulerService.scheduleBookingTasks(booking);
        }
    }
}
