package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Booking;
import com.electricitybusiness.api.model.TerminalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class BookingSchedulerService {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final TerminalService terminalService;

    private final Map<UUID, List<ScheduledFuture<?>>> futures = new HashMap<>();

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

    public void cancelBookingTasks(UUID bookingId) {
        List<ScheduledFuture<?>> list = futures.remove(bookingId);
        if (list != null) {
            list.forEach(f -> { if (f != null) f.cancel(false); });
        }
    }
}
