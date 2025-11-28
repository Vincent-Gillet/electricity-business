/*
package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.config.TestSecurityConfig;
import com.electricitybusiness.api.dto.booking.BookingCreateDTO; // Importer le DTO de création
import com.electricitybusiness.api.dto.booking.BookingDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.*;
import com.electricitybusiness.api.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*; // Importer all static matchers
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = BookingController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com.electricitybusiness.api.config.*"  // Exclut tout le package config
        ),
        excludeAutoConfiguration = {
                ErrorMvcAutoConfiguration.class,
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class) // Ou la configuration de sécurité appropriée
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService; // Utilisez @MockBean au lieu de @Mock

    @MockitoBean
    private EntityMapper mapper;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper; // Pour sérialiser/désérialiser les objets JSON

    private Booking testBooking;
    private BookingDTO testBookingDTO;
    private BookingCreateDTO testBookingCreateDTO;
    private User testUser;

    @MockitoBean
    private Terminal testTerminal;
    @MockitoBean
    private TerminalService terminalService;
    @MockitoBean
    private CarService carService;
    @Autowired
    private EntityMapper entityMapper;
    @Autowired
    private AddressService addressService;

    @BeforeEach
    void setUp() {
        // Initialisation des données de test
        testUser = new User();
        testUser.setIdUser(1L);
        testUser.setEmailUser("testuser@example.com");
        testUser.setSurnameUser("testuser");

        testTerminal = new Terminal();
        testTerminal.setIdTerminal(1L);
        testTerminal.setNameTerminal("Terminal 1");

        LocalDateTime futureStartingDate = LocalDateTime.now().plusDays(1); // Essayez plusDays(1) ou plusHours(24)
        LocalDateTime futureEndingDate = futureStartingDate.plusHours(2);

        testBooking = new Booking();
        testBooking.setIdBooking(1L);
        testBooking.setPublicId(UUID.randomUUID());
        testBooking.setUser(testUser);
        testBooking.setStartingDate(futureStartingDate);
        testBooking.setEndingDate(futureEndingDate);
        testBooking.setTotalAmount(BigDecimal.valueOf(60.0));


        testBookingCreateDTO = new BookingCreateDTO();
        testBookingCreateDTO.setNumberBooking("BK-123456");
        testBookingCreateDTO.setStartingDate(futureStartingDate);
        testBookingCreateDTO.setEndingDate(futureEndingDate);
        testBookingCreateDTO.setStatusBooking(BookingStatus.EN_ATTENTE);
        testBookingCreateDTO.setTotalAmount(BigDecimal.valueOf(60.0));
        testBookingCreateDTO.setPaymentDate(LocalDateTime.of(2025, 11, 27, 14, 0));
        testBookingCreateDTO.setIdUser(testUser.getIdUser());
        testBookingCreateDTO.setPublicIdTerminal(UUID.randomUUID());
        testBookingCreateDTO.setPublicIdCar(UUID.randomUUID());
        testBookingCreateDTO.setPublicIdOption(UUID.randomUUID());

        testBookingDTO = new BookingDTO();
        testBookingDTO.setPublicId(testBooking.getPublicId());
        testBookingDTO.setIdUser(testUser.getIdUser());
        testBookingDTO.setStartingDate(testBooking.getStartingDate());
        testBookingDTO.setEndingDate(testBooking.getEndingDate());

        UUID terminalPublicId = testBookingCreateDTO.getPublicIdTerminal(); // Utiliser l'UUID déjà généré pour DTO
        UUID carPublicId = testBookingCreateDTO.getPublicIdCar();
        UUID addressPublicId = UUID.randomUUID();

        // Mock pour getTerminalByPublicId (si appelé par le mapper)
        Terminal mockTerminal = new Terminal();
        mockTerminal.setPublicId(terminalPublicId);
        mockTerminal.setIdTerminal(1L); // Assurez-vous que l'ID interne est aussi setté si nécessaire
        // ... autres propriétés de mockTerminal si nécessaire
        given(terminalService.getTerminalByPublicId(terminalPublicId)).willReturn(mockTerminal);

        Address mockAddress = new Address();
        mockAddress.setPublicId(addressPublicId);
        mockAddress.setIdAddress(1L);
        given(entityMapper.toEntity(addressService.getAddressDTOByPublicId(mockAddress.getPublicId())).willReturn(mockAddress));

        // Mock pour getCarByPublicId (si appelé par le mapper)
        Car mockCar = new Car();
        mockCar.setPublicId(carPublicId);
        mockCar.setIdCar(1L); // Assurez-vous que l'ID interne est aussi setté si nécessaire
        // ... autres propriétés de mockCar si nécessaire
        given(carService.getCarByPublicId(carPublicId)).willReturn(mockCar);

        Booking bookingToSave = new Booking();
        bookingToSave.setIdBooking(1L); // ID interne
        bookingToSave.setPublicId(UUID.randomUUID()); // Le nouveau publicId généré pour la réservation
        bookingToSave.setUser(testUser);
        bookingToSave.setStartingDate(testBookingCreateDTO.getStartingDate());
        bookingToSave.setEndingDate(testBookingCreateDTO.getEndingDate());
        bookingToSave.setTerminal(mockTerminal); // Utiliser le mock récupéré
        bookingToSave.setCar(mockCar); // Utiliser le mock récupéré
        bookingToSave.setStatusBooking(testBookingCreateDTO.getStatusBooking()); // Le statut initial
        // ... autres champs

        // Mock du mapper pour convertir BookingCreateDTO en Booking
        given(mapper.toEntityCreate(
                eq(testBookingCreateDTO),
                eq(testUser.getIdUser()),
                eq(testBookingCreateDTO.getPublicIdTerminal()),
                eq(testBookingCreateDTO.getPublicIdCar()),
                eq(testBookingCreateDTO.getPublicIdOption())
        )).willReturn(bookingToSave);

        Booking savedBooking = bookingToSave; // Le service renvoie l'entité qu'il a sauvegardée
        savedBooking.setIdBooking(100L); // On peut changer l'ID interne pour simuler un enregistrement DB
        given(bookingService.saveBooking(any(Booking.class))).willReturn(savedBooking);

        // Mock du mapper pour convertir Booking en BookingDTO
        // Le DTO final doit refléter l'entité sauvegardée
        BookingDTO finalBookingDTO = new BookingDTO();
        finalBookingDTO.setPublicId(savedBooking.getPublicId());
        finalBookingDTO.setIdUser(testUser.getIdUser()); // Ou savedBooking.getUser().getIdUser()
        finalBookingDTO.setStartingDate(savedBooking.getStartingDate());
        finalBookingDTO.setEndingDate(savedBooking.getEndingDate());
        // ... remplir les autres champs de finalBookingDTO à partir de savedBooking

        given(mapper.toDTO(eq(savedBooking))).willReturn(finalBookingDTO);
    }


    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void getAllBookings_WithAdminRole_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/bookings/all"))
                .andExpect(status().isOk());
    }

    // Test pour la création d'une réservation
    @Test
    @WithMockUser(username = "testuser@example.com", authorities = {"USER"})
    void saveBookingByToken_ShouldReturnCreatedBooking() throws Exception {
        // ... votre code de test ...
        // Assurez-vous que les objets attendus par le mapper (bookingToSave, savedBooking)
        // sont créés avec les bonnes valeurs basées sur testBookingCreateDTO et testUser.

        // Mock du mapper pour convertir BookingCreateDTO en Booking
        Booking bookingToSave = new Booking();
        bookingToSave.setUser(testUser);
        bookingToSave.setStartingDate(testBookingCreateDTO.getStartingDate()); // Utilise la date future initialisée
        bookingToSave.setEndingDate(testBookingCreateDTO.getEndingDate());     // Utilise la date future initialisée
        bookingToSave.setTerminal(terminalService.getTerminalByPublicId(testBookingCreateDTO.getPublicIdTerminal())); // Assurez-vous que c'est mappé
        bookingToSave.setCar(carService.getCarByPublicId(testBookingCreateDTO.getPublicIdCar()));     // Assurez-vous que c'est mappé
        // ... et d'autres champs si le mapper les utilise

        given(mapper.toEntityCreate(
                eq(testBookingCreateDTO),
                eq(testUser.getIdUser()),
                eq(testBookingCreateDTO.getPublicIdTerminal()), // Passez les vraies valeurs attendues par le mapper
                eq(testBookingCreateDTO.getPublicIdCar()),
                eq(testBookingCreateDTO.getPublicIdOption()) // Si publicIdOption est présent dans le DTO
        )).willReturn(bookingToSave);

        UUID generatedPublicId = UUID.randomUUID();

        // ... Mock du service pour sauvegarder la réservation ...
        Booking savedBooking = new Booking();
        savedBooking.setIdBooking(100L);
        savedBooking.setPublicId(generatedPublicId);
        savedBooking.setUser(testUser);
        savedBooking.setCar(carService.getCarByPublicId(testBookingCreateDTO.getPublicIdCar()));
        savedBooking.setTerminal(terminalService.getTerminalByPublicId(testBookingCreateDTO.getPublicIdTerminal()));
        savedBooking.setOption(null); // Si une option est associée, la définir ici
        savedBooking.setNumberBooking("BK-123456");
        savedBooking.setStatusBooking(testBookingCreateDTO.getStatusBooking());
        savedBooking.setTotalAmount(testBookingCreateDTO.getTotalAmount());
        savedBooking.setPaymentDate(testBookingCreateDTO.getPaymentDate());
        savedBooking.setStartingDate(testBookingCreateDTO.getStartingDate()); // Assurez-vous que le service retourne les mêmes dates ou des dates dérivées
        savedBooking.setEndingDate(testBookingCreateDTO.getEndingDate());

        given(bookingService.saveBooking(any(Booking.class))).willReturn(savedBooking);

        // ... Mock du mapper pour convertir Booking en BookingDTO ...
        BookingDTO finalBookingDTO = new BookingDTO();
        finalBookingDTO.setPublicId(savedBooking.getPublicId()); // <-- Ceci est OK
        finalBookingDTO.setNumberBooking("BK-123456"); // <-- Ceci est OK
        finalBookingDTO.setStartingDate(savedBooking.getStartingDate()); // <-- Ceci est OK
        finalBookingDTO.setEndingDate(savedBooking.getEndingDate()); // <-- Ceci est OK
        finalBookingDTO.setStatusBooking(testBookingCreateDTO.getStatusBooking()); // <-- Ceci est OK
        finalBookingDTO.setTotalAmount(testBookingCreateDTO.getTotalAmount()); // <-- Ceci est OK
        finalBookingDTO.setPaymentDate(testBookingCreateDTO.getPaymentDate()); // <-- Ceci est OK
        finalBookingDTO.setUserClientDTO(entityMapper.toDTO(testUser)); // <-- Peut être OK si non attendu
        finalBookingDTO.setUserOwnerDTO(entityMapper.toDTO(testUser)); // <-- Peut être OK si non attendu
        finalBookingDTO.setAddressDTO(entityMapper.toDTO()); // <-- Peut être OK si non attendu
        finalBookingDTO.setTerminalDTO(entityMapper.toDTO(testTerminal)); // <-- Ceci EST UN PROBLEME POTENTIEL
        finalBookingDTO.setIdUser(testUser.getIdUser()); // <-- Ceci est OK
        finalBookingDTO.setIdTerminal(savedBooking.getTerminal().getIdTerminal()); // <-- Ceci EST UN PROBLEME POTENTIEL
        finalBookingDTO.setIdCar(savedBooking.getCar().getIdCar()); // <-- Ceci est OK
        finalBookingDTO.setIdOption(0L);

        given(mapper.toDTO(any(Booking.class))).willReturn(finalBookingDTO);

        // Exécution du test
        mockMvc.perform(post("/api/bookings/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testBookingCreateDTO))) // Envoie le DTO préparé
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").value(finalBookingDTO.getPublicId().toString()))
                .andExpect(jsonPath("$.idUser").value(finalBookingDTO.getIdUser()));
    }

    // Test pour la récupération de toutes les réservations (ADMIN)
    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ADMIN"})
    void getAllBookings_ShouldReturnListOfBookings() throws Exception {
        given(bookingService.getAllBookings()).willReturn(Collections.singletonList(testBooking));
        given(mapper.toDTO(testBooking)).willReturn(testBookingDTO);

        mockMvc.perform(get("/api/bookings/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].publicId").value(testBookingDTO.getPublicId().toString()));
    }

    // Test pour la récupération d'une réservation par ID (ADMIN)
    @Test
    @WithMockUser(username = "admin@example.com", authorities = {"ADMIN"})
    void getBookingById_ShouldReturnBooking() throws Exception {
        given(bookingService.getBookingById(anyLong())).willReturn(Optional.of(testBooking));
        given(mapper.toDTO(testBooking)).willReturn(testBookingDTO);

        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value(testBookingDTO.getPublicId().toString()));
    }

    // Test pour la suppression d'une réservation (utilisateur authentifié)
    @Test
    @WithMockUser(username = "testuser@example.com")
    void deleteBooking_ShouldReturnNoContent() throws Exception {
        given(bookingService.existsById(anyLong())).willReturn(true);
        Mockito.doNothing().when(bookingService).deleteBookingById(anyLong());

        mockMvc.perform(delete("/api/bookings/1"))
                .andExpect(status().isNoContent());
    }
}

*/
