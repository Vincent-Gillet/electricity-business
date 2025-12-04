package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.user.UserDTO;
import com.electricitybusiness.api.exception.InvalidTokenException;
import com.electricitybusiness.api.exception.ResourceNotFoundException;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.RefreshToken;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.model.UserRole;
import com.electricitybusiness.api.repository.RefreshTokenRepository;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.security.Key;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {
    private static final String TEST_ACCESS_SECRET_KEY = "dGVzdC1hY2Nlc3Mta2V5LWZvci11bml0LXRlc3RzLW11c3QtYmUtYXQtbGVhc3QtMjU2LWJpdHMtbG9uZy1IUzI1Ng==";
    private static final String TEST_REFRESH_SECRET_KEY = "dGVzdC1yZWZyZXNoLXNlY3JldC1rZXktZm9yLXVuaXQtcmVmcmVzaC10ZXN0cy1tdXN0LWJlLWF0LWxlYXN0LTI1Ni1iaXRzLWxvbmctSEcyNTY=";

    @Mock
    private UserRepository userRepository;

    @Mock
    private EntityMapper entityMapper;

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private JwtService jwtService;

    private String VALID_ACCESS_TOKEN;
    private String VALID_REFRESH_TOKEN;
    private String EXPIRED_ACCESS_TOKEN;
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

    private final RefreshToken MOCK_REFRESH_TOKEN = new RefreshToken("refresh-token-123", MOCK_USER);

    @BeforeEach
    void setUp() {
        // Configuration des clés secrètes
        ReflectionTestUtils.setField(jwtService, "accessSecretKey", TEST_ACCESS_SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "refreshSecretKey", TEST_REFRESH_SECRET_KEY);

        // Génération des tokens de test
        Key accessSigningKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_ACCESS_SECRET_KEY));
        Key refreshSigningKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_REFRESH_SECRET_KEY));

        // Token d'accès valide (10 minutes)
        this.VALID_ACCESS_TOKEN = Jwts.builder()
                .setSubject("test@email.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .signWith(accessSigningKey, SignatureAlgorithm.HS256)
                .compact();

        // Token de rafraîchissement valide (7 jours)
        this.VALID_REFRESH_TOKEN = Jwts.builder()
                .setSubject("test@email.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
                .signWith(refreshSigningKey, SignatureAlgorithm.HS256)
                .compact();

        // Token d'accès expiré
        this.EXPIRED_ACCESS_TOKEN = Jwts.builder()
                .setSubject("test@email.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // Il y a 1 heure
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 30)) // Expiré il y a 30 min
                .signWith(accessSigningKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Tests pour extractUsername
    @Test
    void validateAccessToken_ValidToken_ReturnsTrue() {
        boolean isValid = jwtService.validateAccessToken(VALID_ACCESS_TOKEN);
        assertTrue(isValid);
    }

    @Test
    void validateAccessToken_ExpiredToken_ReturnsFalse() {
        boolean isValid = jwtService.validateAccessToken(EXPIRED_ACCESS_TOKEN);
        assertFalse(isValid);
    }

    @Test
    void validateAccessToken_InvalidToken_ReturnsFalse() {
        boolean isValid = jwtService.validateAccessToken(INVALID_TOKEN);
        assertFalse(isValid);
    }

    //  Tests pour generateAccessToken
    @Test
    void generateAccessToken_ValidUsername_ReturnsNonEmptyToken() {
        String token = jwtService.generateAccessToken("test@email.com");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateAccessToken_ValidUsername_ContainsCorrectSubject() {
        String token = jwtService.generateAccessToken("test@email.com");
        String email = jwtService.extractUsername(token, TEST_ACCESS_SECRET_KEY);
        assertEquals("test@email.com", email);
    }

    // Tests pour generateRefreshToken
    @Test
    void generateRefreshToken_ValidUsername_ReturnsNonEmptyToken() {
        String token = jwtService.generateRefreshToken("test@email.com");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateRefreshToken_ValidUsername_ContainsCorrectSubject() {
        String token = jwtService.generateRefreshToken("test@email.com");
        String email = jwtService.extractUsername(token, TEST_REFRESH_SECRET_KEY);
        assertEquals("test@email.com", email);
    }

    // Tests pour generateRefreshTokenBdd
    @Test
    void generateRefreshTokenBdd_ValidUser_ReturnsSavedToken() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(MOCK_REFRESH_TOKEN);

        RefreshToken result = jwtService.generateRefreshTokenBdd(MOCK_USER);

        assertNotNull(result);
        assertEquals(MOCK_REFRESH_TOKEN.getIdRefreshToken(), result.getIdRefreshToken());
        assertEquals(MOCK_USER, result.getUser());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    // Tests pour extractUsername
    @Test
    void extractUsername_ValidToken_ReturnsEmail() {
        String email = jwtService.extractUsername(VALID_ACCESS_TOKEN, TEST_ACCESS_SECRET_KEY);
        assertEquals("test@email.com", email);
    }

    @Test
    void extractUsername_ExpiredToken_ThrowsInvalidTokenException() {
        assertThrows(InvalidTokenException.class, () ->
                jwtService.extractUsername(EXPIRED_ACCESS_TOKEN, TEST_ACCESS_SECRET_KEY));
    }

    @Test
    void extractUsername_InvalidToken_ThrowsInvalidTokenException() {
        assertThrows(InvalidTokenException.class, () ->
                jwtService.extractUsername(INVALID_TOKEN, TEST_ACCESS_SECRET_KEY));
    }

    // Tests pour getUserByAccessToken
    @Test
    void getUserByAccessToken_ValidTokenAndUserExists_ReturnsUser() {
        when(userService.getUserByEmail("test@email.com")).thenReturn(MOCK_USER);

        User user = jwtService.getUserByAccessToken(VALID_ACCESS_TOKEN);

        assertEquals(MOCK_USER, user);
        verify(userService, times(1)).getUserByEmail("test@email.com");
    }

    @Test
    void getUserByAccessToken_ValidTokenButUserNotFound_ThrowsResourceNotFoundException() {
        when(userService.getUserByEmail(anyString())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () ->
                jwtService.getUserByAccessToken(VALID_ACCESS_TOKEN));
    }

    @Test
    void getUserByAccessToken_ExpiredToken_ThrowsInvalidTokenException() {
        assertThrows(InvalidTokenException.class, () ->
                jwtService.getUserByAccessToken(EXPIRED_ACCESS_TOKEN));
        verify(userService, never()).getUserByEmail(anyString());
    }

    @Test
    void getUserByAccessToken_InvalidToken_ThrowsInvalidTokenException() {
        assertThrows(InvalidTokenException.class, () ->
                jwtService.getUserByAccessToken(INVALID_TOKEN));
        verify(userService, never()).getUserByEmail(anyString());
    }

    // Tests pour getUserDTOByAccessToken
    @Test
    void getUserDTOByAccessToken_ValidTokenAndUserExists_ReturnsUserDTO() {
        when(userService.getUserByEmail("test@email.com")).thenReturn(MOCK_USER);
        when(entityMapper.toDTO(MOCK_USER)).thenReturn(MOCK_USER_DTO);

        Optional<UserDTO> result = jwtService.getUserDTOByAccessToken(VALID_ACCESS_TOKEN);

        assertTrue(result.isPresent());
        assertEquals(MOCK_USER_DTO, result.get());
    }

    @Test
    void getUserDTOByAccessToken_ValidTokenButUserNotFound_ReturnsEmptyOptional() {
        when(userService.getUserByEmail(anyString())).thenReturn(null);

        Optional<UserDTO> result = jwtService.getUserDTOByAccessToken(VALID_ACCESS_TOKEN);

        assertTrue(result.isEmpty());
    }

    @Test
    void getUserDTOByAccessToken_ExpiredToken_ReturnsEmptyOptional() {
        Optional<UserDTO> result = jwtService.getUserDTOByAccessToken(EXPIRED_ACCESS_TOKEN);

        assertTrue(result.isEmpty());
    }

    @Test
    void getUserDTOByAccessToken_InvalidToken_ReturnsEmptyOptional() {
        Optional<UserDTO> result = jwtService.getUserDTOByAccessToken(INVALID_TOKEN);

        assertTrue(result.isEmpty());
    }

    // Tests pour getRefreshTokenByToken
    @Test
    void getRefreshTokenByToken_TokenExists_ReturnsRefreshToken() {
        when(refreshTokenRepository.findById("refresh-token-123")).thenReturn(Optional.of(MOCK_REFRESH_TOKEN));

        Optional<RefreshToken> result = jwtService.getRefreshTokenByToken("refresh-token-123");

        assertTrue(result.isPresent());
        assertEquals(MOCK_REFRESH_TOKEN, result.get());
        verify(refreshTokenRepository, times(1)).findById("refresh-token-123");
    }

    @Test
    void getRefreshTokenByToken_TokenNotFound_ReturnsEmptyOptional() {
        when(refreshTokenRepository.findById("non-existent-token")).thenReturn(Optional.empty());

        Optional<RefreshToken> result = jwtService.getRefreshTokenByToken("non-existent-token");

        assertTrue(result.isEmpty());
        verify(refreshTokenRepository, times(1)).findById("non-existent-token");
    }

    // Tests pour deleteRefreshToken
    @Test
    void deleteRefreshToken_ValidToken_DeletesToken() {
        jwtService.deleteRefreshToken("refresh-token-123");
        verify(refreshTokenRepository, times(1)).deleteById("refresh-token-123");
    }

    //  Tests pour isTokenValid
    @Test
    void isTokenValid_ValidTokenAndMatchingUser_ReturnsTrue() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "test@email.com", "", Collections.emptyList());

        boolean isValid = jwtService.isTokenValid(VALID_ACCESS_TOKEN, TEST_ACCESS_SECRET_KEY, userDetails);

        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ValidTokenButDifferentUser_ReturnsFalse() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "different@email.com", "", Collections.emptyList());

        boolean isValid = jwtService.isTokenValid(VALID_ACCESS_TOKEN, TEST_ACCESS_SECRET_KEY, userDetails);

        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "test@email.com", "", Collections.emptyList());

        boolean isValid = jwtService.isTokenValid(EXPIRED_ACCESS_TOKEN, TEST_ACCESS_SECRET_KEY, userDetails);

        assertFalse(isValid);
    }

    @Test
    void isTokenExpired_ExpiredToken_ReturnsTrue() throws Exception {
        Method isTokenExpiredMethod = JwtService.class.getDeclaredMethod("isTokenExpired", String.class, String.class);
        isTokenExpiredMethod.setAccessible(true);
        boolean isExpired = (boolean) isTokenExpiredMethod.invoke(jwtService, EXPIRED_ACCESS_TOKEN, TEST_ACCESS_SECRET_KEY);

        assertTrue(isExpired);
    }
}
