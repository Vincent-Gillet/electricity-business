package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@EntityScan(basePackages = {"com.electricitybusiness.api.model"})
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private User user2;
    private Terminal testTerminal1;
    private Terminal testTerminal2;

    private Booking booking1;
    private Booking booking2;
    private Booking booking3;
    private Booking booking4;

    @BeforeEach
    void setUp() {
        // Nettoyage avant chaque test
        entityManager.clear();
        bookingRepository.deleteAll();

        // Création des utilisateurs
        user1 = User.builder()
                .surnameUser("Doe")
                .firstName("John")
                .pseudo("johndoe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .phone("1234567890")
                .emailUser("john.doe@example.com")
                .passwordUser("password123")
                .role(UserRole.USER)
                .banished(false)
                .build();

        user2 = User.builder()
                .surnameUser("Smith")
                .firstName("Jane")
                .pseudo("otheruser")
                .dateOfBirth(LocalDate.of(1985, 8, 20))
                .phone("0987654321")
                .emailUser("john.smith@example.com")
                .passwordUser("password456")
                .role(UserRole.USER)
                .banished(false)
                .build();

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        testTerminal1 = new Terminal(
                null,
                UUID.randomUUID(),
                "Terminal Alpha",
                BigDecimal.valueOf(48.8566),
                BigDecimal.valueOf(2.3522),
                BigDecimal.valueOf(3.7),
                BigDecimal.valueOf(22.0),
                "Type2",
                true,
                TerminalStatus.LIBRE,
                false,
                LocalDateTime.now().minusDays(1),
                null,
                null,
                null,
                null,
                null,
                null
        );

        testTerminal2 = new Terminal(
                null,
                UUID.randomUUID(),
                "Terminal Beta",
                BigDecimal.valueOf(34.0522),
                BigDecimal.valueOf(-118.2437),
                BigDecimal.valueOf(7.4),
                BigDecimal.valueOf(50.0),
                "CCS",
                true,
                TerminalStatus.LIBRE,
                false,
                LocalDateTime.now().minusDays(1),
                null,
                null,
                null,
                null,
                null,
                null
        );

        entityManager.persist(testTerminal1);
        entityManager.persist(testTerminal2);
        entityManager.flush();

        LocalDateTime now = LocalDateTime.now();

        // Création des réservations pour user1
        booking1 = new Booking(
                null,
                UUID.randomUUID(),
                user1,
                null,
                testTerminal1,
                null,
                "CODE001",
                BookingStatus.ACCEPTEE,
                BigDecimal.valueOf(25.00),
                now.minusMonths(1),
                now.plusDays(5),
                now.plusDays(5).plusHours(2)
        );
        booking2 = new Booking(
                null,
                UUID.randomUUID(),
                user1,
                null,
                testTerminal2,
                null,
                "CODE002",
                BookingStatus.EN_ATTENTE,
                BigDecimal.valueOf(30.00),
                now.minusMonths(2),
                now.plusDays(10),
                now.plusDays(10).plusHours(2)
        );

        booking3 = new Booking(
                null,
                UUID.randomUUID(),
                user1,
                null,
                testTerminal1,
                null,
                "CODE003",
                BookingStatus.REFUSEE,
                BigDecimal.valueOf(20.00),
                now.minusMonths(3),
                now.plusDays(15),
                now.plusDays(15).plusHours(2)
        );

        booking4 = new Booking(
                null,
                UUID.randomUUID(),
                user1,
                null,
                testTerminal2,
                null,
                "CODE004",
                BookingStatus.ACCEPTEE,
                BigDecimal.valueOf(35.00),
                now.minusMonths(4),
                now.plusMonths(1),
                now.plusMonths(1).plusHours(2)
        );

        // Réservation pour otherUser
        Booking otherUserBooking = new Booking(
                null,
                UUID.randomUUID(),
                user2,
                null,
                testTerminal1,
                null,
                "CODEOTHER",
                BookingStatus.ACCEPTEE,
                BigDecimal.valueOf(10.00),
                now.minusMonths(5),
                now.plusWeeks(1),
                now.plusWeeks(1).plusHours(1)
        );

        entityManager.persist(booking1);
        entityManager.persist(booking2);
        entityManager.persist(booking3);
        entityManager.persist(booking4);
        entityManager.persist(otherUserBooking);
        entityManager.flush();
    }

    /**
     * Teste la récupération des réservations pour un utilisateur sans aucun autre filtre.
     * Ordre par défaut ASC.
     */
    @Test
    void findBookingsByUserMyBookings_OnlyUser_ReturnsAllBookingsOrderedAsc() {
        List<Booking> bookings = bookingRepository.findBookingsByUserMyBookings(
                user1, null, null, "ASC", null);

        assertThat(bookings).hasSize(4);
        assertThat(bookings.get(0).getPublicId()).isEqualTo(booking1.getPublicId());
        assertThat(bookings.get(1).getPublicId()).isEqualTo(booking2.getPublicId());
        assertThat(bookings.get(2).getPublicId()).isEqualTo(booking3.getPublicId());
        assertThat(bookings.get(3).getPublicId()).isEqualTo(booking4.getPublicId());
    }

    /**
     * Teste la récupération des réservations avec un orderBooking='DESC'.
     */
    @Test
    void findBookingsByUserMyBookings_OnlyUser_ReturnsAllBookingsOrderedDesc() {
        List<Booking> bookings = bookingRepository.findBookingsByUserMyBookings(
                user1, null, null, "DESC", null);

        assertThat(bookings).hasSize(4);
        assertThat(bookings.get(0).getPublicId()).isEqualTo(booking4.getPublicId());
        assertThat(bookings.get(1).getPublicId()).isEqualTo(booking3.getPublicId());
        assertThat(bookings.get(2).getPublicId()).isEqualTo(booking2.getPublicId());
        assertThat(bookings.get(3).getPublicId()).isEqualTo(booking1.getPublicId());
    }

    /**
     * Teste la récupération des réservations avec un orderBooking null (devrait être ASC par défaut de la requête).
     */
    @Test
    void findBookingsByUserMyBookings_NullOrder_ReturnsAllBookingsOrderedAsc() {
        List<Booking> bookings = bookingRepository.findBookingsByUserMyBookings(
                user1, null, null, null, null);

        assertThat(bookings).hasSize(4);
        assertThat(bookings.get(0).getPublicId()).isEqualTo(booking1.getPublicId()); // 2023-01-05
        assertThat(bookings.get(1).getPublicId()).isEqualTo(booking2.getPublicId()); // 2023-01-10
        assertThat(bookings.get(2).getPublicId()).isEqualTo(booking3.getPublicId()); // 2023-01-15
        assertThat(bookings.get(3).getPublicId()).isEqualTo(booking4.getPublicId()); // 2023-02-01
    }

    /**
     * Teste la récupération des réservations par plage de dates.
     */
    @Test
    void findBookingsByUserMyBookings_WithStartingDateAndEndingDate_ReturnsFilteredBookings() {
        LocalDateTime filterStartingDate = LocalDateTime.now().plusDays(6);
        LocalDateTime filterEndingDate = LocalDateTime.now().plusDays(32);

        List<Booking> bookings = bookingRepository.findBookingsByUserMyBookings(
                user1, filterStartingDate, filterEndingDate, "ASC", null);

        assertThat(bookings).hasSize(3);
        assertThat(bookings.get(0).getPublicId()).isEqualTo(booking2.getPublicId());
        assertThat(bookings.get(1).getPublicId()).isEqualTo(booking3.getPublicId());
        assertThat(bookings.get(2).getPublicId()).isEqualTo(booking4.getPublicId());
    }

    /**
     * Teste la récupération des réservations par statut.
     */
    @Test
    void findBookingsByUserMyBookings_WithStatus_ReturnsFilteredBookings() {
        List<Booking> bookings = bookingRepository.findBookingsByUserMyBookings(
                user1, null, null, "ASC", BookingStatus.ACCEPTEE);

        assertThat(bookings).hasSize(2);
        assertThat(bookings.get(0).getPublicId()).isEqualTo(booking1.getPublicId()); // 2023-01-10
        assertThat(bookings.get(1).getPublicId()).isEqualTo(booking4.getPublicId()); // 2023-02-01
    }

    /**
     * Teste la récupération des réservations par le nouveau statut EN_ATTENTE.
     */
    @Test
    void findBookingsByUserMyBookings_WithEnAttenteStatus_ReturnsFilteredBookings() {
        List<Booking> bookings = bookingRepository.findBookingsByUserMyBookings(
                user1, null, null, "ASC", BookingStatus.EN_ATTENTE);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getPublicId()).isEqualTo(booking2.getPublicId()); // 2023-01-05
    }


    /**
     * Teste la récupération des réservations avec tous les filtres : dates et statut.
     * Et un ordre DESC.
     */
    @Test
    void findBookingsByUserMyBookings_WithAllFiltersAndDescOrder_ReturnsFilteredBookings() {
        LocalDateTime filterStartingDate = LocalDateTime.now().plusDays(3);
        LocalDateTime filterEndingDate = LocalDateTime.now().plusDays(6);

        List<Booking> bookings = bookingRepository.findBookingsByUserMyBookings(
                user1, filterStartingDate, filterEndingDate, "DESC", BookingStatus.ACCEPTEE);

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getPublicId()).isEqualTo(booking1.getPublicId());
    }

    /**
     * Teste le cas où aucun booking ne correspond aux critères.
     */
    @Test
    void findBookingsByUserMyBookings_NoMatchingBookings_ReturnsEmptyList() {
        LocalDateTime futureSartingDate = LocalDateTime.now().minusYears(3);
        LocalDateTime futureEndingDate = LocalDateTime.now().minusYears(2);

        List<Booking> bookings = bookingRepository.findBookingsByUserMyBookings(
                user1, futureSartingDate, futureEndingDate, "ASC", null);

        assertThat(bookings).isEmpty();
    }
}
