package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.user.UserDTO;
import com.electricitybusiness.api.exception.InvalidTokenException;
import com.electricitybusiness.api.exception.ResourceNotFoundException;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.RefreshToken;
import com.electricitybusiness.api.repository.RefreshTokenRepository;
import com.electricitybusiness.api.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import com.electricitybusiness.api.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Getter
public class JwtService {
    @Value("${jwt.secret-key-access-token}")
    private String accessSecretKey;

    @Value("${jwt.secret-key-refresh-token}")
    private String refreshSecretKey;

    private final RefreshTokenRepository refreshTokenRepository;
    private final EntityMapper entityMapper;
    private final UserRepository userRepository;
    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    // Durées d'expiration
    private static final long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 10; // 10 minutes
    private static final long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 7 jours

    /**
     * Valide un access token.
     * @param token Le token à valider
     * @return true si le token est valide, false sinon
     */
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(accessSecretKey))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Génère un access token.
     * @param username Le nom d'utilisateur (email)
     * @return L'access token généré
     */
    public String generateAccessToken(String username) {
        return buildToken(username, accessSecretKey, ACCESS_TOKEN_EXPIRATION_TIME);
    }

    /**
     * Génère un refresh token.
     * @param username Le nom d'utilisateur (email)
     * @return Le refresh token généré
     */
    public String generateRefreshToken(String username) {
        return buildToken(username, refreshSecretKey, REFRESH_TOKEN_EXPIRATION_TIME);
    }

    /**
     * Génère un refresh token et le sauvegarde en base de données.
     * @param user L'utilisateur pour lequel générer le refresh token
     * @return Le RefreshToken sauvegardé en base de données
     */
    public RefreshToken generateRefreshTokenBdd(User user) {
        String token = generateRefreshToken(user.getEmailUser());
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setIdRefreshToken(token);
        refreshToken.setUser(user);
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Méthode utilitaire pour construire un token JWT.
     * @param username Le nom d'utilisateur (email)
     * @param secretKey La clé secrète utilisée pour signer le token
     * @param expirationTime Le temps d'expiration en millisecondes
     * @return Le token JWT généré
     */
    private String buildToken(String username, String secretKey, long expirationTime) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(secretKey), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Méthode utilitaire pour obtenir une clé utilisable par la bibliothèque JJWT.
     * Elle convertit la chaîne `secretKey` (en base64) en un objet `Key`.
     * @param secretKey La clé secrète en base64
     * @return La clé de signature
     */
    private Key getSigningKey(String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrait toutes les réclamations (claims) d'un token JWT.
     * @param token Le token JWT
     * @param secretKey La clé secrète utilisée pour signer le token
     * @return Les réclamations extraites du token
     * @throws InvalidTokenException Si le token est invalide ou expiré
     */
    public Claims extractAllClaims(String token, String secretKey) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(secretKey))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expiré: {}", e.getMessage());
            throw new InvalidTokenException("Token JWT expiré");
        } catch (MalformedJwtException | SignatureException | IllegalArgumentException e) {
            logger.warn("Token JWT invalide: {}", e.getMessage());
            throw new InvalidTokenException("Token JWT invalide");
        }
    }

    /**
     * Extrait une réclamation spécifique (claim) d'un token JWT.
     * @param token Le token JWT
     * @param secretKey La clé secrète utilisée pour signer le token
     * @param claimsResolver Une fonction pour extraire la réclamation souhaitée
     * @param <T> Le type de la réclamation à extraire
     * @return La réclamation extraite du token
     */
    public <T> T extractClaim(String token, String secretKey, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, secretKey);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait le nom d'utilisateur (email) d'un token JWT.
     * @param token Le token JWT
     * @param secretKey La clé secrète utilisée pour signer le token
     * @return Le nom d'utilisateur extrait du token
     */
    public String extractUsername(String token, String secretKey) {
        return extractClaim(token, secretKey, Claims::getSubject);
    }

    /**
     * Vérifie si un token est valide pour un utilisateur donné.
     * @param token Le token JWT à vérifier
     * @param secretKey La clé secrète utilisée pour signer le token
     * @param userDetails Les détails de l'utilisateur à vérifier
     * @return true si le token est valide, false sinon
     */
    public boolean isTokenValid(String token, String secretKey, UserDetails userDetails) {
        try {
            final String username = extractUsername(token, secretKey);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token, secretKey));
        } catch (InvalidTokenException e) {
            return false;
        }
    }

    /**
     * Extrait la date d'expiration d'un token JWT.
     * @param token Le token JWT
     * @param secretKey La clé secrète utilisée pour signer le token
     * @return La date d'expiration du token
     */
    protected Date extractExpiration(String token, String secretKey) throws InvalidTokenException {
        try {
            final Claims claims = extractAllClaims(token, secretKey);
            return claims.getExpiration();
        } catch (JwtException e) {
            throw new InvalidTokenException("Token JWT expiré");
        }
    }

    /**
     * Vérifie si un token est expiré.
     * @param token Le token JWT à vérifier
     * @param secretKey La clé secrète utilisée pour signer le token
     * @return true si le token est expiré, false sinon
     */
    boolean isTokenExpired(String token, String secretKey) {
        try {
            final Date expirationDate = extractExpiration(token, secretKey);
            return expirationDate.before(new Date());
        } catch (InvalidTokenException e) {
            return true;
        }
    }

    /**
     * Récupère un refresh token depuis la base de données par son identifiant.
     * @param id L'identifiant du refresh token
     * @return Un Optional contenant le RefreshToken si trouvé, sinon vide
     */
    public Optional<RefreshToken> getRefreshTokenByToken(String id) {
        return refreshTokenRepository.findById(id);
    }

    /**
     * Supprime un refresh token de la base de données.
     * @param token Le token à supprimer
     */
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteById(token);
    }

    /**
     * Récupère l'utilisateur associé à un access token.
     * @param accessToken Le token d'accès JWT
     * @return L'utilisateur correspondant au token
     * @throws InvalidTokenException Si le token est invalide ou expiré
     * @throws ResourceNotFoundException Si l'utilisateur n'est pas trouvé
     */
    public User getUserByAccessToken(String accessToken) {
        try {
            String email = extractUsername(accessToken, accessSecretKey);
            logger.debug("Recherche de l'utilisateur pour l'email: {}", email);

            User user = userService.getUserByEmail(email);
            if (user == null) {
                logger.warn("Utilisateur non trouvé pour l'email: {}", email);
                throw new ResourceNotFoundException("Utilisateur non trouvé pour l'email: " + email);
            }
            return user;
        } catch (ExpiredJwtException e) {
            logger.warn("Token expiré: {}", e.getMessage());
            throw new InvalidTokenException("Token expiré");
        } catch (JwtException e) {
            logger.warn("Token invalide: {}", e.getMessage());
            throw new InvalidTokenException("Token invalide");
        }
    }

    /**
     * Récupère le UserDTO associé à un access token.
     * @param accessToken Le token d'accès JWT
     * @return Un Optional contenant le UserDTO si trouvé, sinon vide
     */
    public Optional<UserDTO> getUserDTOByAccessToken(String accessToken) {
        logger.info("Récupération du UserDTO à partir du token");
        try {
            User user = getUserByAccessToken(accessToken);
            UserDTO userDTO = entityMapper.toDTO(user);
            logger.debug("UserDTO généré avec succès pour l'utilisateur: {}", user.getEmailUser());
            return Optional.of(userDTO);
        } catch (InvalidTokenException | ResourceNotFoundException e) {
            logger.warn("Échec de la récupération du UserDTO: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
