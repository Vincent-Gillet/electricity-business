package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.config.JwtAuthFilter;
import com.electricitybusiness.api.config.TestSecurityConfig;
import com.electricitybusiness.api.dto.terminal.TerminalDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Terminal;
import com.electricitybusiness.api.service.TerminalService;
import com.electricitybusiness.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TerminalController.class,
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
class TerminalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TerminalService terminalService;

    @MockitoBean
    private EntityMapper mapper;

    @MockitoBean
    private UserService userService;

    private Terminal testTerminal1;
    private Terminal testTerminal2;
    private TerminalDTO testTerminalDTO1;
    private TerminalDTO testTerminalDTO2;

    @BeforeEach
    void setUp() {
        UUID testTerminalUUID1 = UUID.randomUUID();
        UUID testTerminalUUID2 = UUID.randomUUID();

        // Initialisation des objets Terminal factices
        testTerminal1 = new Terminal();
        testTerminal1.setIdTerminal(1L);
        testTerminal1.setPublicId(testTerminalUUID1);
        testTerminal1.setNameTerminal("Terminal A");
        testTerminal1.setLatitude(BigDecimal.valueOf(48.8566));
        testTerminal1.setLongitude(BigDecimal.valueOf(2.3522));
        testTerminal1.setOccupied(false);

        testTerminal2 = new Terminal();
        testTerminal2.setIdTerminal(2L);
        testTerminal1.setPublicId(testTerminalUUID2);
        testTerminal2.setNameTerminal("Terminal B");
        testTerminal2.setLatitude(BigDecimal.valueOf(48.8600));
        testTerminal2.setLongitude(BigDecimal.valueOf(2.3600));
        testTerminal2.setOccupied(true);

        // Initialisation des objets TerminalDTO factices
        testTerminalDTO1 = new TerminalDTO();
        testTerminalDTO1.setPublicId(testTerminalUUID1);
        testTerminalDTO1.setNameTerminal("Terminal A DTO");
        testTerminalDTO1.setLatitude(BigDecimal.valueOf(48.8566));
        testTerminalDTO1.setLongitude(BigDecimal.valueOf(2.3522));
        testTerminalDTO1.setOccupied(false);

        testTerminalDTO2 = new TerminalDTO();
        testTerminalDTO2.setPublicId(testTerminalUUID2);
        testTerminalDTO2.setNameTerminal("Terminal B DTO");
        testTerminalDTO2.setLatitude(BigDecimal.valueOf(48.8600));
        testTerminalDTO2.setLongitude(BigDecimal.valueOf(2.3600));
        testTerminalDTO2.setOccupied(true);

        // Mock le comportement du mapper pour tous les tests
        when(mapper.toTerminalDTO(testTerminal1)).thenReturn(testTerminalDTO1);
        when(mapper.toTerminalDTO(testTerminal2)).thenReturn(testTerminalDTO2);
    }

    /**
     * Teste la recherche de terminaux avec tous les paramètres fournis.
     * Vérifie que le contrôleur renvoie les terminaux filtrés correctement.
     */
    @Test
    @WithMockUser
    void searchTerminals_WithAllParameters_ReturnsFilteredTerminals() throws Exception {
        BigDecimal longitude = BigDecimal.valueOf(2.35);
        BigDecimal latitude = BigDecimal.valueOf(48.85);
        Double radius = 1.0;
        Boolean occupied = false;
        LocalDateTime startingDate = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime endingDate = LocalDateTime.of(2023, 12, 31, 23, 59);

        List<Terminal> filteredTerminals = Collections.singletonList(testTerminal2);

        when(terminalService.searchTerminals(
                eq(longitude), eq(latitude), eq(radius), eq(occupied), eq(startingDate), eq(endingDate)
        )).thenReturn(filteredTerminals);

        mockMvc.perform(get("/api/terminals/search-terminals")
                        .param("longitude", longitude.toString())
                        .param("latitude", latitude.toString())
                        .param("radius", radius.toString())
                        .param("occupied", occupied.toString())
                        .param("startingDate", startingDate.toString())
                        .param("endingDate", endingDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].publicId").value(testTerminalDTO2.getPublicId().toString()));

        verify(terminalService).searchTerminals(
                eq(longitude), eq(latitude), eq(radius), eq(occupied), eq(startingDate), eq(endingDate)
        );
        verify(mapper, times(1)).toTerminalDTO(testTerminal2);
        verifyNoMoreInteractions(terminalService, mapper);
    }

    /**
     * Teste le scénario où les paramètres obligatoires (longitude, latitude, radius) sont absents.
     * Le contrôleur doit renvoyer un 400 Bad Request.
     */
    @Test
    @WithMockUser
    void searchTerminals_MissingMandatoryParameters_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/terminals/search-terminals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(terminalService, mapper);
    }

    /**
     * Teste la recherche avec uniquement les paramètres de localisation (longitude, latitude, radius).
     */
    @Test
    @WithMockUser
    void searchTerminals_WithLocationAndOccupiedParameters_ReturnsFilteredTerminals() throws Exception {
        BigDecimal longitude = BigDecimal.valueOf(2.35);
        BigDecimal latitude = BigDecimal.valueOf(48.85);
        Double radius = 1.0;
        Boolean occupied = false;

        List<Terminal> filteredTerminals = Collections.singletonList(testTerminal1);

        when(terminalService.searchTerminals(
                eq(longitude), eq(latitude), eq(radius), eq(occupied), isNull(), isNull()
        )).thenReturn(filteredTerminals);

        mockMvc.perform(get("/api/terminals/search-terminals")
                        .param("longitude", longitude.toString())
                        .param("latitude", latitude.toString())
                        .param("radius", radius.toString())
                        .param("occupied", occupied.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].publicId").value(testTerminalDTO1.getPublicId().toString()));

        verify(terminalService).searchTerminals(
                eq(longitude), eq(latitude), eq(radius), eq(occupied), isNull(), isNull()
        );
        verify(mapper, times(1)).toTerminalDTO(testTerminal1);
        verifyNoMoreInteractions(terminalService, mapper);
    }

    /**
     * Teste le cas où un paramètre numérique (BigDecimal) a un format invalide.
     * Spring MVC devrait renvoyer un 400 Bad Request.
     */
    @Test
    @WithMockUser
    void searchTerminals_InvalidNumericParameter_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/terminals/search-terminals")
                        .param("longitude", "invalid_number")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(terminalService, mapper);
    }

    /**
     * Teste le cas où un paramètre booléen a un format invalide.
     * Spring MVC devrait renvoyer un 400 Bad Request.
     */
    @Test
    @WithMockUser
    void searchTerminals_InvalidBooleanParameter_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/terminals/search-terminals")
                        .param("occupied", "not_a_boolean")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(terminalService, mapper);
    }

    /**
     * Teste le cas où un paramètre de date/heure a un format invalide.
     * Spring MVC devrait renvoyer un 400 Bad Request.
     */
    @Test
    @WithMockUser
    void searchTerminals_InvalidDateParameter_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/terminals/search-terminals")
                        .param("startingDate", "not_a_date")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(terminalService, mapper);
    }

    @Test
    @WithMockUser
    void searchTerminals_NoMatchingTerminals_ReturnsEmptyList() throws Exception {
        BigDecimal longitude = BigDecimal.valueOf(1.0);
        BigDecimal latitude = BigDecimal.valueOf(1.0);
        Double radius = 0.5;
        Boolean occupied = false;

        when(terminalService.searchTerminals(
                eq(longitude), eq(latitude), eq(radius), eq(occupied), isNull(), isNull()
        )).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/terminals/search-terminals")
                        .param("longitude", longitude.toString())
                        .param("latitude", latitude.toString())
                        .param("radius", radius.toString())
                        .param("occupied", occupied.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(terminalService).searchTerminals(
                eq(longitude), eq(latitude), eq(radius), eq(occupied), isNull(), isNull()
        );
        verifyNoMoreInteractions(terminalService, mapper);
    }

}
