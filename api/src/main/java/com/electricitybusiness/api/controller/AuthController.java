package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.config.CustomUserDetailService;
import com.electricitybusiness.api.model.RefreshToken;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

/*    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest request, HttpServletResponse response) {
        try {
            // Authentification de l'utilisateur
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.emailUser(), request.passwordUser())
            );

            final UserDetails userDetails = customUserDetailService.loadUserByUsername(request.emailUser());
            final String jwt = jwtService.generateAccessToken(userDetails.getUsername());

            // Génération et sauvegarde du refresh token en BDD
            final RefreshToken refreshToken = jwtService.generateRefreshTokenBdd((User) userDetails);

            // Création d'un cookie HttpOnly pour le refresh token (optionnel mais recommandé)
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getIdRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 jours
                    .sameSite("None")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // Retourne uniquement l'access token (le refresh token est dans le cookie)
            return ResponseEntity.ok(Map.of("accessToken", jwt));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email ou mot de passe incorrect"));
        }
    }*/

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthRequest request, HttpServletResponse response) {
        System.out.println("AuthController: Attempting authentication for user: " + request.emailUser());
        try {
            System.out.println("AuthController: Calling authenticationManager.authenticate() for user: " + request.emailUser());
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.emailUser(), request.passwordUser())
            );
            System.out.println("AuthController: Authentication successful for user: " + request.emailUser());

            System.out.println("AuthController: Loading UserDetails for user: " + request.emailUser());
            final UserDetails userDetails = customUserDetailService.loadUserByUsername(request.emailUser());
            System.out.println("AuthController: UserDetails loaded: " + userDetails.getUsername());

            System.out.println("AuthController: Generating Access Token for user: " + userDetails.getUsername());
            final String jwt = jwtService.generateAccessToken(userDetails.getUsername());
            System.out.println("AuthController: Access Token generated.");

            System.out.println("AuthController: Generating Refresh Token in DB for user: " + userDetails.getUsername());
            final RefreshToken refreshToken = jwtService.generateRefreshTokenBdd((User) userDetails);
            System.out.println("AuthController: Refresh Token generated with ID: " + refreshToken.getIdRefreshToken());

            System.out.println("AuthController: Creating HttpOnly cookie for refresh token.");
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getIdRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 jours
                    .sameSite("None")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            System.out.println("AuthController: Authentication successful for user: " + request.emailUser() + ". Returning access token.");
            return ResponseEntity.ok(Map.of("accessToken", jwt));

        } catch (Exception e) {
            System.err.println("AuthController: ERROR - Authentication failed for user: " + request.emailUser());
            e.printStackTrace(); // Ceci est CRUCIAL pour voir la stack trace complète de l'erreur
            System.err.println("AuthController: Returning UNAUTHORIZED response.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email ou mot de passe incorrect"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken = extractRefreshTokenFromCookie(request, "refreshAccessToken");

        System.out.println("== Request == : " + request);
        System.out.println("== Refresh Token == : " + refreshToken);

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

        try {
            User user = refreshTokenSaved.get().getUser();
            String username = user.getUsername();

            // Valide le refresh token avec la clé de refresh
            if (!jwtService.isTokenValid(refreshToken, jwtService.getRefreshSecretKey(), user)) { // <--- UTILISER getRefreshSecretKey()
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Le refresh token est invalide."));
            }

            // Génère un nouvel ACCESS token
            String newAccessToken = jwtService.generateAccessToken(username); // OK, utilise la clé d'accès

            // Met à jour le cookie du refresh token
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true) // Assurez-vous que c'est bien 'true' en production
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60) // 7 jours
                    .sameSite("None")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // Retourne le nouvel access token
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Erreur lors du rafraîchissement du token."));
        }
    }



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

        try {
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

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Une erreur est survenue lors de la déconnexion."));
        }
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
