package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import javax.xml.crypto.Data;
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
        // Nettoyer la BDD avant chaque test et insérer des données de base
        bookingRepository.deleteAll();
        terminalRepository.deleteAll();
        placeRepository.deleteAll();
        userRepository.deleteAll();
        addressRepository.deleteAll();
        mediaRepository.deleteAll();
        entityManager.flush(); // S'assurer que les suppressions sont appliquées
        entityManager.clear(); // Détacher les entités pour éviter des problèmes de persistance


        Media media1 = new Media(null, "PhotoProfil", "http://example.com/pic.jpg", "image/jpeg", "Photo de profil", "100KB", LocalDateTime.now(), null, null, null, null);
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
                UUID.randomUUID(),                "Home Address",
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

        // Créer des terminaux avec des statuts d'occupation cohérents
        // Terminal 1: OCCUPEE. Aura une réservation qui ne chevauche PAS les dates de test
        terminal1 = new Terminal(null, UUID.randomUUID(), "Term1 - Occupied",
                new BigDecimal("48.86666700"), new BigDecimal("2.33333300"), // Proche du centre de recherche
                new BigDecimal("2.40"), new BigDecimal("22.00"), "Instruction Term1",
                true, TerminalStatus.OCCUPEE, // <-- 'occupied' à true, 'status' à OCCUPEE
                true, LocalDateTime.of(2017,2,3,6,30,40), LocalDateTime.of(2017,2,3,6,30,40), user1, null, place1, null, null);

        // Terminal 2: LIBRE. Aucun booking. Bien dans le rayon.
        terminal2 = new Terminal(null, UUID.randomUUID(), "Term2 - Free",
                new BigDecimal("48.85661300"), new BigDecimal("2.35222200"), // Très proche du centre de recherche
                new BigDecimal("3.00"), new BigDecimal("30.00"), "Instruction 2",
                false, TerminalStatus.LIBRE, // <-- 'occupied' à false, 'status' à LIBRE
                false, LocalDateTime.of(2018,3,4,7,40,50), LocalDateTime.of(2018,3,4,7,40,50), user1, null, place1, null, null);

        // Terminal 3: LIBRE. Aucun booking. Egalement dans le rayon.
        terminal3 = new Terminal(null, UUID.randomUUID(), "Term3 - Free",
                new BigDecimal("48.85884400"), new BigDecimal("2.29435100"), // Aussi dans le rayon
                new BigDecimal("4.00"), new BigDecimal("40.00"), "Instruction 3",
                false, TerminalStatus.LIBRE, // <-- 'occupied' à false, 'status' à LIBRE
                false, LocalDateTime.of(2019,4,5,8,50,59), LocalDateTime.of(2019,4,5,8,50,59), user1, null, place1, null, null);


        terminalFar = new Terminal(null, UUID.randomUUID(), "TermFar - Free",
                new BigDecimal("49.00000000"), new BigDecimal("2.00000000"), // Loin de (48.85, 2.35)
                new BigDecimal("5.00"), new BigDecimal("50.00"), "Instruction Far",
                false, TerminalStatus.LIBRE,
                false, LocalDateTime.now(), LocalDateTime.now(), user1, null, place1, null, null);
        terminalRepository.save(terminalFar);

        // Terminal pour une autre place
        terminalAnotherPlace = new Terminal(null, UUID.randomUUID(), "TermAnotherPlace",
                new BigDecimal("48.86000000"), new BigDecimal("2.30000000"),
                new BigDecimal("3.50"), new BigDecimal("35.00"), "Instruction Another Place",
                false, TerminalStatus.LIBRE,
                false, LocalDateTime.now(), LocalDateTime.now(), user1, null, place2, null, null);
        terminalRepository.save(terminalAnotherPlace);

        terminalRepository.saveAll(List.of(terminal1, terminal2, terminal3, terminalFar, terminalAnotherPlace));

        LocalDateTime futureStartingDate = LocalDateTime.now().plusHours(1); // Ex: dans 1 heure
        LocalDateTime futureEndingDate = futureStartingDate.plusHours(2);
        LocalDateTime futurePaymentDate = futureStartingDate.minusMinutes(30);

        // Créer un booking pour terminal1 qui ne chevauche PAS les dates de recherche du test
        // (Booking en 2019, recherche en 2025)
        Booking booking1 = new Booking(null, UUID.randomUUID(), user1, null, terminal1, null,
                "1231923", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(48.85),
                futurePaymentDate,
                futureStartingDate, // Début de réservation en 2019
                futureEndingDate // Fin de réservation en 2019
        );
        bookingRepository.save(booking1);

        entityManager.flush();
        entityManager.clear();
    }

    // Test de la méthode searchTerminals avec différents scénarios

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
        assertThat(results).hasSize(4);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term1 - Occupied", "Term2 - Free", "Term3 - Free", "TermAnotherPlace");
    }

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

    // Test de la méthode findByPlace

    @Test
    void testFindByPlace() {
        List<Terminal> terminalsInPlace1 = terminalRepository.findByPlace(place1);
        assertThat(terminalsInPlace1).isNotNull();
        assertThat(terminalsInPlace1).hasSize(4); // terminal1, terminal2, terminal3, terminalFar
        assertThat(terminalsInPlace1).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder(terminal1.getNameTerminal(), terminal2.getNameTerminal(), terminal3.getNameTerminal(), terminalFar.getNameTerminal());

        List<Terminal> terminalsInPlace2 = terminalRepository.findByPlace(place2);
        assertThat(terminalsInPlace2).isNotNull();
        assertThat(terminalsInPlace2).hasSize(1);
        assertThat(terminalsInPlace2).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder(terminalAnotherPlace.getNameTerminal());
    }

    //

    @Test
    void testFindByStatusTerminal_LIBRE() {
        List<Terminal> libreTerminals = terminalRepository.findByStatusTerminal(TerminalStatus.LIBRE);
        assertThat(libreTerminals).isNotNull();
        // terminal2, terminal3, terminalFar, terminalAnotherPlace sont LIBRE
        assertThat(libreTerminals).hasSize(4);
        assertThat(libreTerminals).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermAnotherPlace", "TermFar - Free");
    }

    @Test
    void testFindByStatusTerminal_OCCUPEE() {
        List<Terminal> occupiedTerminals = terminalRepository.findByStatusTerminal(TerminalStatus.OCCUPEE);
        assertThat(occupiedTerminals).isNotNull();
        // terminal1 est OCCUPEE
        assertThat(occupiedTerminals).hasSize(1);
        assertThat(occupiedTerminals).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder("Term1 - Occupied");
    }

    @Test
    void testFindByOccupied_True() {
        List<Terminal> occupiedTerminals = terminalRepository.findByOccupied(true);
        assertThat(occupiedTerminals).isNotNull();
        // Seul terminal1 a occupied = true
        assertThat(occupiedTerminals).hasSize(1);
        assertThat(occupiedTerminals).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder("Term1 - Occupied");
    }

    @Test
    void testFindByOccupied_False() {
        List<Terminal> freeTerminals = terminalRepository.findByOccupied(false);
        assertThat(freeTerminals).isNotNull();
        // terminal2, terminal3, terminalFar, terminalAnotherPlace ont occupied = false
        assertThat(freeTerminals).hasSize(4);
        assertThat(freeTerminals).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermFar - Free", "TermAnotherPlace");
    }

    @Test
    void testFindByPlaceAndStatusTerminal() {
        List<Terminal> result = terminalRepository.findByPlaceAndStatusTerminal(place1, TerminalStatus.LIBRE);
        assertThat(result).isNotNull();
        // Dans place1, terminal2, terminal3, terminalFar sont LIBRE
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermFar - Free");
    }

    @Test
    void testFindTerminalByPlace_PublicId() {
        List<Terminal> result = terminalRepository.findTerminalByPlace_PublicId(place1.getPublicId());
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4); // terminal1, terminal2, terminal3, terminalFar
        assertThat(result).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder(terminal1.getNameTerminal(), terminal2.getNameTerminal(), terminal3.getNameTerminal(), terminalFar.getNameTerminal());

        List<Terminal> result2 = terminalRepository.findTerminalByPlace_PublicId(UUID.randomUUID()); // ID inexistant
        assertThat(result2).isNotNull();
        assertThat(result2).isEmpty();
    }

    @Test
    void testFindByPublicId() {
        Optional<Terminal> foundTerminal = terminalRepository.findByPublicId(terminal1.getPublicId());
        assertThat(foundTerminal).isPresent();
        assertThat(foundTerminal.get().getNameTerminal()).isEqualTo("Term1 - Occupied");

        Optional<Terminal> notFoundTerminal = terminalRepository.findByPublicId(UUID.randomUUID());
        assertThat(notFoundTerminal).isNotPresent();
    }

    @Test
    void testDeleteTerminalByPublicId() {
        UUID publicIdToDelete = terminal2.getPublicId();

        // S'assurer qu'il existe avant la suppression
        assertThat(terminalRepository.findByPublicId(publicIdToDelete)).isPresent();

        terminalRepository.deleteTerminalByPublicId(publicIdToDelete);
        entityManager.flush(); // Exécute la suppression

        // S'assurer qu'il n'existe plus
        assertThat(terminalRepository.findByPublicId(publicIdToDelete)).isNotPresent();
        assertThat(terminalRepository.findAll()).hasSize(4); // 5 terminaux au départ, 1 supprimé
    }

    @Test
    void testSearchTerminals_noFilters() {
        // Devrait retourner tous les terminaux
        // Si 'occupied' est NULL, la clause 't.occupied = :occupied' est ignorée.
        // Si les dates sont NULL, la clause de réservation est ignorée.
        // Par défaut, searchTerminals devrait retourner tous les terminaux
        // C'est-à-dire : terminal1, terminal2, terminal3, terminalFar, terminalAnotherPlace
        List<Terminal> results = terminalRepository.searchTerminals(null, null, null, null, null, null);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(5); // Tous sauf terminal1 (qui a occupied = true)
        assertThat(results).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder("Term1 - Occupied", "Term2 - Free", "Term3 - Free", "TermFar - Free", "TermAnotherPlace");
    }

    @Test
    void testSearchTerminals_filterByOccupiedTrue() {
        // Ne devrait retourner que terminal1 (qui a occupied = true)
        List<Terminal> results = terminalRepository.searchTerminals(null, null, null, true, null, null);
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term1 - Occupied");
    }

    @Test
    void testSearchTerminals_filterByOccupiedFalse() {
        // Devrait retourner tous les terminaux qui ont occupied = false
        // terminal2, terminal3, terminalFar, terminalAnotherPlace
        List<Terminal> results = terminalRepository.searchTerminals(null, null, null, false, null, null);
        assertThat(results).isNotNull();
        assertThat(results).hasSize(4);
        assertThat(results).extracting(Terminal::getNameTerminal)
                .containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermFar - Free", "TermAnotherPlace");
    }

    @Test
    void testSearchTerminals_withinRadius_OccupiedFalse() {
        // Ce test est similaire à testSearchTerminals_outsideRadius, mais je le rends plus générique.
        // Coordonnées au centre de Paris
        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0; // Rayon de 5 km

        // Recherche les terminaux non occupés dans un rayon de 5km autour de (48.85, 2.35)
        // Ceux qui devraient être trouvés: terminal2, terminal3. terminal1 est occupé. terminalFar est trop loin. terminalAnotherPlace est à place2 mais proche géographiquement.
        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, false, null, null);

        assertThat(results).isNotNull();
        assertThat(results).hasSize(3); // terminal2, terminal3, terminalAnotherPlace
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermAnotherPlace");
    }

    @Test
    void testSearchTerminals_withNoConflictingBookingPeriod() {
        // Période de recherche où aucun terminal n'a de réservation.
        // Tous les terminaux non occupés devraient être retournés.
        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusYears(1); // Très loin dans le futur
        LocalDateTime endDate = LocalDateTime.now().plusYears(1).plusDays(2);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        // terminal2, terminal3, terminalAnotherPlace sont libres et dans le rayon.
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermAnotherPlace");
    }

    @Test
    void testSearchTerminals_withBookingStartingWithinSearchPeriod() {
        // Le booking commence D+11, finit D+13
        // La recherche est pour [D+10, D+12]
        // Le booking chevauche, donc terminal2 doit être exclu.
        Booking conflictingBooking = new Booking(null, UUID.randomUUID(), user1, null, terminal2, null,
                "PARTIAL_OVERLAP", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(48.85),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(11), // booking starts D+11
                LocalDateTime.now().plusDays(13)); // booking ends D+13
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
        assertThat(results).hasSize(2); // terminal3, terminalAnotherPlace
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term3 - Free", "TermAnotherPlace");
    }


    @Test
    void testSearchTerminals_withBookingEndingWithinSearchPeriod() {
        // Le booking commence D+9, finit D+11
        // La recherche est pour [D+10, D+12]
        // Le booking chevauche, donc terminal2 doit être exclu.
        Booking conflictingBooking = new Booking(null, UUID.randomUUID(), user1, null, terminal2, null,
                "PARTIAL_OVERLAP_END", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(48.85),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(9), // booking starts D+9
                LocalDateTime.now().plusDays(11)); // booking ends D+11
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
        assertThat(results).hasSize(2); // terminal3, terminalAnotherPlace
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term3 - Free", "TermAnotherPlace");
    }

    @Test
    void testSearchTerminals_withBookingFullyEncompassingSearchPeriod() {
        // Le booking commence D+9, finit D+13
        // La recherche est pour [D+10, D+12]
        // Le booking chevauche entièrement la période de recherche, donc terminal2 doit être exclu.
        Booking conflictingBooking = new Booking(null, UUID.randomUUID(), user1, null, terminal2, null,
                "FULL_ENCOMPASS", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(48.85),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(9), // booking starts D+9
                LocalDateTime.now().plusDays(13)); // booking ends D+13
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
        assertThat(results).hasSize(2); // terminal3, terminalAnotherPlace
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term3 - Free", "TermAnotherPlace");
    }

    @Test
    void testSearchTerminals_withBookingImmediatelyBeforeSearchPeriod() {
        // Le booking finit juste avant le début de la recherche. Ne doit pas être un conflit.
        Booking nonConflictingBooking = new Booking(null, UUID.randomUUID(), user1, null, terminal2, null,
                "BEFORE", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(48.85),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(8),
                LocalDateTime.now().plusDays(9)); // booking ends D+9
        bookingRepository.save(nonConflictingBooking);
        entityManager.flush();
        entityManager.clear();

        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusDays(10); // search starts D+10
        LocalDateTime endDate = LocalDateTime.now().plusDays(12);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        // terminal2 ne devrait PAS être exclu car le booking ne chevauche pas.
        // On devrait retrouver terminal2, terminal3, terminalAnotherPlace
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermAnotherPlace");
    }

    @Test
    void testSearchTerminals_withBookingImmediatelyAfterSearchPeriod() {
        // Le booking commence juste après la fin de la recherche. Ne doit pas être un conflit.
        Booking nonConflictingBooking = new Booking(null, UUID.randomUUID(), user1, null, terminal2, null,
                "AFTER", BookingStatus.EN_ATTENTE, BigDecimal.valueOf(48.85),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(13), // booking starts D+13
                LocalDateTime.now().plusDays(14));
        bookingRepository.save(nonConflictingBooking);
        entityManager.flush();
        entityManager.clear();

        BigDecimal longitude = new BigDecimal("2.35");
        BigDecimal latitude = new BigDecimal("48.85");
        double radius = 5.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusDays(10);
        LocalDateTime endDate = LocalDateTime.now().plusDays(12); // search ends D+12

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        // terminal2 ne devrait PAS être exclu car le booking ne chevauche pas.
        // On devrait retrouver terminal2, terminal3, terminalAnotherPlace
        assertThat(results).hasSize(3);
        assertThat(results).extracting(Terminal::getNameTerminal).containsExactlyInAnyOrder("Term2 - Free", "Term3 - Free", "TermAnotherPlace");
    }

    @Test
    void testSearchTerminals_allParameters_noResults() {
        // Cas où aucun terminal ne correspond aux critères
        BigDecimal longitude = new BigDecimal("1.00"); // Très loin
        BigDecimal latitude = new BigDecimal("1.00");   // Très loin
        double radius = 1.0;
        Boolean occupied = false;
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);

        List<Terminal> results = terminalRepository.searchTerminals(longitude, latitude, radius, occupied, startDate, endDate);

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }
}
