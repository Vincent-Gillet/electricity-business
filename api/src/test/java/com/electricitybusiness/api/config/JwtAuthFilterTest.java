package com.electricitybusiness.api.config;

import com.electricitybusiness.api.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTest {
    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @Mock
    private HandlerExceptionResolver handlerExceptionResolver;

    private UserDetails mockUserDetails;
    private final String TEST_EMAIL = "test@example.com";
    private final String VALID_JWT = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjQzMjAxNjAwLCJleHAiOjE5NTg3MzYwMDB9.someSignature"; // Un token valide (pas réellement valide, juste pour le mock)

    @BeforeEach
    void setUp() {
        // Nettoyer le contexte de sécurité avant chaque test
        SecurityContextHolder.clearContext();
        // Créer un UserDetails mocké pour simuler la base de données
        mockUserDetails = new User(TEST_EMAIL, "password", Collections.emptyList());
    }

    @Test
    void doFilterInternal_NoAuthorizationHeader_ContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString()); // Aucune interaction avec JwtService
        assertNull(SecurityContextHolder.getContext().getAuthentication()); // Pas d'authentification
    }

    @Test
    void doFilterInternal_AuthorizationHeaderWithoutBearer_ContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(VALID_JWT); // Manque le préfixe "Bearer "

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService); // Aucune interaction avec JwtService car le préfixe est manquant
        verifyNoInteractions(userDetailsService);
        verifyNoInteractions(handlerExceptionResolver);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_TokenWithoutBearerPrefix_ContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(VALID_JWT); // Pas de "Bearer "

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, never()).extractUsername(anyString()); // Aucune tentative d'extraction
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidToken_UserFound_AuthenticatesUser() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_JWT);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(TEST_EMAIL);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(mockUserDetails);
        when(jwtService.isTokenValid(VALID_JWT, mockUserDetails)).thenReturn(true);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, times(1)).extractUsername(VALID_JWT);
        verify(userDetailsService, times(1)).loadUserByUsername(TEST_EMAIL);
        verify(jwtService, times(1)).isTokenValid(VALID_JWT, mockUserDetails);

        // Vérifier que le contexte de sécurité a été mis à jour
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        assertEquals(TEST_EMAIL, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void doFilterInternal_ValidToken_UserNotFound_ContinuesFilterChainWithoutAuthenticating() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_JWT);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(TEST_EMAIL);
        // Simuler que loadUserByUsername renvoie null ou lance une exception (par exemple UsernameNotFoundException)
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(null);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtService, times(1)).extractUsername(VALID_JWT);
        verify(userDetailsService, times(1)).loadUserByUsername(TEST_EMAIL);
        verify(jwtService, never()).isTokenValid(anyString(), any()); // Pas de validation si l'utilisateur n'est pas trouvé
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(handlerExceptionResolver);
        assertNull(SecurityContextHolder.getContext().getAuthentication()); // Pas d'authentification
    }

    @Test
    void doFilterInternal_ValidToken_ButNotValid_ContinuesFilterChain() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_JWT);
        when(jwtService.extractUsername(VALID_JWT)).thenReturn(TEST_EMAIL);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(mockUserDetails);
        when(jwtService.isTokenValid(VALID_JWT, mockUserDetails)).thenReturn(false); // Token non valide

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        verify(jwtService, times(1)).extractUsername(VALID_JWT);
        verify(userDetailsService, times(1)).loadUserByUsername(TEST_EMAIL);
        verify(jwtService, times(1)).isTokenValid(VALID_JWT, mockUserDetails);
        assertNull(SecurityContextHolder.getContext().getAuthentication()); // Pas d'authentification
    }



}
