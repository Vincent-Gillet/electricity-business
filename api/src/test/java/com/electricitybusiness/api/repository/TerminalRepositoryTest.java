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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@EntityScan(basePackages = {"com.electricitybusiness.api.model"})
public class TerminalRepositoryTest {
    @Autowired
    private TerminalRepository terminalRepository;
    @Autowired
    private PlaceRepository placeRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MediaRepository mediaRepository;
    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user1;
    private Place place1;
    private Terminal terminal1;
    private Terminal terminal2;
    private Terminal terminal3;
    private Terminal terminalFar;
    private Terminal terminalAnotherPlace;
    private Place place2;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        terminalRepository.deleteAll();
        placeRepository.deleteAll();
        userRepository.deleteAll();
        addressRepository.deleteAll();
        mediaRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        Media media1 = new Media(
                null,
                "PhotoProfil",
                "http://example.com/pic.jpg",
                "image/jpeg",
                "Photo de profil",
                "100KB",
                LocalDateTime.now(),
                null,
                null,
                null,
                null);
        mediaRepository.save(media1);

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
                .media(media1)
                .build();
        userRepository.save(user1);

        Address address1 = new Address(
                null,
                UUID.randomUUID(),
                "Home Address",
                "10 Rue de la Paix",
                "75000",
                "Paris",
                "France",
                "",
                "",
                "",
                false,
                null,
                user1
        );
        entityManager.persist(address1);
        entityManager.flush();

        Address address2 = new Address(
                null,
                UUID.randomUUID(),
                "Work Address",
                "20 Avenue des Champs",
                "75008",
                "Paris",
                "France",
                "",
                "",
                "",
                false,
                null,
                user1
        );
        entityManager.persist(address2);
        entityManager.flush();

        place1 = new Place(
                null,
                UUID.randomUUID(),
                "Description Place 1",
                user1,
                null,
                address1);
        placeRepository.save(place1);

        place2 = new Place(
                null,
                UUID.randomUUID(),
                "Description Place 2",
                user1,
                null,
                address2);
        placeRepository.save(place2);

        terminal1 = new Terminal(null, UUID.randomUUID(), "Term1 - Occupied",
                new BigDecimal("48.86666700"), new BigDecimal("2.33333300"),
                new BigDecimal("2.40"), new BigDecimal("22.00"), "Instruction Term1",
                true, TerminalStatus.OCCUPEE,
                true, LocalDateTime.of(2017,2,3,6,30,40),
                LocalDateTime.of(2017,2,3,6,30,40),
                user1, null, place1, null, null);

        terminal2 = new Terminal(null, UUID.randomUUID(), "Term2 - Free",
                new BigDecimal("48.85661300"), new BigDecimal("2.35222200"),
                new BigDecimal("3.00"), new BigDecimal("30.00"), "Instruction 2",
                false, TerminalStatus.LIBRE,
                false, LocalDateTime.of(2018,3,4,7,40,50),
                LocalDateTime.of(2018,3,4,7,40,50),
                user1, null, place1, null, null);

        terminal3 = new Terminal(null, UUID.randomUUID(), "Term3 - Free",
                new BigDecimal("48.85884400"), new BigDecimal("2.29435100"),
                new BigDecimal("4.00"), new BigDecimal("40.00"), "Instruction 3",
                false, TerminalStatus.LIBRE,
                false, LocalDateTime.of(2019,4,5,8,50,59),
                LocalDateTime.of(2019,4,5,8,50,59),
                user1, null, place1, null, null);

        terminalFar = new Terminal(null, UUID.randomUUID(), "TermFar - Free",
                new BigDecimal("49.00000000"), new BigDecimal("2.00000000"),
                new BigDecimal("5.00"), new BigDecimal("50.00"), "Instruction Far",
                false, TerminalStatus.LIBRE,
                false, LocalDateTime.now(), LocalDateTime.now(),
                user1, null, place1, null, null);

        terminalAnotherPlace = new Terminal(null, UUID.randomUUID(), "TermAnotherPlace",
                new BigDecimal("48.86000000"), new BigDecimal("2.30000000"),
                new BigDecimal("3.50"), new BigDecimal("35.00"), "Instruction Another Place",
                false, TerminalStatus.LIBRE,
                false, LocalDateTime.now(), LocalDateTime.now(),
                user1, null, place2, null, null);

        terminalRepository.saveAll(List.of(terminal1, terminal2, terminal3, terminalFar, terminalAnotherPlace));

        LocalDateTime futureStartingDate = LocalDateTime.now().plusHours(1);
        LocalDateTime futureEndingDate = futureStartingDate.plusHours(2);
        LocalDateTime futurePaymentDate = futureStartingDate.minusMinutes(30);

        Booking booking1 = new Booking(null, UUID.randomUUID(), user1, null, terminal1, null,
                "1231923", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(48.85),
                futurePaymentDate,
                futureStartingDate,
                futureEndingDate
        );
        bookingRepository.save(booking1);

        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Test de la méthode searchTerminals du TerminalRepository avec tous les paramètres.
     * Vérifie que seuls les terminaux libres dans le rayon et sans réservation en conflit sont récupérés.
     */
    @Test
    void testSearchTerminals_allParameters() {
        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusDays(10);
        LocalDateTime endDate = LocalDateTime.now().plusDays(12);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        // Assertions
        assertThat(results).isNotNull();
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermAnotherPlace");
    }

    /**
     * Test de la méthode searchTerminals du TerminalRepository sans filtre d'occupation.
     * Vérifie que tous les terminaux dans le rayon sont récupérés, indépendamment de leur statut d'occupation.
     */
    @Test
    void testSearchTerminals_noOccupiedFilter() {
        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0;
        Boolean occupied = null;
        LocalDateTime startDate = LocalDateTime.now().plusDays(10);
        LocalDateTime endDate = LocalDateTime.now().plusDays(12);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermAnotherPlace");
    }

    /**
     * Test de la méthode searchTerminals du TerminalRepository avec une période de réservation en conflit (chevauchement total).
     * Vérifie que les terminaux avec des réservations en conflit sont exclus des résultats.
     */
    @Test
    void testSearchTerminals_withConflictingBooking() {
        Booking conflictingBooking = new Booking(null, UUID.randomUUID(), user1, null, terminal2, null,
                "CONFLICT", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(48.85),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(11), // Chevauche la période de recherche
                LocalDateTime.now().plusDays(13) // Chevauche la période de recherche
        );
        bookingRepository.save(conflictingBooking);

        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusDays(10);
        LocalDateTime endDate = LocalDateTime.now().plusDays(12);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term3 - Free", "TermAnotherPlace");
    }

    /**
     * Test de la méthode searchTerminals du TerminalRepository avec un rayon excluant certains terminaux.
     * Vérifie que seuls les terminaux dans le rayon spécifié sont récupérés.
     */
    @Test
    void testSearchTerminals_outsideRadius() {
        Terminal terminalFar = new Terminal(null, UUID.randomUUID(), "TermFar - Free",
                new BigDecimal("49.00000000"), new BigDecimal("2.00000000"), // Loin de (48.85, 2.35)
                new BigDecimal("5.00"), new BigDecimal("50.00"), "Instruction Far",
                false, TerminalStatus.LIBRE,
                false, LocalDateTime.now(), LocalDateTime.now(), user1, null, place1, null, null);
        terminalRepository.save(terminalFar);

        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 1.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusDays(10);
        LocalDateTime endDate = LocalDateTime.now().plusDays(12);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term2 - Free");
    }

    /**
     * Test de la méthode findByPlace du TerminalRepository.
     * Vérifie que les terminaux associés à un lieu spécifique sont correctement récupérés.
     */
    @Test
    void testFindByPlace() {
        List<Terminal> terminalsInPlace1 = terminalRepository.findByPlace(place1);
        assertThat(terminalsInPlace1).isNotNull();
        assertThat(terminalsInPlace1).hasSize(4);
        assertThat(terminalsInPlace1).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder(terminal1.getNameTerminal(), terminal2.getNameTerminal(), terminal3.getNameTerminal(), terminalFar.getNameTerminal());

        List<Terminal> terminalsInPlace2 = terminalRepository.findByPlace(place2);
        assertThat(terminalsInPlace2).isNotNull();
        assertThat(terminalsInPlace2).hasSize(1);
        assertThat(terminalsInPlace2).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder(terminalAnotherPlace.getNameTerminal());
    }

    /**
     * Test de la méthode findByStatusTerminal du TerminalRepository.
     * Vérifie que les terminaux avec un statut spécifique sont correctement récupérés.
     */
    @Test
    void testFindByStatusTerminal_LIBRE() {
        List<Terminal> libreTerminals = terminalRepository.findByStatusTerminal(TerminalStatus.LIBRE);
        assertThat(libreTerminals).isNotNull();
        assertThat(libreTerminals).hasSize(4);
        assertThat(libreTerminals).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermAnotherPlace", "TermFar - Free");
    }

    /**
     * Test de la méthode findByStatusTerminal du TerminalRepository.
     * Vérifie que les terminaux avec un statut spécifique sont correctement récupérés.
     */
    @Test
    void testFindByStatusTerminal_OCCUPEE() {
        List<Terminal> occupiedTerminals = terminalRepository.findByStatusTerminal(TerminalStatus.OCCUPEE);
        assertThat(occupiedTerminals).isNotNull();
        assertThat(occupiedTerminals).hasSize(1);
        assertThat(occupiedTerminals).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder("Term1 - Occupied");
    }

    /**
     * Test de la méthode findByOccupied du TerminalRepository.
     * Vérifie que les terminaux avec un statut d'occupation spécifique sont correctement récupérés.
     */
    @Test
    void testFindByOccupied_True() {
        List<Terminal> occupiedTerminals = terminalRepository.findByOccupied(true);
        assertThat(occupiedTerminals).isNotNull();
        assertThat(occupiedTerminals).hasSize(1);
        assertThat(occupiedTerminals).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder("Term1 - Occupied");
    }

    /**
     * Test de la méthode findByOccupied du TerminalRepository.
     * Vérifie que les terminaux avec un statut d'occupation spécifique sont correctement récupérés.
     */
    @Test
    void testFindByOccupied_False() {
        List<Terminal> freeTerminals = terminalRepository.findByOccupied(false);
        assertThat(freeTerminals).isNotNull();
        assertThat(freeTerminals).hasSize(4);
        assertThat(freeTerminals).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermFar - Free", "TermAnotherPlace");
    }

    /**
     * Test de la méthode findByPlaceAndStatusTerminal du TerminalRepository.
     * Vérifie que les terminaux associés à un lieu spécifique et avec un statut spécifique sont correctement récupérés.
     */
    @Test
    void testFindByPlaceAndStatusTerminal() {
        List<Terminal> result = terminalRepository.findByPlaceAndStatusTerminal(place1, TerminalStatus.LIBRE);
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermFar - Free");
    }

    /**
     * Test de la méthode findTerminalByPlace_PublicId du TerminalRepository.
     * Vérifie que les terminaux associés à un lieu spécifique (identifié par son publicId) sont correctement récupérés.
     */
    @Test
    void testFindTerminalByPlace_PublicId() {
        List<Terminal> result = terminalRepository.findTerminalByPlace_PublicId(place1.getPublicId());
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);
        assertThat(result).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder(terminal1.getNameTerminal(), terminal2.getNameTerminal(), terminal3.getNameTerminal(), terminalFar.getNameTerminal());

        List<Terminal> result2 = terminalRepository.findTerminalByPlace_PublicId(UUID.randomUUID());
        assertThat(result2).isNotNull();
        assertThat(result2).isEmpty();
    }

    /**
     * Test de la méthode findByPublicId du TerminalRepository.
     * Vérifie que le terminal avec un publicId spécifique est correctement récupéré.
     */
    @Test
    void testFindByPublicId() {
        Optional<Terminal> foundTerminal = terminalRepository.findByPublicId(terminal1.getPublicId());
        assertThat(foundTerminal).isPresent();
        assertThat(foundTerminal.get().getNameTerminal()).isEqualTo("Term1 - Occupied");

        Optional<Terminal> notFoundTerminal = terminalRepository.findByPublicId(UUID.randomUUID());
        assertThat(notFoundTerminal).isNotPresent();
    }

    /**
     * Test de la méthode deleteTerminalByPublicId du TerminalRepository.
     * Vérifie que le terminal avec un publicId spécifique est correctement supprimé.
     */
    @Test
    void testDeleteTerminalByPublicId() {
        UUID publicIdToDelete = terminal2.getPublicId();

        assertThat(terminalRepository.findByPublicId(publicIdToDelete)).isPresent();

        terminalRepository.deleteTerminalByPublicId(publicIdToDelete);
        entityManager.flush();

        assertThat(terminalRepository.findByPublicId(publicIdToDelete)).isNotPresent();
        assertThat(terminalRepository.findAll()).hasSize(4);
    }

    /**
     * Test de la méthode searchTerminals du TerminalRepository sans filtres.
     * Vérifie que tous les terminaux libres sont récupérés.
     */
    @Test
    void testSearchTerminals_noFilters() {
        List<Terminal> results = terminalRepository.searchTerminals(null, null, null, null, null, null);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(4);
        assertThat(results).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermFar - Free", "TermAnotherPlace");
    }

    /**
     * Test de la méthode searchTerminals du TerminalRepository avec filtre d'occupation.
     * Vérifie que seuls les terminaux libres sont récupérés.
     */
    @Test
    void testSearchTerminals_withinRadius_OccupiedFalse() {
        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0;

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, false, null, null);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermAnotherPlace");
    }

    /**
     * Test de la méthode searchTerminals du TerminalRepository avec une période de réservation sans conflit.
     * Vérifie que tous les terminaux libres dans le rayon sont récupérés.
     */
    @Test
    void testSearchTerminals_withNoConflictingBookingPeriod() {
        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusYears(1);
        LocalDateTime endDate = LocalDateTime.now().plusYears(1).plusDays(2);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermAnotherPlace");
    }

    /**
     * Test de la méthode searchTerminals du TerminalRepository avec une période de réservation en conflit (chevauchement total).
     * Vérifie que les terminaux avec des réservations en conflit sont exclus des résultats.
     */
    @Test
    void testSearchTerminals_withBookingStartingWithinSearchPeriod() {
        Booking conflictingBooking = new Booking(null, UUID.randomUUID(), user1, null, terminal2, null,
                "PARTIAL_OVERLAP", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(48.85),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(11),
                LocalDateTime.now().plusDays(13));
        bookingRepository.save(conflictingBooking);
        entityManager.flush();
        entityManager.clear();

        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusDays(10);
        LocalDateTime endDate = LocalDateTime.now().plusDays(12);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term3 - Free", "TermAnotherPlace");
    }

    /**
     * Test de la méthode searchTerminals du TerminalRepository avec une période de réservation en conflit (chevauchement partiel à la fin).
     * Vérifie que les terminaux avec des réservations en conflit sont exclus des résultats.
     */
    @Test
    void testSearchTerminals_withBookingEndingWithinSearchPeriod() {
        Booking conflictingBooking = new Booking(null, UUID.randomUUID(), user1, null, terminal2, null,
                "PARTIAL_OVERLAP_END", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(48.85),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(9),
                LocalDateTime.now().plusDays(11));
        bookingRepository.save(conflictingBooking);
        entityManager.flush();
        entityManager.clear();

        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusDays(10);
        LocalDateTime endDate = LocalDateTime.now().plusDays(12);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term3 - Free", "TermAnotherPlace");
    }

    /**
     * Test de la méthode searchTerminals du TerminalRepository avec une période de réservation en conflit (chevauchement total).
     * Vérifie que les terminaux avec des réservations en conflit sont exclus des résultats.
     */
    @Test
    void testSearchTerminals_withBookingFullyEncompassingSearchPeriod() {
        Booking conflictingBooking = new Booking(null, UUID.randomUUID(), user1, null, terminal2, null,
                "FULL_ENCOMPASS", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(48.85),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(9),
                LocalDateTime.now().plusDays(13));
        bookingRepository.save(conflictingBooking);
        entityManager.flush();
        entityManager.clear();

        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusDays(10);
        LocalDateTime endDate = LocalDateTime.now().plusDays(12);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term3 - Free", "TermAnotherPlace");
    }

    /**
     * Test de la méthode searchTerminals du TerminalRepository avec une période de réservation sans conflit (réservation juste avant la période de recherche).
     * Vérifie que tous les terminaux libres dans le rayon sont récupérés.
     */
    @Test
    void testSearchTerminals_withBookingImmediatelyBeforeSearchPeriod() {
        Booking nonConflictingBooking = new Booking(null, UUID.randomUUID(), user1, null, terminal2, null,
                "BEFORE", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(48.85),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(8),
                LocalDateTime.now().plusDays(9));
        bookingRepository.save(nonConflictingBooking);
        entityManager.flush();
        entityManager.clear();

        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusDays(10);
        LocalDateTime endDate = LocalDateTime.now().plusDays(12);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermAnotherPlace");
    }

    /**
     * Test de la méthode searchTerminals du TerminalRepository avec une période de réservation sans conflit (réservation juste après la période de recherche).
     * Vérifie que tous les terminaux libres dans le rayon sont récupérés.
     */
    @Test
    void testSearchTerminals_withBookingImmediatelyAfterSearchPeriod() {
        Booking nonConflictingBooking = new Booking(null, UUID.randomUUID(), user1, null, terminal2, null,
                "AFTER", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(48.85),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(13),
                LocalDateTime.now().plusDays(14));
        bookingRepository.save(nonConflictingBooking);
        entityManager.flush();
        entityManager.clear();

        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusDays(10);
        LocalDateTime endDate = LocalDateTime.now().plusDays(12);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermAnotherPlace");
    }

    /**
     * Test le cas où aucun terminal ne correspond aux critères de recherche.
     */
    @Test
    void testSearchTerminals_allParameters_noResults() {
        BigDecimal longitude = new BigDecimal("1.00");
        BigDecimal latitude = new BigDecimal("1.00");
        double radius = 1.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }
}
