package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.config.CustomUserDetailService;
import com.electricitybusiness.api.config.JwtAuthFilter;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.*;
import com.electricitybusiness.api.repository.*;
import com.electricitybusiness.api.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.mockito.Mock;
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
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
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
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private EntityMapper mapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private CustomUserDetailService customUserDetailService;

    @MockitoBean
    private TerminalService terminalService;
    @MockitoBean
    private CarService carService;
    @MockitoBean
    private AddressService addressService;

    private AuthController authController;

    private User testUser;
    private RefreshToken testRefreshTokenEntity;
    private String testAccessToken;
    private String testRefreshTokenJwt;

    @BeforeEach
    void setUp() {
        testRefreshTokenJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJpZFVzZXIiOiJ" +
                ThreadLocalRandom.current().nextLong(1, 10000) +
                "IiwiaXNzIjoib2xpbmVjaGFyZ2UuY29tIiwiZXhwIjoxNjk2ODY5NzcyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        testUser = User.builder()
                .idUser(ThreadLocalRandom.current().nextLong(1, 10000))
                .emailUser("test@example.com")
                .passwordUser("password")
                .role(UserRole.USER)
                .surnameUser("TestSurname")
                .firstName("TestFirstname")
                .pseudo("testpseudo")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .phone("0123456789")
                .build();

        testAccessToken = "un.vrai.faux.jwt.access.token";
        String newMockedAccessToken = "mocked_access_token";

        testRefreshTokenEntity = RefreshToken.builder()
                .idRefreshToken(testRefreshTokenJwt)
                .user(testUser)
                .build();

        when(jwtService.generateAccessToken(eq(testUser.getEmailUser()))).thenReturn(testAccessToken);
        when(jwtService.getRefreshTokenByToken(testRefreshTokenJwt)).thenReturn(Optional.of(testRefreshTokenEntity));
        when(jwtService.getRefreshSecretKey()).thenReturn("mock_refresh_secret_key");
        when(jwtService.isTokenValid(eq(testRefreshTokenJwt), eq("mock_refresh_secret_key"), eq(testUser))).thenReturn(true);
        when(jwtService.generateAccessToken(testUser.getUsername())).thenReturn(newMockedAccessToken);

        authController = new AuthController(
                jwtService,
                authenticationManager,
                customUserDetailService
        );
    }

    /**
     * Teste le cas de succès d'authentification avec des identifiants valides.
     */
    @Test
    void login_success() throws Exception {
        AuthController.AuthRequest authRequest = new AuthController.AuthRequest("test@example.com", "password");
        String expectedAccessToken = testAccessToken;
        String expectedRefreshTokenId = testRefreshTokenJwt;
        Authentication authenticatedToken = new UsernamePasswordAuthenticationToken(
                testUser,
                null,
                testUser.getAuthorities()
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticatedToken);
        when(customUserDetailService.loadUserByUsername(eq(testUser.getEmailUser())))
                .thenReturn(testUser);
        when(jwtService.generateAccessToken(eq(testUser.getEmailUser())))
                .thenReturn(expectedAccessToken);
        when(jwtService.generateRefreshTokenBdd(eq(testUser)))
                .thenReturn(testRefreshTokenEntity);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value(expectedAccessToken))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refreshToken=" + expectedRefreshTokenId)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("SameSite=None")));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(customUserDetailService).loadUserByUsername(eq(testUser.getEmailUser()));
        verify(jwtService).generateAccessToken(eq(testUser.getEmailUser()));
        verify(jwtService).generateRefreshTokenBdd(eq(testUser));
    }

    /**
     * Teste le cas d'échec d'authentification avec des identifiants invalides.
     */
    @Test
    void authenticate_Failure_InvalidCredentials() throws Exception {
        AuthController.AuthRequest authRequest = new AuthController.AuthRequest("wrong@example.com", "wrongpassword");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Email ou mot de passe incorrect"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(customUserDetailService);
        verifyNoInteractions(jwtService);
    }

    /**
     * Teste le rafraîchissement réussi du token d'accès.
     */
    @Test
    void refreshAccessToken_Success() throws Exception {
        // Mock des appels de service
        when(jwtService.getRefreshTokenByToken(testRefreshTokenJwt)).thenReturn(Optional.of(testRefreshTokenEntity));
        when(jwtService.isTokenValid(eq(testRefreshTokenJwt), anyString(), eq(testUser))).thenReturn(true);
        String newMockedAccessToken = "new_mocked_access_token_after_refresh";
        when(jwtService.generateAccessToken(testUser.getEmailUser())).thenReturn(newMockedAccessToken);

        Cookie refreshTokenCookie = new Cookie("refreshToken", testRefreshTokenJwt);

        // L'assertion doit maintenant vérifier le NOUVEL access token généré
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").value(newMockedAccessToken))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refreshToken=" + testRefreshTokenJwt)))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Secure")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("SameSite=None")));

        verify(jwtService).getRefreshTokenByToken(testRefreshTokenJwt);
        verify(jwtService).isTokenValid(eq(testRefreshTokenJwt), anyString(), eq(testUser));
        verify(jwtService).generateAccessToken(testUser.getEmailUser());
    }

    /**
     * Teste le cas où aucun token n'est fourni.
     */
    @Test
    void refreshAccessToken_Failure_MissingToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Le refresh token est manquant."));
        verifyNoInteractions(jwtService);
    }

    /**
     * Teste le cas où un token vide est fourni.
     */
    @Test
    void refreshAccessToken_Failure_EmptyToken() throws Exception {
        Cookie emptyRefreshTokenCookie = new Cookie("refreshAccessToken", ""); // Cookie avec valeur vide
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(emptyRefreshTokenCookie)) // Ajoutez le cookie vide
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Le refresh token est manquant."));
        verifyNoInteractions(jwtService); // Toujours aucune interaction avec jwtService
    }

    /**
     * Teste le cas où un token invalide est fourni.
     */
    @Test
    void refreshAccessToken_Failure_InvalidTokenPresent() throws Exception {

        String testRefreshTokenJwt = "some_valid_looking_jwt_string_for_test_purposes";
        Cookie refreshTokenCookie;

        // Arrange
        when(jwtService.getRefreshTokenByToken(testRefreshTokenJwt)).thenReturn(Optional.empty());

        refreshTokenCookie = new Cookie("refreshToken", testRefreshTokenJwt);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Le refresh token est invalide ou expiré."));

        verify(jwtService).getRefreshTokenByToken(testRefreshTokenJwt);
        verifyNoMoreInteractions(jwtService);
    }

    /**
     * Teste le cas où le token n'est pas trouvé dans la base de données.
     */
    @Test
    void refreshAccessToken_Failure_NotFoundInDB() throws Exception {
        when(jwtService.getRefreshTokenByToken(testRefreshTokenJwt)).thenReturn(Optional.empty());

        Cookie refreshTokenCookie = new Cookie("refreshToken", testRefreshTokenJwt);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Le refresh token est invalide ou expiré."));

        verify(jwtService).getRefreshTokenByToken(testRefreshTokenJwt);
        verifyNoMoreInteractions(jwtService);
    }

    /**
     * Teste le cas où le token est invalide lors de la validation.
     */
    @Test
    void refreshAccessToken_Failure_InvalidToken() throws Exception {
        when(jwtService.getRefreshTokenByToken(testRefreshTokenJwt)).thenReturn(Optional.of(testRefreshTokenEntity));
        when(jwtService.getRefreshSecretKey()).thenReturn("mock_refresh_secret_key");
        when(jwtService.isTokenValid(eq(testRefreshTokenJwt), anyString(), any(User.class))).thenReturn(false);

        Cookie refreshTokenCookie = new Cookie("refreshToken", testRefreshTokenJwt);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Le refresh token est invalide."));

        verify(jwtService).getRefreshTokenByToken(testRefreshTokenJwt);
        verify(jwtService).getRefreshSecretKey();
        verify(jwtService).isTokenValid(eq(testRefreshTokenJwt), anyString(), eq(testUser));
        verifyNoMoreInteractions(jwtService);
    }

    /**
     * Teste le cas où une erreur se produit lors de la validation du token.
     */
    @Test
    void refreshAccessToken_Failure_ProcessingError() throws Exception {
        when(jwtService.getRefreshTokenByToken(testRefreshTokenJwt)).thenReturn(Optional.of(testRefreshTokenEntity));
        when(jwtService.getRefreshSecretKey()).thenReturn("mock_refresh_secret_key");
        when(jwtService.isTokenValid(eq(testRefreshTokenJwt), anyString(), any(User.class))).thenThrow(new RuntimeException("Simulated error"));

        Cookie refreshTokenCookie = new Cookie("refreshToken", testRefreshTokenJwt);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Erreur lors du rafraîchissement du token."));

        verify(jwtService).getRefreshTokenByToken(testRefreshTokenJwt);
        verify(jwtService).isTokenValid(eq(testRefreshTokenJwt), anyString(), eq(testUser));
    }


    // --- Tests pour /api/auth/logout ---

    /**
     * Test de la déconnexion avec le token dans le cookie.
     */
    @Test
    void logout_Success_Cookie() throws Exception {
        doNothing().when(jwtService).deleteRefreshToken(testRefreshTokenJwt);

        Cookie refreshTokenCookie = new Cookie("refreshToken", testRefreshTokenJwt);

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Déconnexion réussie."))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refreshToken=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0")));

        verify(jwtService).deleteRefreshToken(testRefreshTokenJwt);
    }

    /**
     * Test de la déconnexion avec le token dans l'en-tête HTTP.
     */
    @Test
    void logout_Success_Header() throws Exception {
        doNothing().when(jwtService).deleteRefreshToken(testRefreshTokenJwt);

        mockMvc.perform(post("/api/auth/logout")
                        .header("X-Refresh-Token", testRefreshTokenJwt))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Déconnexion réussie."))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("refreshToken=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, org.hamcrest.Matchers.containsString("Max-Age=0"))); // Vérifie que le cookie est expiré

        verify(jwtService).deleteRefreshToken(testRefreshTokenJwt);
    }

    /**
     * Test de la déconnexion échouant en l'absence de token.
     */
    @Test
    void logout_Failure_MissingToken() throws Exception {
        // Exécute la requête de déconnexion sans cookie ni header
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Le refresh token est manquant."));

        // Vérifie qu'aucune interaction avec le service JWT n'a eu lieu
        verifyNoInteractions(jwtService);
    }

    /**
     * Test de la déconnexion échouant en cas d'erreur interne lors de la suppression du token.
     */
    @Test
    void logout_Failure_InternalError() throws Exception {
        // Simule une exception lors de la suppression du refresh token
        doThrow(new RuntimeException("DB error during deletion")).when(jwtService).deleteRefreshToken(testRefreshTokenJwt);

        Cookie refreshTokenCookie = new Cookie("refreshToken", testRefreshTokenJwt);

        // Exécute la requête de déconnexion
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(refreshTokenCookie))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Une erreur est survenue lors de la déconnexion."));

        // Vérifie que la méthode de suppression a été appelée malgré l'erreur
        verify(jwtService).deleteRefreshToken(testRefreshTokenJwt);
    }

    // --- Tests unitaires pour la méthode extractRefreshTokenFromCookie ---

    /**
     * Teste la méthode extractRefreshTokenFromCookie dans différents scénarios.
     */
    @Test
    void extractRefreshTokenFromCookie_whenNoCookiesExist_shouldReturnNull() throws Exception {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        // Act
        Method method = AuthController.class.getDeclaredMethod("extractRefreshTokenFromCookie", HttpServletRequest.class, String.class);
        method.setAccessible(true);
        String refreshToken = (String) method.invoke(authController, request, "testSource");

        // Assert
        assertNull(refreshToken, "Le refresh token devrait être null quand aucun cookie n'existe.");
    }

    /**
     * Teste le cas où le cookie "refreshToken" n'est pas présent.
     */
    @Test
    void extractRefreshTokenFromCookie_whenCookieNotFound_shouldReturnNull() throws Exception {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Cookie[] cookies = {
                new Cookie("otherCookie", "value"),
                new Cookie("another", "value2")
        };
        when(request.getCookies()).thenReturn(cookies);

        // Act
        Method method = AuthController.class.getDeclaredMethod("extractRefreshTokenFromCookie", HttpServletRequest.class, String.class);
        method.setAccessible(true);
        String refreshToken = (String) method.invoke(authController, request, "testSource");

        // Assert
        assertNull(refreshToken, "Le refresh token devrait être null quand le cookie 'refreshToken' n'est pas trouvé.");
    }

    /**
     * Teste le cas où le cookie "refreshToken" est présent et retourne sa valeur.
     */
    @Test
    void extractRefreshTokenFromCookie_whenCookieFound_shouldReturnValue() throws Exception {
        // Arrange
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String expectedRefreshTokenValue = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.token.valide";
        Cookie[] cookies = {
                new Cookie("otherCookie", "value1"),
                new Cookie("refreshToken", expectedRefreshTokenValue),
                new Cookie("anotherCookie", "value2")
        };
        when(request.getCookies()).thenReturn(cookies);

        // Act
        Method method = AuthController.class.getDeclaredMethod("extractRefreshTokenFromCookie", HttpServletRequest.class, String.class);
        method.setAccessible(true);
        String refreshToken = (String) method.invoke(authController, request, "testSource");

        // Assert
        assertEquals(expectedRefreshTokenValue, refreshToken, "Le refresh token devrait retourner sa valeur.");
    }

}
