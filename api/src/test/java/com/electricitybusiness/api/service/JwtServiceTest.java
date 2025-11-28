package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.user.UserDTO;
import com.electricitybusiness.api.exception.InvalidTokenException;
import com.electricitybusiness.api.exception.ResourceNotFoundException;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.model.UserRole;
import com.electricitybusiness.api.repository.UserRepository;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
    private static final String TEST_SECRET_KEY = "dGVzdC1hY2Nlc3Mta2V5LWZvci11bml0LXRlc3RzLW11c3QtYmUtYXQtbGVhc3QtMjU2LWJpdHMtbG9uZy1IUzI1Ng==";

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private JwtService jwtService;

    private String VALID_TOKEN;
    private String EXPIRED_TOKEN;
    private final String INVALID_TOKEN = "invalid.token.here";
    private final User MOCK_USER = new User(
            1L, "Dupont", "Martin", "martin123",
            "test@email.com", "password",
            UserRole.USER, LocalDate.of(2003, 1, 1),
            "0123456789", "FR1111111111111111111111153",
            false, null, null, null, null, null
    );
    private final UserDTO MOCK_USER_DTO = new UserDTO(
            "Dupont", "Martin", "martin123",
            "test@email.com", UserRole.USER,
            LocalDate.of(2003, 1, 1), "0123456789",
            "FR1111111111111111111111153", false
    );

    @BeforeEach
    void setUp() {
        // 1. Définir la clé secrète
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET_KEY);

        // --- NOUVEAU: Décoder la clé de Base64 pour la signature dans les tests ---
        Key signingKeyForTest = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET_KEY));

        // 2. Générer un token VALIDE avec la clé correctement décodée
        this.VALID_TOKEN = Jwts.builder()
                .setSubject("test@email.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10)) // 10 minutes
                .signWith(signingKeyForTest, SignatureAlgorithm.HS256) // <-- Utilisez la clé décodée
                .compact();

        // 3. Générer un token EXPIRÉ avec la clé correctement décodée
        this.EXPIRED_TOKEN = Jwts.builder()
                .setSubject("test@email.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // Il y a 1 heure
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 30)) // Expiré il y a 30 min
                .signWith(signingKeyForTest, SignatureAlgorithm.HS256) // <-- Utilisez la clé décodée
                .compact();
    }

    // ========== Tests pour extractUsername ==========
    @Test
    void extractUsername_ValidToken_ReturnsEmail() {
        String email = jwtService.extractUsername(VALID_TOKEN);
        assertEquals("test@email.com", email);
    }

    @Test
    void extractUsername_ExpiredToken_ThrowsInvalidTokenException() {
        assertThrows(InvalidTokenException.class, () -> jwtService.extractUsername(EXPIRED_TOKEN));
    }

    @Test
    void extractUsername_InvalidToken_ThrowsInvalidTokenException() {
        assertThrows(InvalidTokenException.class, () -> jwtService.extractUsername(INVALID_TOKEN));
    }

    // ========== Tests pour getUserByAccessToken ==========
    @Test
    void getUserByAccessToken_ValidTokenAndUserExists_ReturnsUser() {
        when(userService.getUserByEmail("test@email.com")).thenReturn(MOCK_USER);

        User user = jwtService.getUserByAccessToken(VALID_TOKEN);
        assertEquals(MOCK_USER, user);
        verify(userService, times(1)).getUserByEmail("test@email.com");
    }

    @Test
    void getUserByAccessToken_ValidTokenButUserNotFound_ThrowsResourceNotFoundException() {
        when(userService.getUserByEmail(anyString())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> jwtService.getUserByAccessToken(VALID_TOKEN));
    }

    @Test
    void getUserByAccessToken_ExpiredToken_ThrowsInvalidTokenException() {
        assertThrows(InvalidTokenException.class, () -> jwtService.getUserByAccessToken(EXPIRED_TOKEN));
        verify(userService, never()).getUserByEmail(anyString()); // Vérifie que getUserByEmail n'est pas appelé
    }

    @Test
    void getUserByAccessToken_InvalidToken_ThrowsInvalidTokenException() {
        assertThrows(InvalidTokenException.class, () -> jwtService.getUserByAccessToken(INVALID_TOKEN));
        verify(userService, never()).getUserByEmail(anyString());
    }

    // ========== Tests pour getUserDTOByAccessToken ==========
    @Test
    void getUserDTOByAccessToken_ValidTokenAndUserExists_ReturnsUserDTO() {
        when(userService.getUserByEmail("test@email.com")).thenReturn(MOCK_USER);
        when(entityMapper.toDTO(MOCK_USER)).thenReturn(MOCK_USER_DTO);

        Optional<UserDTO> result = jwtService.getUserDTOByAccessToken(VALID_TOKEN);
        assertTrue(result.isPresent());
        assertEquals(MOCK_USER_DTO, result.get());
    }

    @Test
    void getUserDTOByAccessToken_ValidTokenButUserNotFound_ReturnsEmptyOptional() {
        when(userService.getUserByEmail(anyString())).thenReturn(null);

        Optional<UserDTO> result = jwtService.getUserDTOByAccessToken(VALID_TOKEN);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserDTOByAccessToken_ExpiredToken_ReturnsEmptyOptional() {
        Optional<UserDTO> result = jwtService.getUserDTOByAccessToken(EXPIRED_TOKEN);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserDTOByAccessToken_InvalidToken_ReturnsEmptyOptional() {
        Optional<UserDTO> result = jwtService.getUserDTOByAccessToken(INVALID_TOKEN);
        assertTrue(result.isEmpty());
    }

    // ========== Tests pour generateAccessToken ==========
    @Test
    void generateAccessToken_ValidUsername_ReturnsNonEmptyToken() {
        String token = jwtService.generateAccessToken("test@email.com");
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Vérifie que le token peut être parsé et contient le bon sujet
        String email = jwtService.extractUsername(token);
        assertEquals("test@email.com", email);
    }
}
