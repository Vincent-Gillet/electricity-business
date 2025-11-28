package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.user.*;
import com.electricitybusiness.api.exception.ResourceNotFoundException;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.service.JwtService;
import com.electricitybusiness.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des utilisateurs.
 * Expose les endpoints pour les opérations CRUD sur les utilisateurs.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EntityMapper mapper;
    private final JwtService jwtService;

    /**
     * Récupère tous les utilisateurs.
     * GET /api/users
     * @return Une liste de tous les utilisateurs
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> userDTOList = userService.getAllUsers();
        return ResponseEntity.ok(userDTOList);
    }

    /**
     * Récupère un utilisateur par son ID.
     * GET /api/users/{id}
     * @param id L'identifiant de l'utilisateur à récupérer
     * @return L'utilisateur correspondant à l'ID, ou un statut HTTP 404 Not Found si non trouvé
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(mapper.toDTO(user));
    }

    /**
     * Crée un nouvel utilisateur.
     * POST /api/users
     * @param userDTO L'utilisateur à créer
     * @return L'utilisateur créé avec un statut HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<UserDTO> saveUser(@Valid @RequestBody UserCreateDTO userDTO) {
        UserDTO savedDTO = userService.saveUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }

    /**
     * Met à jour un utilisateur existant.
     * PUT /api/users/{id}
     * @param id L'identifiant de l'utilisateur à mettre à jour
     * @param userUpdateDTO Les nouvelles informations de l'utilisateur
     * @return L'utilisateur mis à jour, ou un statut HTTP 404 Not Found si l'ID n'existe pas
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        User existing = userService.getUserById(id);
        User user = mapper.toEntity(userUpdateDTO, existing);
        User updatedUser = userService.updateUser(id, user);
        UserDTO updatedDTO = mapper.toDTO(updatedUser);
        return ResponseEntity.ok(updatedDTO);
    }

    /**
     * Met à jour le mot de passe d'un utilisateur.
     * PUT /api/users/password/{id}
     * @param id L'identifiant de l'utilisateur dont le mot de passe doit être mis à jour
     * @param userUpdatePasswordDTO Les nouvelles informations de mot de passe
     * @return L'utilisateur mis à jour avec le nouveau mot de passe, ou un statut HTTP 404 Not Found si l'ID n'existe pas
     */
    @PutMapping("/password/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> updateUserPassword(@PathVariable Long id, @Valid @RequestBody UserUpdatePasswordDTO userUpdatePasswordDTO) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        User existing = userService.getUserById(id);
        User user = mapper.toEntityPassword(userUpdatePasswordDTO, existing);
        User updatedUser = userService.updateUser(id, user);
        UserDTO updatedDTO = mapper.toDTO(updatedUser);
        return ResponseEntity.ok(updatedDTO);
    }

    /**
     * Met à jour le statut "banni" d'un utilisateur.
     * PUT /api/users/banni/{id}
     * @param id L'identifiant de l'utilisateur à mettre à jour
     * @param userUpdateBanniDTO Les nouvelles informations de l'utilisateur, y compris le statut banni
     * @return L'utilisateur mis à jour avec le statut banni, ou un statut HTTP 404 Not Found si l'ID n'existe pas
     */
    @PutMapping("/banished/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> updateUserBanni(@PathVariable Long id, @Valid @RequestBody UserUpdateBanishedDTO userUpdateBanniDTO) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        User existing = userService.getUserById(id);
        User user = mapper.toEntityBanished(userUpdateBanniDTO, existing);
        User updatedUser = userService.updateUser(id, user);
        UserDTO updatedDTO = mapper.toDTO(updatedUser);
        return ResponseEntity.ok(updatedDTO);
    }

    /**
     * Met à jour le rôle d'un utilisateur.
     * PUT /api/users/role/{id}
     * @param id L'identifiant de l'utilisateur dont le rôle doit être mis à jour
     * @param userUpdateRoleDTO Les nouvelles informations de rôle de l'utilisateur
     * @return L'utilisateur mis à jour avec le nouveau rôle, ou un statut HTTP 404 Not Found si l'ID n'existe pas
     */
    @PutMapping("/role/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> updateUserRole(@PathVariable Long id, @Valid @RequestBody UserUpdateRoleDTO userUpdateRoleDTO) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        User existing = userService.getUserById(id);
        User user = mapper.toEntityRole(userUpdateRoleDTO, existing);
        User updatedUser = userService.updateUser(id, user);
        UserDTO updatedDTO = mapper.toDTO(updatedUser);
        return ResponseEntity.ok(updatedDTO);
    }

    /**
     * Supprime un utilisateur par son ID.
     * DELETE /api/users/{id}
     * @param id L'identifiant de l'utilisateur à supprimer
     * @return Une réponse vide avec le statut 204 No Content si l'utilisateur a été supprimé, ou 404 Not Found si l'utilisateur n'existe pas
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère un utilisateur par son pseudo.
     * GET /api/users/pseudo/{pseudo}
     * @param pseudo Le pseudo de l'utilisateur à récupérer
     * @return L'utilisateur correspondant au pseudo, ou un statut HTTP 404 Not Found si non trouvé
     */
    @GetMapping("/pseudo/{pseudo}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> getUserByPseudo(@PathVariable String pseudo) {
        return userService.findByPseudo(pseudo)
                .map(user -> ResponseEntity.ok(mapper.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Récupère un utilisateur par son adresse email.
     * GET /api/users/email/{email}
     * @param email L'adresse email de l'utilisateur à récupérer
     * @return L'utilisateur correspondant à l'email, ou un statut HTTP 404 Not Found si non trouvé
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(mapper.toDTO(user));
    }

    @GetMapping("/me")
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
    }

    /**
     * Met à jour un utilisateur existant.
     * PUT /api/users/{id}
     * @param token L'identifiant de l'utilisateur à mettre à jour
     * @param userUpdateDTO Les nouvelles informations de l'utilisateur
     * @return L'utilisateur mis à jour, ou un statut HTTP 404 Not Found si l'ID n'existe pas
     */
    @PutMapping("/token")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateUser(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UserUpdateDTO userUpdateDTO
    ) {
        String rawToken = token.replace("Bearer ", "");
        User existingUser = jwtService.getUserByAccessToken(rawToken);
        try {
            User incoming = mapper.toEntity(userUpdateDTO, existingUser);
            User updated = userService.updateUserToken(existingUser.getIdUser(), incoming);
            return ResponseEntity.ok(mapper.toDTO(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Met à jour le mot de passe d'un utilisateur.
     * PUT /api/users/password/{id}
     * @param token L'identifiant de l'utilisateur dont le mot de passe doit être mis à jour
     * @param userUpdatePasswordDTO Les nouvelles informations de mot de passe
     * @return L'utilisateur mis à jour avec le nouveau mot de passe, ou un statut HTTP 404 Not Found si l'ID n'existe pas
     */
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> updateUserPassword(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody UserUpdatePasswordDTO userUpdatePasswordDTO
    ) {
        String rawToken = token.replace("Bearer ", "");

        User existingUser = jwtService.getUserByAccessToken(rawToken);

        try {
            User incoming = mapper.toEntityPassword(userUpdatePasswordDTO, existingUser);
            User updated = userService.updateUserToken(existingUser.getIdUser(), incoming);
            return ResponseEntity.ok(mapper.toDTO(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @DeleteMapping("/delete/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteUserByTokenAccess(@RequestHeader("Authorization") String authHeader) {
        try {
            if (!authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String accessToken = authHeader.substring(7);
            User existingUser = jwtService.getUserByAccessToken(accessToken);

            userService.deleteUserById(existingUser.getIdUser());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
