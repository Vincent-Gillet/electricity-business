package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.user.UserDTO;
import com.electricitybusiness.api.exception.InvalidTokenException;
import com.electricitybusiness.api.exception.ResourceNotFoundException;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${jwt.secret-key-access-token}")
    private String secretKey;

    private final EntityMapper entityMapper;
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private static final long EXPIRATION_TIME = 1000 * 60 * 10; // 10 minutes
/*
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours juste pour les tests
*/

    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)

                .setIssuedAt(new Date(System.currentTimeMillis()))

                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))

                .signWith(getSigningKey(), SignatureAlgorithm.HS256)

                .compact();
    }

    /**
     * Méthode utilitaire pour obtenir une clé utilisable par la bibliothèque JJWT.
     * Elle convertit la chaîne `SECRET_KEY` (en base64) en un objet `Key`.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Méthode pour extraire **tous les "claims"** (les données contenues dans le token).
// Les claims sont typiquement : username, date d’expiration, rôles, etc.
    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()) // Utilise la clé correctement décodée en Base64
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token JWT expiré: {}", e.getMessage());
            throw new InvalidTokenException("Token JWT expiré"); // Encapsule dans InvalidTokenException
        } catch (MalformedJwtException | SignatureException | IllegalArgumentException e) {
            logger.warn("Token JWT invalide: {}", e.getMessage());
            throw new InvalidTokenException("Token JWT invalide"); // Encapsule dans InvalidTokenException
        }
    }

    /**
     * Méthode générique pour extraire **un seul claim** depuis un token JWT.
     * Elle utilise une fonction (`claimsResolver`) pour dire **quel champ** on veut extraire.
     * Par exemple : `Claims::getSubject` pour le username, `Claims::getExpiration` pour la date.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        // On applique la fonction passée en paramètre à l'objet Claims
        return claimsResolver.apply(claims);
    }

    /**
     * Extrait le **username** contenu dans le token.
     * En JWT, il est stocké dans le champ `sub` (subject).
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait la **date d'expiration** du token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Vérifie si le token est **expiré**.
     * On compare la date d’expiration du token à la date actuelle.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Vérifie si un token est **valide** :
     * - Le nom d’utilisateur dans le token correspond à celui de l'utilisateur authentifié.
     * - Le token n’est pas expiré.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private final UserService userService;

/*    public Optional<UserDTO> getUserDTOByAccessToken(String accessToken) {
        try {
            String username = extractUsername(accessToken);

            if (username != null) {
                Optional<User> user = userService.findByUserEmail(username);

                return user.map(u -> {
                    return new UserDTO(
                            u.getSurnameUser(),
                            u.getFirstName(),
                            u.getUsername(),
                            u.getEmailUser(),
                            u.getRole(),
                            u.getDateOfBirth(),
                            u.getPhone(),
                            u.getIban(),
                            u.getBanished()
                    );
                });
            }
            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }*/

/*    public Optional<User> getUserByAccessToken(String accessToken) {
        try {
            String username = extractUsername(accessToken);

            if (username != null) {
                Optional<User> user = userService.findByUserEmail(username);

                return user;
            }
            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }*/

    public User getUserByAccessToken(String accessToken) {
        try {
            String email = extractUsername(accessToken);
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
