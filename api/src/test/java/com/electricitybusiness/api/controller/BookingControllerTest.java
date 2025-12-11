package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.config.JwtAuthFilter;
import com.electricitybusiness.api.config.TestSecurityConfig;
import com.electricitybusiness.api.dto.booking.BookingCreateDTO;
import com.electricitybusiness.api.dto.booking.BookingDTO;
import com.electricitybusiness.api.dto.booking.BookingStatusDTO;
import com.electricitybusiness.api.exception.ResourceNotFoundException;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.*;
import com.electricitybusiness.api.repository.*;
import com.electricitybusiness.api.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.electricitybusiness.api.repository.UserRepository;
import com.electricitybusiness.api.repository.AddressRepository;
import com.electricitybusiness.api.repository.PlaceRepository;
import com.electricitybusiness.api.repository.OptionRepository;
import com.electricitybusiness.api.repository.CarRepository;
import com.electricitybusiness.api.repository.TerminalRepository;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = BookingController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        ),
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                ErrorMvcAutoConfiguration.class,
                DataSourceAutoConfiguration.class,
                JpaRepositoriesAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private EntityMapper mapper;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TerminalService terminalService;
    @MockitoBean
    private CarService carService;
    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private AddressRepository addressRepository;
    @MockitoBean
    private PlaceRepository placeRepository;
    @MockitoBean
    private OptionRepository optionRepository;
    @MockitoBean
    private CarRepository carRepository;
    @MockitoBean
    private TerminalRepository terminalRepository;

    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    private Booking testBooking;
    private BookingDTO testBookingDTO;
    private BookingCreateDTO testBookingCreateDTO;
    private User testUser;
    private UUID bookingPublicId;
    private Long terminalId;
    private Long carId;
    private Long optionId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private UUID carPublicId;
    private UUID optionPublicId;
    private UUID terminalPublicId;
    private BookingDTO updatedBookingDTO;
    private Booking updatedBookingEntity;
    private final Long existingBookingId = 1L;
    private final UUID existingBookingPublicId = UUID.fromString("d00765c0-7e8a-4c8f-ab46-4b2cf077dac5");

    @Mock
    private Terminal testTerminal;
    @Mock
    private Car testCar;
    @Mock
    private Option testOption;
    @Autowired
    private EntityMapper entityMapper;
    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        MockitoAnnotations.openMocks(this);

        bookingPublicId = UUID.randomUUID();
        bookingPublicId = UUID.randomUUID();
        terminalId = ThreadLocalRandom.current().nextLong(1, 10000);
        carId = ThreadLocalRandom.current().nextLong(1, 10000);
        optionId = ThreadLocalRandom.current().nextLong(1, 10000);
        startDate = LocalDateTime.now().plusDays(1);
        endDate = LocalDateTime.now().plusDays(3);
        carPublicId = UUID.randomUUID();
        optionPublicId = UUID.randomUUID();
        terminalPublicId = UUID.randomUUID();

        testUser = new User();
        testUser.setIdUser(1L);
        testUser.setEmailUser("testuser@example.com");
        testUser.setSurnameUser("testuser");

        testTerminal = new Terminal();
        testTerminal.setIdTerminal(terminalId);
        testTerminal.setPublicId(terminalPublicId);
        testTerminal.setNameTerminal("Borne Test 4051");

        testCar = new Car();
        testCar.setPublicId(carPublicId);
        testCar.setModel("Model X");
        testCar.setIdCar(carId);

        testOption = new Option();
        testOption.setPublicId(optionPublicId);
        testOption.setNameOption("GPS Option");
        testOption.setIdOption(optionId);

        testBooking = new Booking();
        testBooking.setIdBooking(1L);
        testBooking.setPublicId(UUID.randomUUID());
        testBooking.setUser(testUser);

        testBookingDTO = new BookingDTO();
        testBookingDTO.setPublicId(testBooking.getPublicId());
        testBookingDTO.setIdUser(testUser.getIdUser());
        testBookingDTO.setIdTerminal(terminalId);

        User ownerUser = new User();
        ownerUser.setIdUser(2L);
        ownerUser.setEmailUser("owner@example.com");
        ownerUser.setSurnameUser("owner");

        Address testAddress = new Address();
        testAddress.setIdAddress(1L);
        testAddress.setCity("Test City");

        Place testPlace = new Place();
        testPlace.setIdPlace(1L);
        testPlace.setUser(ownerUser);
        testPlace.setAddress(testAddress);

        Terminal testTerminalInstance = new Terminal();
        testTerminalInstance.setIdTerminal(1L);
        testTerminalInstance.setPlace(testPlace);

        testBooking.setTerminal(testTerminalInstance);

        testBookingDTO = new BookingDTO();
        testBookingDTO.setPublicId(testBooking.getPublicId());
        testBookingDTO.setIdUser(testUser.getIdUser());
        testBookingDTO.setIdTerminal(testTerminalInstance.getIdTerminal());
        testBookingDTO.setIdCar(carId);
        testBookingDTO.setIdOption(optionId);
        testBookingDTO.setStartingDate(startDate);
        testBookingDTO.setEndingDate(endDate);
        testBookingDTO.setStatusBooking(BookingStatus.ACCEPTEE);

        testBookingCreateDTO = new BookingCreateDTO();
        testBookingCreateDTO.setPublicIdTerminal(testTerminal.getPublicId());
        testBookingCreateDTO.setPublicIdCar(testCar.getPublicId());
        testBookingCreateDTO.setPublicIdOption(testOption.getPublicId());
        testBookingCreateDTO.setStartingDate(LocalDateTime.now().plusDays(10));
        testBookingCreateDTO.setEndingDate(LocalDateTime.now().plusDays(12));
        testBookingCreateDTO.setStatusBooking(BookingStatus.EN_ATTENTE);
        testBookingCreateDTO.setNumberBooking(null);
        testBookingCreateDTO.setTotalAmount(null);
        testBookingCreateDTO.setPaymentDate(null);
        testBookingCreateDTO.setIdUser(null);

        updatedBookingDTO = new BookingDTO();
        updatedBookingDTO.setPublicId(testBooking.getPublicId());
        updatedBookingDTO.setIdUser(testUser.getIdUser());
        updatedBookingDTO.setIdTerminal(testTerminal.getIdTerminal());
        updatedBookingDTO.setStatusBooking(BookingStatus.ACCEPTEE);
        updatedBookingDTO.setStartingDate(LocalDateTime.now().plusDays(10));
        updatedBookingDTO.setEndingDate(LocalDateTime.now().plusDays(12));

/*
        updatedBookingDTO.setStartingDate(LocalDateTime.of(2025, 12, 10, 9, 0, 0));
        updatedBookingDTO.setEndingDate(LocalDateTime.of(2025, 12, 10, 10, 0, 0));
*/

        updatedBookingEntity = new Booking();
        updatedBookingEntity.setIdBooking(existingBookingId);
        updatedBookingEntity.setPublicId(existingBookingPublicId);
        updatedBookingEntity.setStatusBooking(BookingStatus.ACCEPTEE);
        updatedBookingEntity.setStartingDate(LocalDateTime.now().plusDays(10));
        updatedBookingEntity.setEndingDate(LocalDateTime.now().plusDays(12));
/*        updatedBookingEntity.setStartingDate(LocalDateTime.of(2025, 12, 10, 9, 0, 0));
        updatedBookingEntity.setEndingDate(LocalDateTime.of(2025, 12, 10, 10, 0, 0));*/
        updatedBookingEntity.setTerminal(testTerminal);
        updatedBookingEntity.setUser(testUser);
    }

    /**
     * Test pour la récupération de toutes les réservations (ADMIN)
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getAllBookings_WithAdminRole_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/bookings/all"))
                .andExpect(status().isOk());
    }

    /**
     * Test pour la récupération de toutes les réservations (ADMIN)
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ADMIN"})
    void getAllBookings_ShouldReturnListOfBookings() throws Exception {
        given(bookingService.getAllBookings()).willReturn(Collections.singletonList(testBooking));
        given(mapper.toBookingDTO(testBooking)).willReturn(testBookingDTO);

        mockMvc.perform(get("/api/bookings/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publicId").value(testBookingDTO.getPublicId().toString()));
    }

    /**
     * Test pour la récupération d'une réservation par ID (ADMIN)
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ADMIN"})
    void getBookingById_ShouldReturnBooking() throws Exception {
        given(bookingService.getBookingById(anyLong())).willReturn(Optional.of(testBooking));
        given(mapper.toBookingDTO(testBooking)).willReturn(testBookingDTO);

        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(testBookingDTO.getPublicId().toString()));
    }

    /**
     * Test pour la suppression d'une réservation (utilisateur authentifié)
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com")
    void deleteBooking_ShouldReturnNoContent() throws Exception {
        given(bookingService.existsById(anyLong())).willReturn(true);
        Mockito.doNothing().when(bookingService).deleteBookingById(anyLong());

        mockMvc.perform(delete("/api/bookings/1"))
                .andExpect(status().isNoContent());
    }

    /**
     * Test pour la suppression d'une réservation (utilisateur authentifié)
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getAllBookingsByUserClient_ShouldReturnListOfBookings() throws Exception {
        // Configuration de l'utilisateur
        User testUser = new User();
        testUser.setIdUser(1L);
        testUser.setEmailUser("testuser@example.com");

        // Configuration des réservations
        List<Booking> bookings = new ArrayList<>();
        Booking booking1 = new Booking();
        booking1.setIdBooking(1L);
        booking1.setPublicId(UUID.randomUUID());
        booking1.setUser(testUser);
        booking1.setStartingDate(LocalDateTime.now().plusDays(1));
        booking1.setEndingDate(LocalDateTime.now().plusDays(2));
        booking1.setStatusBooking(BookingStatus.ACCEPTEE);
        bookings.add(booking1);

        // Mock du service pour récupérer l'utilisateur par email
        given(userService.getIdByEmailUser(testUser.getEmailUser())).willReturn(testUser.getIdUser());
        given(userService.getUserById(testUser.getIdUser())).willReturn(testUser);

        // Mock du service pour récupérer les réservations par utilisateur
        given(bookingService.getBookingsByUserClient(
                any(User.class),
                nullable(LocalDateTime.class),
                nullable(LocalDateTime.class),
                anyString(),
                any(BookingStatus.class)))
                .willReturn(bookings);

        // Mock du mapper pour convertir Booking en BookingDTO
        BookingDTO bookingDTO1 = new BookingDTO();
        bookingDTO1.setPublicId(booking1.getPublicId());
        bookingDTO1.setIdUser(testUser.getIdUser());
        bookingDTO1.setStartingDate(booking1.getStartingDate());
        bookingDTO1.setEndingDate(booking1.getEndingDate());
        bookingDTO1.setStatusBooking(booking1.getStatusBooking());

        given(mapper.toBookingDTO(any(Booking.class))).willReturn(bookingDTO1);

        String expectedStartingDateString = bookingDTO1.getStartingDate().toString().replaceAll("0+$", "");
        String expectedEndingDateString = bookingDTO1.getEndingDate().toString().replaceAll("0+$", "");
        if (expectedStartingDateString.endsWith(".")) {
            expectedStartingDateString = expectedStartingDateString.substring(0, expectedStartingDateString.length() - 1);
        }
        if (expectedEndingDateString.endsWith(".")) {
            expectedEndingDateString = expectedEndingDateString.substring(0, expectedEndingDateString.length() - 1);
        }

        // Exécution du test avec différents paramètres
        mockMvc.perform(get("/api/bookings/user/client")
                        .param("startingDate", LocalDateTime.now().minusDays(1).toString())
                        .param("endingDate", LocalDateTime.now().plusDays(1).toString())
                        .param("orderBooking", "ASC")
                        .param("statusBooking", BookingStatus.ACCEPTEE.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$[0].publicId").value(bookingDTO1.getPublicId().toString()))
                .andExpect(jsonPath("$[0].idUser").value(bookingDTO1.getIdUser()))
                .andExpect(jsonPath("$[0].startingDate").value(expectedStartingDateString))
                .andExpect(jsonPath("$[0].endingDate").value(expectedEndingDateString))
                .andExpect(jsonPath("$[0].statusBooking").value(bookingDTO1.getStatusBooking().toString()));

        verify(userService, atLeastOnce()).getIdByEmailUser(testUser.getEmailUser());
        verify(userService, atLeastOnce()).getUserById(testUser.getIdUser());
        verify(bookingService, times(1)).getBookingsByUserClient(
                any(User.class),
                nullable(LocalDateTime.class),
                nullable(LocalDateTime.class),
                anyString(),
                any(BookingStatus.class)
        );
        verify(mapper, times(bookings.size())).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la récupération de toutes les réservations d'un utilisateur client (aucune réservation existante)
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getAllBookingsByUserClient_ShouldReturnEmptyList_WhenNoBookingsExist() throws Exception {
        // Configuration de l'utilisateur
        User testUser = new User();
        testUser.setIdUser(1L);
        testUser.setEmailUser("testuser@example.com");

        // Mock du service pour récupérer l'utilisateur par email
        given(userService.getIdByEmailUser(testUser.getEmailUser())).willReturn(testUser.getIdUser());
        given(userService.getUserById(testUser.getIdUser())).willReturn(testUser);

        // Mock du service pour retourner une liste vide de réservations
        given(bookingService.getBookingsByUserClient(
                any(User.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyString(),
                any(BookingStatus.class)))
                .willReturn(Collections.emptyList());

        // Exécution du test
        mockMvc.perform(get("/api/bookings/user/client")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    /**
     * Test pour la récupération de toutes les réservations d'un utilisateur client avec un statut de réservation invalide
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getAllBookingsByUserClient_WhenInvalidStatusBooking_ShouldReturnBadRequest() throws Exception {
        User testUser = new User();
        testUser.setIdUser(1L);
        testUser.setEmailUser("testuser@example.com");

        when(userService.getIdByEmailUser(testUser.getEmailUser())).thenReturn(testUser.getIdUser());
        when(userService.getUserById(testUser.getIdUser())).thenReturn(testUser);

        mockMvc.perform(get("/api/bookings/user/client")
                        .param("statusBooking", "INVALID_STATUS") // Statut invalide
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).getBookingsByUserClient(
                any(User.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyString(),
                any(BookingStatus.class));
    }

    /**
     * Test pour la récupération de toutes les réservations d'un utilisateur propriétaire
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getAllBookingsByUserOwner_ShouldReturnListOfBookings() throws Exception {
        // Mock des services
        given(userService.getIdByEmailUser("testuser@example.com")).willReturn(testUser.getIdUser());
        given(userService.getUserById(testUser.getIdUser())).willReturn(testUser);
        given(bookingService.getBookingsByUserOwner(testUser)).willReturn(List.of(testBooking));

        // Stubbing du mapper
        doReturn(testBookingDTO).when(mapper).toBookingDTO(any(Booking.class));

        String expectedStartTimeString = testBookingDTO.getStartingDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        expectedStartTimeString = expectedStartTimeString.replaceAll("(\\.\\d*[1-9])0+$", "$1");

        if (expectedStartTimeString.endsWith(".")) {
            expectedStartTimeString = expectedStartTimeString.substring(0, expectedStartTimeString.length() - 1);
        }

        // Test avec MockMvc
        mockMvc.perform(get("/api/bookings/user/owner")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publicId").value(testBookingDTO.getPublicId().toString()))
                .andExpect(jsonPath("$[0].startingDate").value(expectedStartTimeString))
                .andExpect(jsonPath("$[0].idUser").value(testUser.getIdUser()));

        // Vérifications des interactions
        verify(userService, times(1)).getIdByEmailUser("testuser@example.com");
        verify(userService, times(1)).getUserById(testUser.getIdUser());
        verify(bookingService, times(1)).getBookingsByUserOwner(testUser);
        verify(mapper, times(1)).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la récupération de toutes les réservations d'un utilisateur propriétaire (aucune réservation existante)
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getAllBookingsByUserOwner_ShouldReturnEmptyList_WhenNoBookingsExist() throws Exception {
        // Mock des services
        given(userService.getIdByEmailUser("testuser@example.com")).willReturn(testUser.getIdUser());
        given(userService.getUserById(testUser.getIdUser())).willReturn(testUser);
        given(bookingService.getBookingsByUserOwner(testUser)).willReturn(List.of());

        // Test avec MockMvc
        mockMvc.perform(get("/api/bookings/user/owner")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    /**
     * Test pour la mise à jour d'une réservation par son identifiant public (utilisateur authentifié)
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com")
    void updateBookingByPublicId_ShouldReturnUpdatedBooking_WhenAuthenticated() throws Exception {
        // Given
        Long idUser = testUser.getIdUser();
        Booking updatedBooking = new Booking();
        updatedBooking.setPublicId(bookingPublicId);
        updatedBooking.setNumberBooking("UPDATED-BOOK-001");
        updatedBooking.setStatusBooking(BookingStatus.ACCEPTEE);
        updatedBooking.setStartingDate(testBookingCreateDTO.getStartingDate());
        updatedBooking.setEndingDate(testBookingCreateDTO.getEndingDate());
        updatedBooking.setUser(testUser);
        updatedBooking.setTerminal(testTerminal);
        updatedBooking.setCar(testCar);
        updatedBooking.setOption(testOption);


        // Mock des comportements
        given(bookingService.existsByPublicId(eq(bookingPublicId))).willReturn(true);
        given(userService.getIdByEmailUser(eq("testuser@example.com"))).willReturn(idUser);
        given(mapper.toEntityCreate(
                eq(testBookingCreateDTO), eq(idUser),
                eq(testBookingCreateDTO.getPublicIdTerminal()),
                eq(testBookingCreateDTO.getPublicIdOption()),
                eq(testBookingCreateDTO.getPublicIdCar())
        )).willReturn(updatedBooking);
        given(bookingService.updateBooking(eq(bookingPublicId), any(Booking.class))).willReturn(updatedBooking);
        given(mapper.toBookingDTO(eq(updatedBooking))).willReturn(testBookingDTO);

        // When & Then
        mockMvc.perform(put("/api/bookings/publicId/{publicId}", bookingPublicId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookingCreateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(testBookingDTO.getPublicId().toString()))
                .andExpect(jsonPath("$.numberBooking").value(testBookingDTO.getNumberBooking()))
                .andExpect(jsonPath("$.statusBooking").value(testBookingDTO.getStatusBooking().toString()))
                .andExpect(jsonPath("$.idUser").value(testBookingDTO.getIdUser()))
                .andExpect(jsonPath("$.idTerminal").value(testBookingDTO.getIdTerminal()));

        // Vérifications des interactions
        verify(bookingService).existsByPublicId(eq(bookingPublicId));
        verify(userService).getIdByEmailUser(eq("testuser@example.com"));
        verify(mapper).toEntityCreate(
                eq(testBookingCreateDTO), eq(idUser),
                eq(testBookingCreateDTO.getPublicIdTerminal()),
                eq(testBookingCreateDTO.getPublicIdOption()),
                eq(testBookingCreateDTO.getPublicIdCar())
        );
        verify(bookingService).updateBooking(eq(bookingPublicId), any(Booking.class));
        verify(mapper).toBookingDTO(eq(updatedBooking));
    }

    /**
     * Test pour la mise à jour d'une réservation par son identifiant public avec un DTO invalide
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com")
    void updateBookingByPublicId_ShouldReturnBadRequestForInvalidBookingCreateDTO() throws Exception {
        // Création d'un DTO invalide
        BookingCreateDTO invalidBookingCreateDTO = new BookingCreateDTO();
        invalidBookingCreateDTO.setStartingDate(null);
        invalidBookingCreateDTO.setEndingDate(LocalDateTime.now().plusHours(2));
        invalidBookingCreateDTO.setStatusBooking(BookingStatus.EN_ATTENTE);
        invalidBookingCreateDTO.setPublicIdTerminal(UUID.randomUUID());
        invalidBookingCreateDTO.setPublicIdCar(UUID.randomUUID());
        invalidBookingCreateDTO.setPublicIdOption(UUID.randomUUID());

        // When & Then
        mockMvc.perform(put("/api/bookings/publicId/{publicId}", bookingPublicId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookingCreateDTO)))
                .andExpect(status().isBadRequest());

        // Vérification qu'aucune logique métier n'a été appelée après la validation
        verify(bookingService, never()).existsByPublicId(any(UUID.class));
        verify(userService, never()).getIdByEmailUser(any(String.class));
        verify(mapper, never()).toEntityCreate(any(BookingCreateDTO.class), anyLong(), any(UUID.class), any(UUID.class), any(UUID.class));
        verify(bookingService, never()).updateBooking(any(UUID.class), any(Booking.class));
        verify(mapper, never()).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la mise à jour d'une réservation par son identifiant public lorsque l'utilisateur n'existe pas
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "nonexistent@example.com")
    void updateBookingByPublicId_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        // Given
        String nonexistentEmail = "nonexistent@example.com";
        given(bookingService.existsByPublicId(eq(bookingPublicId))).willReturn(true);
        given(userService.getIdByEmailUser(eq(nonexistentEmail)))
                .willThrow(new ResourceNotFoundException("User not found with email: " + nonexistentEmail));

        // When & Then
        mockMvc.perform(put("/api/bookings/publicId/{publicId}", bookingPublicId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookingCreateDTO)))
                .andExpect(status().isNotFound());

        // Vérifications
        verify(bookingService).existsByPublicId(eq(bookingPublicId));
        verify(userService).getIdByEmailUser(eq(nonexistentEmail));
        verify(mapper, never()).toEntityCreate(any(BookingCreateDTO.class), anyLong(), any(UUID.class), any(UUID.class), any(UUID.class));
        verify(bookingService, never()).updateBooking(any(UUID.class), any(Booking.class));
        verify(mapper, never()).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la mise à jour d'une réservation par son identifiant public lorsque la réservation n'existe pas
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com")
    void updateBooking_ShouldReturnNotFound_WhenBookingDoesNotExist() throws Exception {
        // Given
        given(bookingService.existsByPublicId(bookingPublicId)).willReturn(false);
        String requestBody = objectMapper.writeValueAsString(testBookingCreateDTO);
        System.out.println("JSON Request Body for updateBooking_ShouldReturnNotFound (simplifié): " + requestBody);
        System.out.println("Content Length for updateBooking_ShouldReturnNotFound (simplifié): " + requestBody.length());

        // When & Then
        mockMvc.perform(put("/api/bookings/publicId/{publicId}", bookingPublicId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());

        verify(bookingService).existsByPublicId(bookingPublicId);
        verify(userService, never()).getIdByEmailUser(any(String.class));
        verify(bookingService, never()).updateBooking(any(UUID.class), any(Booking.class));
    }


    /**
     * Test pour la mise à jour du statut d'une réservation par son identifiant public
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com")
    void updateBookingStatus_ShouldReturnUpdatedBooking() throws Exception {
        // Given
        BookingStatusDTO statusDTO = new BookingStatusDTO();
        statusDTO.setStatusBooking(BookingStatus.REFUSEE);

        Booking updatedBooking = new Booking();
        updatedBooking.setIdBooking(testBooking.getIdBooking());
        updatedBooking.setPublicId(bookingPublicId);
        updatedBooking.setIdBooking(testUser.getIdUser());
        updatedBooking.setTerminal(testTerminal);
        updatedBooking.setCar(testCar);
        updatedBooking.setOption(testOption);
        updatedBooking.setStartingDate(startDate);
        updatedBooking.setEndingDate(endDate);
        BookingStatus cancelledStatus = BookingStatus.REFUSEE;
        updatedBooking.setStatusBooking(cancelledStatus);

        BookingDTO updatedBookingDTO = new BookingDTO();
        updatedBookingDTO.setPublicId(bookingPublicId);
        updatedBookingDTO.setIdUser(testUser.getIdUser());
        updatedBookingDTO.setIdTerminal(terminalId);
        updatedBookingDTO.setIdCar(carId);
        updatedBookingDTO.setIdOption(optionId);
        updatedBookingDTO.setStartingDate(startDate);
        updatedBookingDTO.setEndingDate(endDate);
        updatedBookingDTO.setStatusBooking(cancelledStatus);

        given(bookingService.existsByPublicId(bookingPublicId)).willReturn(true);
        given(bookingService.updateBookingStatus(eq(bookingPublicId), any(BookingStatusDTO.class))).willReturn(updatedBooking);
        given(mapper.toBookingDTO(any(Booking.class))).willReturn(updatedBookingDTO);

        // When & Then
        mockMvc.perform(put("/api/bookings/publicId/{publicId}/status", bookingPublicId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(updatedBookingDTO.getPublicId().toString()))
                .andExpect(jsonPath("$.statusBooking").value(updatedBookingDTO.getStatusBooking().toString()));

        verify(bookingService).existsByPublicId(bookingPublicId);
        verify(bookingService).updateBookingStatus(eq(bookingPublicId), any(BookingStatusDTO.class));
        verify(mapper).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la mise à jour du statut d'une réservation par son identifiant public lorsque la réservation n'existe pas
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com")
    void updateBookingStatus_ShouldReturnNotFound_WhenBookingDoesNotExist() throws Exception {
        // Given
        given(bookingService.existsByPublicId(bookingPublicId)).willReturn(false);
        BookingStatusDTO statusDTO = new BookingStatusDTO();
        statusDTO.setStatusBooking(BookingStatus.REFUSEE);

        // When & Then
        mockMvc.perform(put("/api/bookings/publicId/{publicId}/status", bookingPublicId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDTO)))
                .andExpect(status().isNotFound());

        verify(bookingService).existsByPublicId(bookingPublicId);
        verify(bookingService, never()).updateBookingStatus(any(UUID.class), any(BookingStatusDTO.class));
    }

    /**
     * Test pour la génération du PDF d'une réservation
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com")
    void generateBookingPdf_ShouldReturnPdf() throws Exception {
        // Given
        byte[] pdfBytes = "Ceci est un contenu PDF de test".getBytes();
        given(bookingService.generateBookingPdf(bookingPublicId)).willReturn(pdfBytes);

        // When & Then
        mockMvc.perform(get("/api/bookings/publicId/{id}/pdf", bookingPublicId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=facture-" + bookingPublicId + ".pdf"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
                .andExpect(content().bytes(pdfBytes));

        verify(bookingService).generateBookingPdf(bookingPublicId);
    }

    /**
     * Test pour la génération du PDF d'une réservation lorsque le service lance une exception
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com")
    void generateBookingPdf_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Given
        given(bookingService.generateBookingPdf(bookingPublicId)).willThrow(new RuntimeException("PDF generation failed"));

        // When & Then
        mockMvc.perform(get("/api/bookings/publicId/{id}/pdf", bookingPublicId))
                .andExpect(status().isInternalServerError());

        verify(bookingService).generateBookingPdf(bookingPublicId);
    }

    /**
     * Test pour la génération du fichier Excel des réservations d'un utilisateur
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com")
    void generateBookingExcel_ShouldReturnExcelFile() throws Exception {
        // Given
        byte[] excelBytes = "Contenu Excel de test".getBytes();
        given(userService.getIdByEmailUser(testUser.getEmailUser())).willReturn(testUser.getIdUser());
        given(userService.getUserById(testUser.getIdUser())).willReturn(testUser);
        given(bookingService.generateBookingExcel(any(User.class))).willReturn(excelBytes);

        // When & Then
        mockMvc.perform(get("/api/bookings/excel"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bookings.xlsx"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(content().bytes(excelBytes));

        verify(userService).getIdByEmailUser(testUser.getEmailUser());
        verify(userService).getUserById(testUser.getIdUser());
        verify(bookingService).generateBookingExcel(any(User.class));
    }

    /**
     * Test pour la récupération de tous les statuts de réservation
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com")
    void getAllBookingStatuses_ShouldReturnListOfStatuses() throws Exception {
        // Given
        List<BookingStatus> statuses = List.of(BookingStatus.EN_ATTENTE, BookingStatus.ACCEPTEE);

        given(bookingService.getAllBookingStatus()).willReturn(statuses);

        // When & Then
        mockMvc.perform(get("/api/bookings/booking-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("EN_ATTENTE"))
                .andExpect(jsonPath("$[1]").value("ACCEPTEE"));

        verify(bookingService).getAllBookingStatus();
    }

    /**
     * Test pour la création d'une réservation
     * @throws Exception
     */
    @Test
    @WithMockUser
    void createBooking_ShouldReturnCreatedBookingAnd201() throws Exception {
        // Préparation
        when(mapper.toEntity(any(BookingDTO.class))).thenReturn(testBooking);
        when(bookingService.saveBooking(any(Booking.class))).thenReturn(testBooking);
        when(mapper.toBookingDTO(testBooking)).thenReturn(testBookingDTO);

        // Exécution et Vérification
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookingDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").value(testBookingDTO.getPublicId().toString()))
                .andExpect(jsonPath("$.numberBooking").value(testBookingDTO.getNumberBooking()))
                .andExpect(jsonPath("$.statusBooking").value(testBookingDTO.getStatusBooking().name()));

        verify(mapper, times(1)).toEntity(any(BookingDTO.class));
        verify(bookingService, times(1)).saveBooking(testBooking);
        verify(mapper, times(1)).toBookingDTO(testBooking);
    }

    /**
     * Test pour la création d'une réservation avec un DTO invalide
     * @throws Exception
     */
    @Test
    @WithMockUser
    void createBooking_ShouldReturn400_WhenInvalidDto() throws Exception {
        // Préparation: Créer un DTO avec des données invalides
        BookingDTO invalidDto = new BookingDTO();
        invalidDto.setIdTerminal(ThreadLocalRandom.current().nextLong(1, 10000));

        // Exécution et Vérification
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(bookingService, never()).saveBooking(any(Booking.class));
    }

    /**
     * Test pour la création d'une réservation lorsque l'entité liée n'est pas trouvée
     * @throws Exception
     */
    @Test
    @WithMockUser
    void createBooking_ShouldReturn404_WhenRelatedEntityNotFound() throws Exception {
        // Préparation
        when(mapper.toEntity(any(BookingDTO.class))).thenReturn(testBooking);
        when(bookingService.saveBooking(any(Booking.class)))
                .thenThrow(new ResourceNotFoundException("Terminal not found"));

        // Exécution et Vérification
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookingDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Terminal not found"));

        // Vérification
        verify(bookingService).saveBooking(any(Booking.class));
        verify(mapper, never()).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la suppression d'une réservation par son identifiant public
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", roles = "{'USER'}")
    void deleteBooking_ByPublicId_ShouldReturnNoContent() throws Exception {
        given(bookingService.existsByPublicId(any(UUID.class))).willReturn(true);
        Mockito.doNothing().when(bookingService).deleteBookingByPublicId(any(UUID.class));

        mockMvc.perform(delete("/api/bookings/publicId/{publicId}", bookingPublicId))
                .andExpect(status().isNoContent());
    }

    /**
     * Test pour la suppression d'une réservation par son identifiant public lorsque la réservation n'existe pas
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void deleteBooking_ByPublicId_ShouldReturnNotFound() throws Exception {
        given(bookingService.existsByPublicId(any(UUID.class))).willReturn(false);

        Mockito.doNothing().when(bookingService).deleteBookingByPublicId(any(UUID.class));

        mockMvc.perform(delete("/api/bookings/publicId/{publicId}", bookingPublicId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).existsByPublicId(bookingPublicId);
        verify(bookingService, never()).deleteBookingByPublicId(any(UUID.class));
    }

    /**
     * Test pour la création d'une réservation par un utilisateur authentifié via token
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void saveBookingByToken_WhenValid_ShouldReturnCreated() throws Exception {

        when(userService.getIdByEmailUser(testUser.getEmailUser())).thenReturn(testUser.getIdUser());
        when(mapper.toEntityCreate(any(BookingCreateDTO.class), eq(testUser.getIdUser()), eq(testTerminal.getPublicId()), eq(testCar.getPublicId()), eq(testOption.getPublicId())))
                .thenReturn(testBooking);
        when(bookingService.saveBooking(any(Booking.class))).thenReturn(testBooking);
        when(mapper.toBookingDTO(any(Booking.class))).thenReturn(testBookingDTO);

        // Exécution de la requête POST
        mockMvc.perform(post("/api/bookings/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookingCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUser").value(testBookingDTO.getIdUser()))
                .andExpect(jsonPath("$.idTerminal").value(testBookingDTO.getIdTerminal().toString()))
                .andExpect(jsonPath("$.idCar").value(testBookingDTO.getIdCar().toString()));

        verify(userService, times(1)).getIdByEmailUser(testUser.getEmailUser());
        verify(mapper, times(1)).toEntityCreate(any(BookingCreateDTO.class), eq(testUser.getIdUser()), eq(testTerminal.getPublicId()), eq(testCar.getPublicId()), eq(testOption.getPublicId()));
        verify(bookingService, times(1)).saveBooking(testBooking);
        verify(mapper, times(1)).toBookingDTO(testBooking);
    }

    /**
     * Test pour la création d'une réservation par un utilisateur authentifié via token lorsque l'utilisateur n'est pas trouvé
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void saveBookingByToken_WhenUserNotFound_ShouldReturnIsUnauthorized() throws Exception {

        when(userService.getIdByEmailUser(testUser.getEmailUser())).thenReturn(null);

        mockMvc.perform(post("/api/bookings/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookingCreateDTO)))
                .andExpect(status().isUnauthorized());

        // Vérifier que la méthode getUserById a été appelée et que le reste ne l'a pas été
        verify(userService, times(1)).getIdByEmailUser(testUser.getEmailUser());
        verify(mapper, never()).toEntityCreate(any(), any(), any(), any(), any());
        verify(bookingService, never()).saveBooking(any(Booking.class));
        verify(mapper, never()).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la création d'une réservation par un utilisateur authentifié via token avec un corps de requête invalide
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void saveBookingByToken_WhenInvalidBody_ShouldReturnBadRequest() throws Exception {
        // Créer un DTO invalide
        BookingCreateDTO invalidBookingCreateDTO = BookingCreateDTO.builder().build();

        mockMvc.perform(post("/api/bookings/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookingCreateDTO)))
                .andExpect(status().isBadRequest()); // Attendre 400 Bad Request

        // Vérifier que le reste des méthodes n'a pas été appelé
        verify(userService, never()).getIdByEmailUser(anyString());
        verify(mapper, never()).toEntityCreate(any(), any(), any(), any(), any());
        verify(bookingService, never()).saveBooking(any(Booking.class));
        verify(mapper, never()).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la suppression d'une réservation par son identifiant lorsque la réservation n'existe pas
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"USER"})
    void deleteBooking_ByPublicId_WhenNotFound_ShouldReturnNotFound() throws Exception {
        Long nonExistentBookingId = 99L;

        when(bookingService.existsById(nonExistentBookingId)).thenReturn(false);

        mockMvc.perform(delete("/api/bookings/{id}", nonExistentBookingId)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(bookingService, times(1)).existsById(nonExistentBookingId);
        verify(bookingService, never()).deleteBookingByPublicId(UUID.randomUUID());
    }

    /**
     * Test pour la récupération des réservations par terminal et statut avec succès
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getBookingsTerminalAndStatus_Success() throws Exception {
        // Arrange
        List<Booking> bookings = List.of(testBooking);
        when(terminalService.getTerminalByPublicId(eq(testTerminal.getPublicId())))
                .thenReturn(testTerminal);
        when(bookingService.findByTerminalAndStatusBooking(
                argThat(terminal -> terminal != null && terminal.getIdTerminal().equals(testTerminal.getIdTerminal())),
                eq(BookingStatus.ACCEPTEE)))
                .thenReturn(bookings);
        when(mapper.toBookingDTO(testBooking)).thenReturn(testBookingDTO);

        // Act & Assert
        mockMvc.perform(get("/api/bookings/for-terminal/{terminalPublicId}/{status}",
                        testTerminal.getPublicId(),
                        BookingStatus.ACCEPTEE.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].publicId").value(testBookingDTO.getPublicId().toString()))
                .andExpect(jsonPath("$[0].idTerminal").value(testBookingDTO.getIdTerminal()))
                .andExpect(jsonPath("$[0].statusBooking").value(testBookingDTO.getStatusBooking().name()));

        // Vérification des appels de méthode
        verify(terminalService, times(1)).getTerminalByPublicId(eq(testTerminal.getPublicId()));
        verify(bookingService, times(1)).findByTerminalAndStatusBooking(
                argThat(terminal -> terminal != null && terminal.getIdTerminal().equals(testTerminal.getIdTerminal())),
                eq(BookingStatus.ACCEPTEE));
        verify(mapper, times(1)).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la récupération des réservations par terminal et statut lorsque aucune réservation n'est trouvée
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getBookingsTerminalAndStatus_NoBookingsFound() throws Exception {
        // Arrange
        BookingStatus status = BookingStatus.EN_ATTENTE;

        when(terminalService.getTerminalByPublicId(eq(testTerminal.getPublicId())))
                .thenReturn(testTerminal);

        when(bookingService.findByTerminalAndStatusBooking(
                argThat(terminal -> terminal != null && terminal.getIdTerminal().equals(testTerminal.getIdTerminal())),
                eq(status)))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/bookings/for-terminal/{terminalPublicId}/{status}",
                        testTerminal.getPublicId(),
                        status.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Vérification des appels de méthode
        verify(terminalService, times(1)).getTerminalByPublicId(eq(testTerminal.getPublicId()));
        verify(bookingService, times(1)).findByTerminalAndStatusBooking(
                argThat(terminal -> terminal != null && terminal.getIdTerminal().equals(testTerminal.getIdTerminal())),
                eq(status));
        verifyNoInteractions(mapper);
    }

    /**
     * Test pour la récupération des réservations par terminal et statut avec un statut de réservation invalide
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getBookingsTerminalAndStatus_InvalidBookingStatus_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String invalidStatus = "NON_EXISTANT_STATUS";

        // Act & Assert
        mockMvc.perform(get("/api/bookings/for-terminal/{terminalPublicId}/{status}",
                        testTerminal.getPublicId(),
                        invalidStatus)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists());

        // Puisque la conversion de l'enum échoue avant d'entrer dans la méthode du contrôleur,
        verifyNoInteractions(terminalService);
        verifyNoInteractions(bookingService);
        verifyNoInteractions(mapper);
    }

    /**
     * Test pour la récupération des réservations par terminal et statut lorsque le terminal n'est pas trouvé
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getBookingsTerminalAndStatus_TerminalNotFound() throws Exception {
        // Arrange
        UUID nonExistentTerminalPublicId = UUID.randomUUID();
        BookingStatus status = BookingStatus.ACCEPTEE;

        when(terminalService.getTerminalByPublicId(eq(nonExistentTerminalPublicId)))
                .thenThrow(new ResourceNotFoundException("Terminal with publicId not found: " + nonExistentTerminalPublicId));

        // Act & Assert
        mockMvc.perform(get("/api/bookings/for-terminal/{terminalPublicId}/{status}",
                        nonExistentTerminalPublicId,
                        status.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists());

        // Vérification des appels de méthode :
        verify(terminalService, times(1)).getTerminalByPublicId(eq(nonExistentTerminalPublicId));
        verifyNoInteractions(bookingService);
        verifyNoInteractions(mapper);
    }

    /**
     * Test pour la récupération des réservations par terminal et statut avec un statut de réservation invalide
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getBookingsTerminalAndStatus_ShouldReturnBadRequest() throws Exception {
        String invalidStatus = "INVALID_STATUS";

        // Exécution de la requête et vérification
        mockMvc.perform(get("/api/bookings/for-terminal/{terminalPublicId}/{statusParam}", testTerminal.getPublicId(), invalidStatus)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid BookingStatus value: " + invalidStatus));

        verifyNoInteractions(terminalService);
        verifyNoInteractions(bookingService);
        verifyNoInteractions(mapper);
    }

    /**
     * Test pour la récupération des réservations par utilisateur et statut
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getBookingsByUtilisateurAndstatus_ShouldReturnListOfBookings() throws Exception {
        // NOUVEAU: Mock pour userService.getUserByEmail
        when(userService.getUserByEmail(eq(testUser.getEmailUser())))
                .thenReturn(testUser);

        testBooking.setStatusBooking(BookingStatus.EN_ATTENTE);

        testBookingDTO.setStatusBooking(BookingStatus.EN_ATTENTE);

        // Mock du bookingService
        when(bookingService.findByUserAndStatusBooking(eq(testUser), eq(BookingStatus.EN_ATTENTE)))
                .thenReturn(List.of(testBooking));

        // Mock du mapper
        doReturn(testBookingDTO).when(mapper).toBookingDTO(any(Booking.class));

        // Préparation de la chaîne de temps attendue pour l'assertion JSON
        String expectedStartTimeString = testBookingDTO.getStartingDate().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        expectedStartTimeString = expectedStartTimeString.replaceAll("(\\.\\d*[1-9])0+$", "$1");
        expectedStartTimeString = expectedStartTimeString.replaceAll("\\.0+$", "");

        // Exécution de la requête et vérification
        mockMvc.perform(get("/api/bookings/{emailUser}/{status}", testUser.getEmailUser(), BookingStatus.EN_ATTENTE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publicId").value(testBookingDTO.getPublicId().toString()))
                .andExpect(jsonPath("$[0].idUser").value(testBookingDTO.getIdUser()))
                .andExpect(jsonPath("$[0].idTerminal").value(testBookingDTO.getIdTerminal()))
                .andExpect(jsonPath("$[0].statusBooking").value(BookingStatus.EN_ATTENTE.name()))
                .andExpect(jsonPath("$[0].startingDate").value(expectedStartTimeString));

        // Vérification des interactions avec les mocks
        verify(userService, times(1)).getUserByEmail(eq(testUser.getEmailUser()));
        verify(bookingService, times(1)).findByUserAndStatusBooking(eq(testUser), eq(BookingStatus.EN_ATTENTE));
        verify(mapper, times(1)).toBookingDTO(testBooking);
    }

    /**
     * Test pour la récupération des réservations par utilisateur et statut avec un statut invalide
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getBookingsByUtilisateurAndstatus_ShouldReturnEmptyList() throws Exception {
        when(userService.getUserByEmail(eq(testUser.getEmailUser())))
                .thenReturn(testUser);
        when(bookingService.findByUserAndStatusBooking(eq(testUser), eq(BookingStatus.ACCEPTEE)))
                .thenReturn(Collections.emptyList());

        // Exécution de la requête et vérification
        mockMvc.perform(get("/api/bookings/{emailUser}/{status}", testUser.getEmailUser(), BookingStatus.ACCEPTEE) // NOUVEAU: Utilisation de l'email
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        // Vérification des interactions avec les mocks
        verify(userService, times(1)).getUserByEmail(eq(testUser.getEmailUser())); // NOUVEAU: Vérification de l'appel au userService
        verify(bookingService, times(1)).findByUserAndStatusBooking(eq(testUser), eq(BookingStatus.ACCEPTEE));
        verify(mapper, times(0)).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la récupération des réservations par utilisateur et statut lorsque l'utilisateur n'est pas trouvé
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getBookingsByUtilisateurAndstatus_UserNotFound() throws Exception {
        when(userService.getUserByEmail(eq("nonexistent@example.com")))
                .thenThrow(new ResourceNotFoundException("User not found with email: nonexistent@example.com"));

        // Exécution de la requête et vérification
        mockMvc.perform(get("/api/bookings/{emailUser}/{status}", "nonexistent@example.com", BookingStatus.EN_ATTENTE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserByEmail(eq("nonexistent@example.com"));
        verify(bookingService, times(0)).findByUserAndStatusBooking(any(), any());
        verify(mapper, times(0)).toBookingDTO(any());
    }

    /**
     * Test pour la récupération des réservations par statut
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getBookingsBystatus_ShouldReturnListOfBookings() throws Exception {
        // Définir le statut pour ce test
        BookingStatus targetStatus = BookingStatus.ACCEPTEE;
        testBooking.setStatusBooking(targetStatus);
        testBookingDTO.setStatusBooking(targetStatus);

        // Mock du bookingService pour retourner une liste de réservations
        when(bookingService.findByStatusBooking(eq(targetStatus)))
                .thenReturn(List.of(testBooking));

        // Mock du mapper pour convertir l'entité en DTO
        when(mapper.toBookingDTO(eq(testBooking)))
                .thenReturn(testBookingDTO);

        // Préparation de la chaîne de temps attendue pour l'assertion JSON
        String expectedStartTimeString = testBookingDTO.getStartingDate().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        // Ajustements pour correspondre au format par défaut de Spring Boot pour LocalDateTime dans JSON
        expectedStartTimeString = expectedStartTimeString.replaceAll("(\\.\\d*[1-9])0+$", "$1");
        expectedStartTimeString = expectedStartTimeString.replaceAll("\\.0+$", "");

        // Exécution de la requête et vérification
        mockMvc.perform(get("/api/bookings/status/{status}", targetStatus)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publicId").value(testBookingDTO.getPublicId().toString()))
                .andExpect(jsonPath("$[0].idUser").value(testBookingDTO.getIdUser()))
                .andExpect(jsonPath("$[0].idTerminal").value(testBookingDTO.getIdTerminal()))
                .andExpect(jsonPath("$[0].statusBooking").value(targetStatus.name()))
                .andExpect(jsonPath("$[0].startingDate").value(expectedStartTimeString));

        // Vérification des interactions avec les mocks
        verify(bookingService, times(1)).findByStatusBooking(eq(targetStatus));
        verify(mapper, times(1)).toBookingDTO(eq(testBooking));
    }

    /**
     * Test pour la récupération des réservations par statut lorsque aucune réservation n'est trouvée
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getBookingsBystatus_NoBookingsFound() throws Exception {
        BookingStatus targetStatus = BookingStatus.EN_ATTENTE;

        // Mock du bookingService pour retourner une liste vide
        when(bookingService.findByStatusBooking(eq(targetStatus)))
                .thenReturn(Collections.emptyList());

        // Exécution de la requête et vérification
        mockMvc.perform(get("/api/bookings/status/{status}", targetStatus)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        // Vérification des interactions avec les mocks
        verify(bookingService, times(1)).findByStatusBooking(eq(targetStatus));
        verify(mapper, never()).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la récupération des réservations par terminal
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getBookingsByTerminal_ShouldReturnListOfBookings() throws Exception {
        UUID terminalId = testTerminal.getPublicId();

        // Mock du terminalService pour retourner la borne quand l'ID est demandé
        when(terminalService.getTerminalByPublicId(eq(terminalId)))
                .thenReturn(testTerminal);

        // Mock du bookingService pour retourner une liste de réservations associées à cette borne
        when(bookingService.findByTerminal(eq(testTerminal)))
                .thenReturn(List.of(testBooking));

        // Mock du mapper pour convertir l'entité en DTO
        when(mapper.toBookingDTO(eq(testBooking)))
                .thenReturn(testBookingDTO);

        // Préparation de la chaîne de temps attendue pour l'assertion JSON
        String expectedStartTimeString = testBookingDTO.getStartingDate().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        expectedStartTimeString = expectedStartTimeString.replaceAll("(\\.\\d*[1-9])0+$", "$1");
        expectedStartTimeString = expectedStartTimeString.replaceAll("\\.0+$", "");

        // Exécution de la requête et vérification
        mockMvc.perform(get("/api/bookings/terminal/{terminalId}", terminalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publicId").value(testBookingDTO.getPublicId().toString()))
                .andExpect(jsonPath("$[0].idUser").value(testBookingDTO.getIdUser()))
                .andExpect(jsonPath("$[0].idTerminal").value(testBookingDTO.getIdTerminal()))
                .andExpect(jsonPath("$[0].statusBooking").value(testBookingDTO.getStatusBooking().name()))
                .andExpect(jsonPath("$[0].startingDate").value(expectedStartTimeString));

        // Vérification des interactions avec les mocks
        verify(terminalService, times(1)).getTerminalByPublicId(eq(terminalId));
        verify(bookingService, times(1)).findByTerminal(eq(testTerminal));
        verify(mapper, times(1)).toBookingDTO(eq(testBooking));
    }

    /**
     * Test pour la récupération des réservations par terminal lorsque aucune réservation n'est trouvée
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getBookingsByTerminal_NoBookingsFoundForTerminal() throws Exception {
        UUID terminalId = testTerminal.getPublicId();

        // Mock du terminalService pour retourner la borne quand l'ID est demandé
        when(terminalService.getTerminalByPublicId(eq(terminalId)))
                .thenReturn(testTerminal);
        when(bookingService.findByTerminal(eq(testTerminal)))
                .thenReturn(Collections.emptyList());

        // Exécution de la requête et vérification
        mockMvc.perform(get("/api/bookings/terminal/{terminalId}", terminalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        // Vérification des interactions avec les mocks
        verify(terminalService, times(1)).getTerminalByPublicId(eq(terminalId));
        verify(bookingService, times(1)).findByTerminal(eq(testTerminal));
        verify(mapper, never()).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la récupération des réservations par terminal lorsque le terminal n'est pas trouvé
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void getBookingsByTerminal_TerminalNotFound() throws Exception {
        UUID nonexistentTerminalId = UUID.randomUUID();

        // Mock du terminalService pour simuler une borne non trouvée
        when(terminalService.getTerminalByPublicId(eq(nonexistentTerminalId)))
                .thenThrow(new ResourceNotFoundException("Terminal not found with ID: " + nonexistentTerminalId));

        // Exécution de la requête et vérification
        mockMvc.perform(get("/api/bookings/terminal/{terminalId}", nonexistentTerminalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Vérification des interactions avec les mocks
        verify(terminalService, times(1)).getTerminalByPublicId(eq(nonexistentTerminalId));
        verify(bookingService, never()).findByTerminal(any(Terminal.class));
        verify(mapper, never()).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la mise à jour d'une réservation existante
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ADMIN"})
    void updateBooking_ShouldReturnUpdatedBooking() throws Exception {
        final Long existingBookingId = 1L;

        // Préparation des mocks
        when(bookingService.existsById(eq(existingBookingId))).thenReturn(true);

        Booking bookingFromDto = new Booking();
        bookingFromDto.setPublicId(updatedBookingDTO.getPublicId());
        bookingFromDto.setIdBooking(existingBookingId);
        bookingFromDto.setStatusBooking(updatedBookingDTO.getStatusBooking());
        bookingFromDto.setStartingDate(updatedBookingDTO.getStartingDate());
        bookingFromDto.setEndingDate(updatedBookingDTO.getEndingDate());
        bookingFromDto.setTerminal(testTerminal);
        bookingFromDto.setUser(testUser);

        when(mapper.toEntity(eq(updatedBookingDTO))).thenReturn(bookingFromDto);

        when(bookingService.updateBooking(eq(existingBookingId), any(Booking.class))).thenReturn(updatedBookingEntity);
        when(mapper.toBookingDTO(eq(updatedBookingEntity))).thenReturn(updatedBookingDTO);

        String expectedStartingDateString = updatedBookingDTO.getStartingDate().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        expectedStartingDateString = expectedStartingDateString.replaceAll("(\\.\\d*[1-9])0+$", "$1");
        expectedStartingDateString = expectedStartingDateString.replaceAll("\\.0+$", "");

        String expectedEndingDateString = updatedBookingDTO.getEndingDate().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        expectedEndingDateString = expectedEndingDateString.replaceAll("(\\.\\d*[1-9])0+$", "$1");
        expectedEndingDateString = expectedEndingDateString.replaceAll("\\.0+$", "");

        // Exécution de la requête PUT
        mockMvc.perform(put("/api/bookings/{id}", existingBookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBookingDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(updatedBookingDTO.getPublicId().toString()))
                .andExpect(jsonPath("$.idUser").value(updatedBookingDTO.getIdUser()))
                .andExpect(jsonPath("$.idTerminal").value(updatedBookingDTO.getIdTerminal()))
                .andExpect(jsonPath("$.statusBooking").value(updatedBookingDTO.getStatusBooking().name()))
                .andExpect(jsonPath("$.startingDate").value(expectedStartingDateString))
                .andExpect(jsonPath("$.endingDate").value(expectedEndingDateString));

        // Vérification des interactions avec les mocks
        verify(bookingService, times(1)).existsById(eq(existingBookingId));
        verify(mapper, times(1)).toEntity(eq(updatedBookingDTO));
        verify(bookingService, times(1)).updateBooking(eq(existingBookingId), any(Booking.class));
        verify(mapper, times(1)).toBookingDTO(eq(updatedBookingEntity));
    }

    /**
     * Test pour la mise à jour d'une réservation inexistante
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ADMIN"})
    void updateBooking_ShouldReturnNotFoundIfBookingDoesNotExist() throws Exception {
        // Mock: Le service indique que la réservation n'existe pas
        Long nonExistentId = 99L;
        when(bookingService.existsById(eq(nonExistentId))).thenReturn(false);

        // Exécution de la requête PUT
        mockMvc.perform(put("/api/bookings/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBookingDTO)))
                .andExpect(status().isNotFound());

        // Vérification qu'aucune logique métier d'update n'a été appelée
        verify(bookingService, times(1)).existsById(eq(nonExistentId));
        verify(mapper, never()).toEntity(any(BookingDTO.class));
        verify(bookingService, never()).updateBooking(anyLong(), any(Booking.class));
        verify(mapper, never()).toBookingDTO(any(Booking.class));
    }

    /**
     * Test pour la mise à jour d'une réservation avec un DTO invalide
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ADMIN"})
    void updateBooking_ShouldReturnBadRequestForInvalidBookingDTO() throws Exception {
        // Préparation des données
        BookingDTO invalidBookingDTO = new BookingDTO();
        invalidBookingDTO.setPublicId(UUID.randomUUID());
        invalidBookingDTO.setNumberBooking("BOOK-INVALID");
        invalidBookingDTO.setStartingDate(null);
        invalidBookingDTO.setEndingDate(LocalDateTime.now().plusHours(2));
        invalidBookingDTO.setStatusBooking(BookingStatus.EN_ATTENTE);
        invalidBookingDTO.setIdUser(1L);
        invalidBookingDTO.setIdTerminal(testTerminal.getIdTerminal());

        // ID de réservation existant pour la mise à jour
        mockMvc.perform(put("/api/bookings/{id}", existingBookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookingDTO)))
                .andExpect(status().isBadRequest());

        // Vérification qu'aucune logique métier d'update n'a été appelée
        verify(bookingService, never()).existsById(anyLong());
        verify(mapper, never()).toEntity(any(BookingDTO.class));
        verify(bookingService, never()).updateBooking(anyLong(), any(Booking.class));
        verify(mapper, never()).toBookingDTO(any(Booking.class));
    }
}
