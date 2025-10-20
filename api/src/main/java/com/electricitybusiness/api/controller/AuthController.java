package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.config.CustomUserDetailService;
import com.electricitybusiness.api.dto.UserDTO;
import com.electricitybusiness.api.model.RefreshToken;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.model.UserRole;
import com.electricitybusiness.api.service.JwtService;
import com.electricitybusiness.api.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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


    record AuthRequest (String emailUser, String passwordUser) {}

/*
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshAccessToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
*/

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


    @GetMapping("/me/{token}")
    public ResponseEntity<UserDTO> getUserByTokenUrl(@PathVariable String token) {
        return refreshTokenService.getUserByRefreshToken(token)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }



    @GetMapping("/me-refresh")
    public ResponseEntity<UserDTO> getUserByTokenRefresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        return refreshTokenService.getUserByRefreshToken(refreshToken)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }


    @GetMapping("/me")
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
    }



    @GetMapping("/test-dto")
    public ResponseEntity<UserDTO> testDto() {
        UserDTO test = new UserDTO();
        test.setSurnameUser("Test");
        test.setFirstName("Test");
        test.setUsername("TestUser");
        test.setRole(UserRole.USER);
        test.setDateOfBirth(LocalDate.of(2000,1, 1));
        test.setPhone("0123456789");
        test.setIban("FR7612345678901234567890123");
        test.setBanished(false);
        return ResponseEntity.ok(test);
    }
}
