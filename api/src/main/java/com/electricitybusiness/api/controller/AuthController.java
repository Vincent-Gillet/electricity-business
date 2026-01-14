package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.config.CustomUserDetailService;
import com.electricitybusiness.api.model.RefreshToken;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailService customUserDetailService;

    /**
     * Point de terminaison pour l'authentification des utilisateurs.
     * Génère un access token JWT et un refresh token stocké en cookie HTTP-only.
     *
     * @param request  La requête d'authentification contenant l'email et le mot de passe
     * @param response La réponse HTTP pour ajouter le cookie
     * @return Un objet ResponseEntity contenant l'access token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.emailUser(), request.passwordUser())
        );

        final UserDetails userDetails = customUserDetailService.loadUserByUsername(request.emailUser());

        final String accessToken = jwtService.generateAccessToken(userDetails.getUsername());
        final RefreshToken refreshToken = jwtService.generateRefreshTokenBdd((User) userDetails);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getIdRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 jours
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }

    /**
     * Point de terminaison pour rafraîchir l'access token en utilisant le refresh token stocké en cookie.
     *
     * @param request  La requête HTTP contenant les cookies
     * @param response La réponse HTTP pour mettre à jour le cookie si nécessaire
     * @return Un objet ResponseEntity contenant le nouvel access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken = extractRefreshTokenFromCookie(request, "refreshAccessToken");

        // Si aucun refresh token n'est trouvé dans les cookies
        if (refreshToken == null || refreshToken.isEmpty()) {
            // L'absence de token est traitée ici
            return ResponseEntity.badRequest().body(Map.of("error", "Le refresh token est manquant."));
        }

        // Vérifier si le refresh token existe en base de données
        Optional<RefreshToken> refreshTokenSaved = jwtService.getRefreshTokenByToken(refreshToken);

        // Si le token n'est pas trouvé en base, il est invalide
        if (refreshTokenSaved.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Le refresh token est invalide ou expiré."));
        }

        User user = refreshTokenSaved.get().getUser();
        String username = user.getUsername();

        // Valide le refresh token avec la clé de refresh
        if (!jwtService.isTokenValid(refreshToken, jwtService.getRefreshSecretKey(), user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Le refresh token est invalide."));
        }

        // Génère un nouvel ACCESS token
        String newAccessToken = jwtService.generateAccessToken(username);

        // Met à jour le cookie du refresh token
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 jours
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Retourne le nouvel access token
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    /**
     * Point de terminaison pour la déconnexion des utilisateurs.
     * Supprime le refresh token de la base de données et le cookie associé.
     *
     * @param refreshTokenHeader Le refresh token passé dans l'en-tête (optionnel)
     * @param request            La requête HTTP contenant les cookies
     * @param response           La réponse HTTP pour supprimer le cookie
     * @return Un objet ResponseEntity indiquant le succès ou l'échec de la déconnexion
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "X-Refresh-Token", required = false) String refreshTokenHeader,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {

        String refreshToken = refreshTokenHeader;
        if (refreshToken == null || refreshToken.isEmpty()) {
            refreshToken = extractRefreshTokenFromCookie(request, "logout");
            if (refreshToken == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le refresh token est manquant."));
            }
        }

        // Supprime le refresh token de la base de données
        jwtService.deleteRefreshToken(refreshToken);

        // Supprime le cookie du refresh token
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie."));
    }

    /**
     * Extrait le refresh token depuis les cookies de la requête.
     * @param request La requête HTTP contenant les cookies
     * @return La valeur du refresh token, ou null s'il n'est pas trouvé
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request,  String source) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    record AuthRequest(String emailUser, String passwordUser) {}
}
