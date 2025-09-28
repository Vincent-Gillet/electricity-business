package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.Booking;
import com.electricitybusiness.api.model.BookingStatus;
import com.electricitybusiness.api.model.Terminal;
import com.electricitybusiness.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);

    List<Booking> findByTerminal(Terminal terminal);

    List<Booking> findByStatusBooking(BookingStatus status);

    List<Booking> findByUserAndStatusBooking(User user, BookingStatus status);

    List<Booking> findByTerminalAndStatusBooking(Terminal terminal, BookingStatus status);

}
