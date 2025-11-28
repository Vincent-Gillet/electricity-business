package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.user.UserDTO;
import com.electricitybusiness.api.model.RefreshToken;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.model.UserRole;
import com.electricitybusiness.api.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshServiceTest {

    @Mock // Mock de la dépendance du service
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks // Injecte les mocks dans le service que nous testons
    private RefreshTokenService refreshTokenService;

    // Clé secrète de test (Base64 encodée) - doit être la même que celle utilisée en prod pour le décodage si vous testez le décodage.
    // Pour les tests, une clé factice mais valide est suffisante.
    // Exemple d'une clé de 32 octets (256 bits) encodée en Base64:
    private final String TEST_SECRET_KEY = "bXlzZWNyZXRrZXlmb3JyZWZyZXNodG9rZW5qd3RfMDEyMzQ1Njc4OQ==";

    // Utilisateur de test
    private User testUser;
    private String testUsername = "john.doe@example.com";
    private String testRefreshTokenId;

    @BeforeEach
    void setUp() {
        // Initialise la clé secrète du service via ReflectionTestUtils car c'est un champ @Value
        ReflectionTestUtils.setField(refreshTokenService, "secretKey", TEST_SECRET_KEY);

        // Crée un utilisateur de test
        testUser = User.builder()
                .idUser(1L)
                .emailUser(testUsername)
                .firstName("John")
                .surnameUser("Doe")
                .role(UserRole.USER)
                .build();

        // Génère un refresh token pour les tests nécessitant un token valide
        testRefreshTokenId = refreshTokenService.generateRefreshToken(testUsername);
    }

    private Key getSigningKeyForTest() {
        byte[] keyBytes = Decoders.BASE64.decode(TEST_SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Test
    void shouldGenerateRefreshToken() {
        // Exécution
        String token = refreshTokenService.generateRefreshToken(testUsername);

        // Vérification
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Vérifiez que le token peut être décodé et contient le bon sujet
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKeyForTest())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertEquals(testUsername, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new Date())); // Devrait expirer dans le futur
    }

    @Test
    void shouldGenerateRefreshTokenBddAndSave() {
        // Préparation du mock
        RefreshToken expectedRefreshToken = new RefreshToken();
        expectedRefreshToken.setIdRefreshToken(testRefreshTokenId); // Le token réel serait généré ici
        expectedRefreshToken.setUser(testUser);

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(expectedRefreshToken);

        // Exécution
        RefreshToken result = refreshTokenService.generateRefreshTokenBdd(testUser);

        // Vérification
        assertNotNull(result);
        assertEquals(testRefreshTokenId, result.getIdRefreshToken());
        assertEquals(testUser, result.getUser());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // Exécution
        String extractedUsername = refreshTokenService.extractUsername(testRefreshTokenId);

        // Vérification
        assertEquals(testUsername, extractedUsername);
    }

    @Test
    void shouldExtractExpirationFromToken() {
        // Exécution
        Date expirationDate = refreshTokenService.extractClaim(testRefreshTokenId, Claims::getExpiration);

        // Vérification (simple vérification que la date n'est pas nulle et est dans le futur pour ce token de test)
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidTokenAndUser() {
        // Préparation du mock UserDetails
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(testUsername);

        // Exécution
        boolean isValid = refreshTokenService.isTokenValid(testRefreshTokenId, userDetails);

        // Vérification
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidUsername() {
        // Préparation du mock UserDetails avec un username différent
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("wrong.user@example.com");

        // Exécution
        boolean isValid = refreshTokenService.isTokenValid(testRefreshTokenId, userDetails);

        // Vérification
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() {
        // Générer un token expiré
        String expiredToken = Jwts.builder()
                .setSubject(testUsername)
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 8)) // Il y a 2 heures
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)) // Expired il y a 8 jours
                .signWith(getSigningKeyForTest(), SignatureAlgorithm.HS256)
                .compact();

        // Préparation du mock UserDetails
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(testUsername);

        // Exécution
        boolean isValid = refreshTokenService.isTokenValid(expiredToken, userDetails);

        // Vérification
        assertFalse(isValid);
    }

    @Test
    void shouldGetRefreshTokenByTokenWhenFound() {
        // Préparation du mock
        RefreshToken expectedRefreshToken = new RefreshToken();
        expectedRefreshToken.setIdRefreshToken(testRefreshTokenId);
        expectedRefreshToken.setUser(testUser);

        when(refreshTokenRepository.findById(testRefreshTokenId)).thenReturn(Optional.of(expectedRefreshToken));

        // Exécution
        Optional<RefreshToken> result = refreshTokenService.getRefreshTokenByToken(testRefreshTokenId);

        // Vérification
        assertTrue(result.isPresent());
        assertEquals(expectedRefreshToken, result.get());
        verify(refreshTokenRepository, times(1)).findById(testRefreshTokenId);
    }

    @Test
    void shouldNotGetRefreshTokenByTokenWhenNotFound() {
        // Préparation du mock
        when(refreshTokenRepository.findById("nonExistentToken")).thenReturn(Optional.empty());

        // Exécution
        Optional<RefreshToken> result = refreshTokenService.getRefreshTokenByToken("nonExistentToken");

        // Vérification
        assertFalse(result.isPresent());
        verify(refreshTokenRepository, times(1)).findById("nonExistentToken");
    }

    @Test
    void shouldGetUserByRefreshTokenWhenFound() {
        // Préparation du mock
        RefreshToken foundRefreshToken = new RefreshToken();
        foundRefreshToken.setIdRefreshToken(testRefreshTokenId);
        foundRefreshToken.setUser(testUser);

        when(refreshTokenRepository.findByIdRefreshToken(testRefreshTokenId)).thenReturn(Optional.of(foundRefreshToken));

        // Exécution
        Optional<UserDTO> result = refreshTokenService.getUserByRefreshToken(testRefreshTokenId);

        // Vérification
        assertTrue(result.isPresent());
        UserDTO userDTO = result.get();
        assertEquals(testUser.getSurnameUser(), userDTO.getSurnameUser());
        assertEquals(testUser.getFirstName(), userDTO.getFirstName());
        assertEquals(testUser.getEmailUser(), userDTO.getEmailUser());
        assertEquals(testUser.getRole(), userDTO.getRole());
        // Vérifiez d'autres champs de UserDTO si nécessaire
        verify(refreshTokenRepository, times(1)).findByIdRefreshToken(testRefreshTokenId);
    }

    @Test
    void shouldNotGetUserByRefreshTokenWhenNotFound() {
        // Préparation du mock
        when(refreshTokenRepository.findByIdRefreshToken("nonExistentToken")).thenReturn(Optional.empty());

        // Exécution
        Optional<UserDTO> result = refreshTokenService.getUserByRefreshToken("nonExistentToken");

        // Vérification
        assertFalse(result.isPresent());
        verify(refreshTokenRepository, times(1)).findByIdRefreshToken("nonExistentToken");
    }

    @Test
    void shouldDeleteRefreshToken() {
        // Exécution
        refreshTokenService.deleteRefreshToken(testRefreshTokenId);

        // Vérification
        verify(refreshTokenRepository, times(1)).deleteById(testRefreshTokenId);
    }
}
