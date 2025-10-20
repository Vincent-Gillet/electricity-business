package com.electricitybusiness.api.service;


import com.electricitybusiness.api.dto.UserDTO;
import com.electricitybusiness.api.model.RefreshToken;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.secret-key-refresh-token}")
    private String secretKey;

    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7; // 7 jours

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)

                .setIssuedAt(new Date(System.currentTimeMillis()))

                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))

                .signWith(getSigningKey(), SignatureAlgorithm.HS256)

                .compact();
    }

    public RefreshToken generateRefreshTokenBdd(User user) {
        String token = generateRefreshToken(user.getEmailUser());
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setIdRefreshToken(token);
        refreshToken.setUser(user);

        return refreshTokenRepository.save(refreshToken);
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
        return Jwts.parserBuilder()
                // On définit la clé de signature pour vérifier l’authenticité du token.
                .setSigningKey(getSigningKey())
                .build()
                // On parse le token JWT et on récupère la partie "Claims" (le payload).
                .parseClaimsJws(token)
                .getBody(); // C’est ici qu’on obtient les données contenues dans le token.
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

    /**
     * Récupère un refresh token par son ID.
     * @param id L'identifiant du refresh token à récupérer
     * @return Un Optional contenant le refresh token si trouvé, sinon vide
     */
    public Optional<RefreshToken> getRefreshTokenByToken(String id) { return refreshTokenRepository.findById(id); }



    public Optional<UserDTO> getUserByRefreshToken(String token) {
        System.out.println("Looking for token: " + token);

        Optional<UserDTO> result = refreshTokenRepository.findByIdRefreshToken(token)
                .map(RefreshToken::getUser)
                .map(user -> new UserDTO(
                        user.getSurnameUser(),
                        user.getFirstName(),
                        user.getUsername(),
                        user.getEmailUser(),
                        user.getRole(),
                        user.getDateOfBirth(),
                        user.getPhone(),
                        user.getIban(),
                        user.getBanished()
                ));

        System.out.println("User found? " + (result.isPresent() ? "Yes: " + result.get() : "No"));

        return result;
    }

    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteById(token);
    }
}
