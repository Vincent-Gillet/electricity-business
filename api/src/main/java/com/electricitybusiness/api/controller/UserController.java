package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.*;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    /**
     * Récupère tous les utilisateurs.
     * GET /api/users
     * @return Une liste de tous les utilisateurs
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDTO> usersDTO = users.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usersDTO);
    }

    /**
     * Récupère un utilisateur par son ID.
     * GET /api/users/{id}
     * @param id L'identifiant de l'utilisateur à récupérer
     * @return L'utilisateur correspondant à l'ID, ou un statut HTTP 404 Not Found si non trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(mapper.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée un nouvel utilisateur.
     * POST /api/users
     * @param userDTO L'utilisateur à créer
     * @return L'utilisateur créé avec un statut HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<UserDTO> saveUser(@Valid @RequestBody UserCreateDTO userDTO) {
        User user = mapper.toEntity(userDTO);
        User savedUser = userService.saveUser(user);
        UserDTO savedDTO = mapper.toDTO(savedUser);
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
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        User existing = userService.getUserById(id).orElseThrow();
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
    public ResponseEntity<UserDTO> updateUserPassword(@PathVariable Long id, @Valid @RequestBody UserUpdatePasswordDTO userUpdatePasswordDTO) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        User existing = userService.getUserById(id).orElseThrow();
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
    public ResponseEntity<UserDTO> updateUserBanni(@PathVariable Long id, @Valid @RequestBody UserUpdateBanishedDTO userUpdateBanniDTO) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        User existing = userService.getUserById(id).orElseThrow();
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
    public ResponseEntity<UserDTO> updateUserRole(@PathVariable Long id, @Valid @RequestBody UserUpdateRoleDTO userUpdateRoleDTO) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        User existing = userService.getUserById(id).orElseThrow();
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
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère un utilisateur par son pseudo.
     * GET /api/users/username/{username}
     * @param username Le pseudo de l'utilisateur à récupérer
     * @return L'utilisateur correspondant au pseudo, ou un statut HTTP 404 Not Found si non trouvé
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
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
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        return userService.findByUserEmail(email)
                .map(user -> ResponseEntity.ok(mapper.toDTO(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}
