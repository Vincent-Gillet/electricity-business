package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.config.CustomUserDetailService;
import com.electricitybusiness.api.dto.AuthenticationResponse;
import com.electricitybusiness.api.dto.user.UserDTO;
import com.electricitybusiness.api.model.RefreshToken;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.model.UserRole;
import com.electricitybusiness.api.service.JwtService;
import com.electricitybusiness.api.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final AuthenticationManager authenticationManager;

    private final CustomUserDetailService customUserDetailService;

    // Original fonctionne
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> authenticate(@RequestBody AuthRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.emailUser(), request.passwordUser())
        );

        final UserDetails userDetails = customUserDetailService.loadUserByUsername(request.emailUser());

        final String jwt = jwtService.generateAccessToken(userDetails.getUsername());

        final RefreshToken refreshToken = refreshTokenService.generateRefreshTokenBdd((User) userDetails);

        return ResponseEntity.ok(
                Map.of(
                        "accessToken", jwt,
                        "refreshToken", String.valueOf(refreshToken.getIdRefreshToken())
                )
        );
    }

    // Nouveau avec cookie HttpOnly pour le refresh token
/*    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthRequest request,
            HttpServletResponse response
    ) { // Injectez HttpServletResponse

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.emailUser(), request.passwordUser())
        );

        final UserDetails userDetails = customUserDetailService.loadUserByUsername(request.emailUser());
        User user = (User) userDetails; // Cast vers votre modèle User (assurez-vous que User implémente UserDetails)

        // 1. Générer l'Access Token (JWT de courte durée, pour les requêtes API)
        final String accessToken = jwtService.generateAccessToken(userDetails.getUsername());

        // 2. Générer un nouveau Refresh Token et le stocker en BDD
        RefreshToken refreshTokenEntity = refreshTokenService.generateRefreshTokenBdd(user);
        String refreshTokenString = refreshTokenEntity.getIdRefreshToken(); // Le JWT généré pour le refresh

        // 3. Créer le cookie HttpOnly pour le Refresh Token
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshTokenString)
                .httpOnly(true) // Empêche l'accès via JavaScript
                .secure(true)  // N'envoyez le cookie que via HTTPS (OBLIGATOIRE en production)
                .path("/api/auth") // Limite la portée du cookie. Le refresh endpoint doit être sous ce chemin.
                // Utilisez un chemin plus précis comme "/api/auth/refresh" si vous le souhaitez.
                .sameSite("Lax") // Protection CSRF : "Strict", "Lax" ou "None" (si cross-domain, nécessite secure=true)
                .maxAge(refreshTokenService.getExpirationTime() / 1000) // Durée de vie du cookie en secondes
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 4. Retourner l'Access Token au frontend dans le corps de la réponse
        return ResponseEntity.ok(
                AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .build()
        );
    }*/


    record AuthRequest (String emailUser, String passwordUser) {}

/*
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshAccessToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
*/


    // Original fonctionne
    @GetMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshAccessToken(@RequestHeader("X-Refresh-Token") String refreshToken) {

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le refresh token est manquant ou invalide."));
        }

        Optional<RefreshToken> refreshTokenSave = refreshTokenService.getRefreshTokenByToken(refreshToken);

        if (refreshTokenSave == null || refreshTokenSave.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Un refresh token est requis."));
        }

        try {
            User user = refreshTokenSave.get().getUser();
            String username = user.getUsername();

            if (!refreshTokenService.isTokenValid(refreshToken, user)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Le refresh token est invalide."));
            }

            String nouveauAccessToken = jwtService.generateAccessToken(username);

            return ResponseEntity.ok(Map.of("accessToken", nouveauAccessToken));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","Une erreur de traitement du refresh token."));
        }

    }



    // --- Endpoint de Rafraîchissement du Token ---
/*    @PostMapping("/refresh") // Utilisez POST, c'est plus approprié pour cette opération
    public ResponseEntity<AuthenticationResponse> refreshAccessToken(
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookieValue, // Lit le cookie HttpOnly
            HttpServletResponse response
    ) {
        // 1. Vérifier la présence du refresh token dans le cookie
        if (refreshTokenCookieValue == null || refreshTokenCookieValue.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    AuthenticationResponse.builder().accessToken(null).build()); // Ou un DTO d'erreur
        }

        // 2. Vérifier si le refresh token existe en BDD
        Optional<RefreshToken> refreshTokenSaveOptional = refreshTokenService.getRefreshTokenByToken(refreshTokenCookieValue);

        if (refreshTokenSaveOptional.isEmpty()) {
            // Le token n'est pas trouvé en BDD (peut-être déjà utilisé, expiré, révoqué)
            clearRefreshTokenCookie(response); // Nettoyer le cookie invalide
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    AuthenticationResponse.builder().accessToken(null).build());
        }

        RefreshToken storedRefreshToken = refreshTokenSaveOptional.get();
        User user = storedRefreshToken.getUser();

        // 3. Valider le refresh token (expiration et signature)
        if (!refreshTokenService.isTokenValid(refreshTokenCookieValue, user)) {
            // Token invalide ou expiré selon la logique du service
            refreshTokenService.deleteRefreshToken(refreshTokenCookieValue); // Supprimer de la BDD
            clearRefreshTokenCookie(response); // Nettoyer le cookie invalide
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    AuthenticationResponse.builder().accessToken(null).build());
        }

        // --- Rotation du Refresh Token (sécurité accrue) ---
        // 4. Supprimer l'ancien refresh token de la BDD (il est à usage unique)
        refreshTokenService.deleteRefreshToken(refreshTokenCookieValue);
        // 5. Générer un NOUVEAU refresh token et le stocker en BDD
        RefreshToken newRefreshTokenEntity = refreshTokenService.generateRefreshTokenBdd(user);
        String newRefreshTokenString = newRefreshTokenEntity.getIdRefreshToken();

        // 6. Générer un nouvel Access Token
        String newAccessToken = jwtService.generateAccessToken(user.getUsername());

        // 7. Définir le nouveau refresh token dans un cookie HttpOnly
        ResponseCookie newRefreshCookie = ResponseCookie.from("refreshToken", newRefreshTokenString)
                .httpOnly(true)
                .secure(true)
                .path("/api/auth") // Doit correspondre au chemin du cookie précédent
                .sameSite("Lax") // Doit correspondre à la configuration du cookie précédent
                .maxAge(refreshTokenService.getExpirationTime() / 1000)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, newRefreshCookie.toString());

        // 8. Retourner le nouvel Access Token
        return ResponseEntity.ok(
                AuthenticationResponse.builder()
                        .accessToken(newAccessToken)
                        .build()
        );
    }*/

/*    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Supprimer le cookie du refresh token côté client
        clearRefreshTokenCookie(response);
        // Optionnel : Si vous souhaitez invalider activement le refresh token en BDD lors de la déconnexion,
        // vous auriez besoin d'un mécanisme pour récupérer sa valeur (par exemple, via un filtre qui le lit du cookie avant qu'il ne soit effacé)
        // ou de gérer une révocation par utilisateur. Pour l'instant, la suppression du cookie est suffisante pour l'UX.
        return ResponseEntity.ok().build();
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/auth") // IMPORTANT : Le chemin doit correspondre au chemin du cookie original !
                .maxAge(0) // Expire immédiatement
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }*/





/*    @GetMapping("/me/{token}")
    public ResponseEntity<UserDTO> getUserByTokenUrl(@PathVariable String token) {
        return refreshTokenService.getUserByRefreshToken(token)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }*/



/*    @GetMapping("/me-refresh")
    public ResponseEntity<UserDTO> getUserByTokenRefresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        return refreshTokenService.getUserByRefreshToken(refreshToken)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }*/

/*    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getUserByTokenAccess(@RequestHeader("Authorization") String authHeader) {
        try {
            if (!authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String accessToken = authHeader.substring(7);
            Optional<UserDTO> userDTO = jwtService.getUserDTOByAccessToken(accessToken);

            if (userDTO.isPresent()) {
                return ResponseEntity.ok(userDTO.get());
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }*/



/*    @GetMapping("/test-dto")
    public ResponseEntity<UserDTO> testDto() {
        UserDTO test = new UserDTO();
        test.setSurnameUser("Test");
        test.setFirstName("Test");
        test.setPseudo("TestUser");
        test.setRole(UserRole.USER);
        test.setDateOfBirth(LocalDate.of(2000,1, 1));
        test.setPhone("0123456789");
        test.setIban("FR7612345678901234567890123");
        test.setBanished(false);
        return ResponseEntity.ok(test);
    }*/
}
