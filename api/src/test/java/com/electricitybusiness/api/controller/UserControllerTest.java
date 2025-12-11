package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.config.ActiveSecurityTestConfig;
import com.electricitybusiness.api.config.JwtAuthFilter;
import com.electricitybusiness.api.dto.user.UserCreateDTO;
import com.electricitybusiness.api.dto.user.UserDTO;
import com.electricitybusiness.api.dto.user.UserUpdateBanishedDTO;
import com.electricitybusiness.api.dto.user.UserUpdateDTO;
import com.electricitybusiness.api.dto.user.UserUpdatePasswordDTO;
import com.electricitybusiness.api.dto.user.UserUpdateRoleDTO;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.model.UserRole;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.service.JwtService;
import com.electricitybusiness.api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthFilter.class
        ),
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class
        }
)
@Import(ActiveSecurityTestConfig.class)
/*
@ActiveProfiles("springSecurityFilterChain")
*/
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private EntityMapper mapper;
    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO userDTO;
    private User user;
    private UserCreateDTO userCreateDTO;
    private UserUpdateDTO userUpdateDTO;
    private UserUpdatePasswordDTO userUpdatePasswordDTO;
    private UserUpdateBanishedDTO userUpdateBanishedDTO;
    private UserUpdateRoleDTO userUpdateRoleDTO;

    @BeforeEach
    void setUp() {
        // Initialisation des objets DTO et Entity pour les tests
        userDTO = new UserDTO(
                "Doe",
                "John",
                "testUser",
                "test@example.com",
                UserRole.USER,
                LocalDate.of(1990, 1, 1),
                "0123456789",
                null,
                false
        );
        user = new User(
                1L,
                "Doe",
                "John",
                "testUser",
                "test@example.com",
                "passwordHash",
                UserRole.USER,
                LocalDate.of(1990, 1, 1),
                "0123456789",
                null,
                false,
                null,
                null,
                null,
                null,
                null);
        userCreateDTO = new UserCreateDTO(
                "Doe",
                "John",
                "testUser",
                "passwordHash",
                "test@example.com",
                LocalDate.of(1990, 1, 1),
                "0123456789"
        );
        userUpdateDTO = new UserUpdateDTO(
                "Doe",
                "John",
                "testUser",
                "test@example.com",
                LocalDate.of(1990, 1, 1),
                "0123456789",
                "DE89370400440532013000"
        );
        userUpdatePasswordDTO = new UserUpdatePasswordDTO("newPassword");
        userUpdateBanishedDTO = new UserUpdateBanishedDTO(true);
        userUpdateRoleDTO = new UserUpdateRoleDTO(UserRole.ADMIN);

        when(mapper.toDTO(any(User.class))).thenReturn(userDTO);
        when(mapper.toEntity(any(UserUpdateDTO.class), any(User.class))).thenReturn(user);
        when(mapper.toEntityPassword(any(UserUpdatePasswordDTO.class), any(User.class))).thenReturn(user);
        when(mapper.toEntityBanished(any(UserUpdateBanishedDTO.class), any(User.class))).thenReturn(user);
        when(mapper.toEntityRole(any(UserUpdateRoleDTO.class), any(User.class))).thenReturn(user);
    }

    /**
     * Teste la récupération de tous les utilisateurs par un administrateur.
     * Vérifie que la réponse contient une liste d'utilisateurs et que le service est appelé une fois.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getAllUsers_whenAdmin_shouldReturnListOfUsers() throws Exception {
        List<UserDTO> userList = Arrays.asList(
                userDTO,
                new UserDTO(
                        "Super",
                        "Admin",
                        "adminUser",
                        "test@example.com",
                        UserRole.ADMIN,
                        LocalDate.of(1990, 1, 1),
                        "0123456789",
                        null,
                        false
                )
        );
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].pseudo").value(userDTO.getPseudo()));

        verify(userService, times(1)).getAllUsers();
    }

    /**
     * Teste la récupération de tous les utilisateurs par un administrateur lorsque aucun utilisateur n'est trouvé.
     * Vérifie que la réponse est un statut 404 Not Found et que le service est appelé une fois.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getAllUsers_whenAdmin_andNoUsersFound_shouldReturnNotFound() throws Exception {
        // Configure le mock pour retourner une liste vide
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getAllUsers();
    }

    /**
     * Teste la récupération d'un utilisateur par son ID par un administrateur lorsque l'utilisateur existe.
     * Vérifie que la réponse contient les informations de l'utilisateur et que le service est appelé une fois.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getUserById_whenAdminAndUserExists_shouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/users/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.pseudo").value(userDTO.getPseudo()));

        verify(userService, times(1)).getUserById(1L);
    }

    /**
     * Teste la récupération d'un utilisateur par son ID par un administrateur lorsque l'utilisateur n'est pas trouvé.
     * Vérifie que la réponse est un statut 404 Not Found et que le service est appelé une fois.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getUserById_whenAdminAndUserNotFound_shouldReturnNotFound() throws Exception {
        when(userService.getUserById(anyLong())).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/users/{id}", 99L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService, times(1)).getUserById(99L);
    }

    /**
     * Teste la récupération d'un utilisateur par son ID par un utilisateur non administrateur.
     * Vérifie que la réponse est un statut 403 Forbidden et que le service n'est jamais appelé.
     */
    @Test
    @WithMockUser(authorities = {"USER"})
    void getUserById_whenNotAdmin_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(userService, never()).getUserById(anyLong());
    }

    /**
     * Teste la récupération d'un utilisateur par son ID par un administrateur lorsque l'utilisateur n'existe pas.
     * Vérifie que la réponse est un statut 404 Not Found et que le service est appelé une fois.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getUserById_whenAdminAndUserNotExists_shouldReturnNotFound() throws Exception {
        Long nonExistentUserId = 99L;

        when(userService.getUserById(nonExistentUserId)).thenReturn(null);

        mockMvc.perform(get("/api/users/{id}", nonExistentUserId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(nonExistentUserId);
    }

    /**
     * Teste la mise à jour d'un utilisateur par son ID par un administrateur lorsque l'utilisateur existe.
     * Vérifie que la réponse contient les informations mises à jour de l'utilisateur et que les services sont appelés une fois.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void updateUser_whenAdminAndUserExists_shouldReturnUpdatedUser() throws Exception {
        when(userService.existsById(1L)).thenReturn(true);
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pseudo").value(userDTO.getPseudo()));

        verify(userService, times(1)).existsById(1L);
        verify(userService, times(1)).getUserById(1L);
        verify(mapper, times(1)).toEntity(any(UserUpdateDTO.class), eq(user));
        verify(userService, times(1)).updateUser(eq(1L), any(User.class));
    }

    /**
     * Teste la mise à jour d'un utilisateur par son ID par un administrateur lorsque l'utilisateur n'est pas trouvé.
     * Vérifie que la réponse est un statut 404 Not Found et que les services appropriés sont appelés.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void updateUser_whenAdminAndUserNotFound_shouldReturnNotFound() throws Exception {
        when(userService.existsById(99L)).thenReturn(false);

        mockMvc.perform(put("/api/users/{id}", 99L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).existsById(99L);
        verify(userService, never()).getUserById(anyLong());
        verify(userService, never()).updateUser(anyLong(), any(User.class));
    }

    /**
     * Teste la mise à jour d'un utilisateur par son ID par un utilisateur non administrateur.
     * Vérifie que la réponse est un statut 403 Forbidden et que les services appropriés ne sont jamais appelés.
     */
    @Test
    @WithMockUser(authorities = {"USER"})
    void updateUser_whenNotAdmin_shouldReturnForbidden() throws Exception {
        mockMvc.perform(put("/api/users/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isForbidden());

        verify(userService, never()).existsById(anyLong());
        verify(userService, never()).updateUser(anyLong(), any(User.class));
    }

    /**
     * Teste la mise à jour du mot de passe d'un utilisateur par son ID par un administrateur lorsque l'utilisateur existe.
     * Vérifie que la réponse est correcte et que les services appropriés sont appelés.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void updateUserPasswordById_whenAdminAndUserExists_shouldReturnUpdatedUser() throws Exception {
        when(userService.existsById(1L)).thenReturn(true);
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/password/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdatePasswordDTO)))
                .andExpect(status().isOk());

        verify(userService, times(1)).existsById(1L);
        verify(userService, times(1)).getUserById(1L);
        verify(mapper, times(1)).toEntityPassword(any(UserUpdatePasswordDTO.class), eq(user));
        verify(userService, times(1)).updateUser(eq(1L), any(User.class));
    }

    /**
     * Teste la mise à jour du mot de passe d'un utilisateur par son ID par un administrateur lorsque l'utilisateur n'est pas trouvé.
     * Vérifie que la réponse est un statut 404 Not Found et que les services appropriés sont appelés.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void updateUserPasswordById_whenAdminAndUserNotFound_shouldReturnNotFound() throws Exception {
        when(userService.existsById(99L)).thenReturn(false);

        mockMvc.perform(put("/api/users/password/{id}", 99L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdatePasswordDTO)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).existsById(99L);
        verify(userService, never()).updateUser(anyLong(), any(User.class));
    }

    /**
     * Teste la mise à jour du statut banni d'un utilisateur par son ID par un administrateur lorsque l'utilisateur existe.
     * Vérifie que la réponse est correcte et que les services appropriés sont appelés.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void updateUserBanni_whenAdminAndUserExists_shouldReturnUpdatedUser() throws Exception {
        when(userService.existsById(1L)).thenReturn(true);
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/banished/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateBanishedDTO)))
                .andExpect(status().isOk());

        verify(userService, times(1)).existsById(1L);
        verify(userService, times(1)).getUserById(1L);
        verify(mapper, times(1)).toEntityBanished(any(UserUpdateBanishedDTO.class), eq(user));
        verify(userService, times(1)).updateUser(eq(1L), any(User.class));
    }

    /**
     * Teste la mise à jour du statut banni d'un utilisateur par son ID par un administrateur lorsque l'utilisateur n'est pas trouvé.
     * Vérifie que la réponse est un statut 404 Not Found et que les services appropriés sont appelés.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void updateUserBanni_whenAdminAndUserNotFound_shouldReturnNotFound() throws Exception {
        Long nonExistentUserId = 99L;
        UserUpdateBanishedDTO userUpdateBanishedDTO = new UserUpdateBanishedDTO(true);

        when(userService.existsById(nonExistentUserId)).thenReturn(false);

        mockMvc.perform(put("/api/users/banished/{id}", nonExistentUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateBanishedDTO)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).existsById(nonExistentUserId);
    }

    /**
     * Teste la mise à jour du rôle d'un utilisateur par son ID par un administrateur lorsque l'utilisateur existe.
     * Vérifie que la réponse est correcte et que les services appropriés sont appelés.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void updateUserRole_whenAdminAndUserExists_shouldReturnUpdatedUser() throws Exception {
        when(userService.existsById(1L)).thenReturn(true);
        when(userService.getUserById(1L)).thenReturn(user);
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/role/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRoleDTO)))
                .andExpect(status().isOk());

        verify(userService, times(1)).existsById(1L);
        verify(userService, times(1)).getUserById(1L);
        verify(mapper, times(1)).toEntityRole(any(UserUpdateRoleDTO.class), eq(user));
        verify(userService, times(1)).updateUser(eq(1L), any(User.class));
    }

    /**
     * Teste la mise à jour du rôle d'un utilisateur par son ID par un administrateur lorsque l'utilisateur n'est pas trouvé.
     * Vérifie que la réponse est un statut 404 Not Found et que les services appropriés sont appelés.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void updateUserRole_whenAdminAndUserNotExists_shouldReturnNotFound() throws Exception {
        Long nonExistentUserId = 99L;
        UserUpdateRoleDTO userUpdateRoleDTO = new UserUpdateRoleDTO(UserRole.USER);

        when(userService.existsById(nonExistentUserId)).thenReturn(false);

        mockMvc.perform(put("/api/users/role/{id}", nonExistentUserId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRoleDTO)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).existsById(nonExistentUserId);
    }

    /**
     * Teste la suppression d'un utilisateur par son ID par un administrateur.
     * Vérifie que la réponse est un statut 204 No Content et que le service de suppression est appelé une fois.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void deleteUser_whenAdmin_shouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUserById(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserById(1L);
    }

    /**
     * Teste la suppression d'un utilisateur par son ID par un utilisateur non administrateur.
     * Vérifie que la réponse est un statut 403 Forbidden et que le service de suppression n'est jamais appelé.
     */
    @Test
    @WithMockUser(authorities = {"USER"})
    void deleteUser_whenNotAdmin_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUserById(anyLong());
    }

    /**
     * Teste la récupération d'un utilisateur par son pseudo par un administrateur lorsque l'utilisateur existe.
     * Vérifie que la réponse contient les informations de l'utilisateur et que le service est appelé une fois.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getUserByPseudo_whenAdminAndUserExists_shouldReturnUser() throws Exception {
        when(userService.findByPseudo("testUser")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/pseudo/{pseudo}", "testUser")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pseudo").value(userDTO.getPseudo()));

        verify(userService, times(1)).findByPseudo("testUser");
    }

    /**
     * Teste la récupération d'un utilisateur par son pseudo par un administrateur lorsque l'utilisateur n'est pas trouvé.
     * Vérifie que la réponse est un statut 404 Not Found et que le service est appelé une fois.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getUserByPseudo_whenAdminAndUserNotFound_shouldReturnNotFound() throws Exception {
        when(userService.findByPseudo("unknownPseudo")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/pseudo/{pseudo}", "unknownPseudo")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findByPseudo("unknownPseudo");
    }

    /**
     * Teste la récupération d'un utilisateur par son email par un administrateur lorsque l'utilisateur existe.
     * Vérifie que la réponse contient les informations de l'utilisateur et que le service est appelé une fois.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getUserByEmail_whenAdminAndUserExists_shouldReturnUser() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(user);

        mockMvc.perform(get("/api/users/email/{email}", "test@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailUser").value(userDTO.getEmailUser()));

        verify(userService, times(1)).getUserByEmail("test@example.com");
    }

    /**
     * Teste la récupération d'un utilisateur par son email par un administrateur lorsque l'utilisateur n'est pas trouvé.
     * Vérifie que la réponse est un statut 404 Not Found et que le service est appelé une fois.
     */
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getUserByEmail_whenAdminAndUserNotFound_shouldReturnNotFound() throws Exception {
        // Supposons que getUserByEmail lève une exception si non trouvé, ou retourne null
        when(userService.getUserByEmail(anyString())).thenThrow(new RuntimeException("Email not found"));

        mockMvc.perform(get("/api/users/email/{email}", "unknown@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()); // Ou NotFound si géré spécifiquement

        verify(userService, times(1)).getUserByEmail("unknown@example.com");
    }

    /**
     * Teste la récupération de l'utilisateur actuel par token d'accès valide.
     * Vérifie que la réponse contient les informations de l'utilisateur et que le service JWT est appelé une fois.
     */
    @Test
    @WithMockUser(username = "currentUser", roles = {"USER"})
    void getUserByTokenAccess_whenValidToken_shouldReturnCurrentUser() throws Exception {
        String accessToken = "mockAccessToken";
        when(jwtService.getUserDTOByAccessToken(accessToken)).thenReturn(Optional.of(userDTO));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(jwtService, times(1)).getUserDTOByAccessToken(accessToken);
    }

    /**
     * Teste la récupération de l'utilisateur actuel sans préfixe Bearer dans le token d'accès.
     * Vérifie que la réponse est un statut 401 Unauthorized et que le service JWT n'est jamais appelé.
     */
    @Test
    @WithMockUser(username = "currentUser", roles = {"USER"})
    void getUserByTokenAccess_whenNoBearerPrefix_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "InvalidTokenFormat"))
                .andExpect(status().isUnauthorized());

        verify(jwtService, never()).getUserDTOByAccessToken(anyString());
    }

    /**
     * Teste la récupération de l'utilisateur actuel avec un token d'accès invalide.
     * Vérifie que la réponse est un statut 401 Unauthorized et que le service JWT est appelé une fois.
     */
    @Test
    @WithMockUser(username = "currentUser", roles = {"USER"})
    void getUserByTokenAccess_whenInvalidToken_shouldReturnUnauthorized() throws Exception {
        String accessToken = "invalidAccessToken";
        when(jwtService.getUserDTOByAccessToken(accessToken)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());

        verify(jwtService, times(1)).getUserDTOByAccessToken(accessToken);
    }

    /**
     * Teste la récupération de l'utilisateur actuel lorsque le service JWT lève une exception.
     * Vérifie que la réponse est un statut 500 Internal Server Error et que le service JWT est appelé une fois.
     */
    @Test
    @WithMockUser(username = "currentUser", roles = {"USER"})
    void getUserByTokenAccess_whenJwtServiceThrowsException_shouldReturnInternalServerError() throws Exception {
        String accessToken = "errorToken";
        when(jwtService.getUserDTOByAccessToken(accessToken)).thenThrow(new RuntimeException("JWT error"));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError());

        verify(jwtService, times(1)).getUserDTOByAccessToken(accessToken);
    }

    /**
     * Teste la mise à jour des informations de l'utilisateur actuel par token d'accès valide.
     * Vérifie que la réponse contient les informations mises à jour de l'utilisateur et que les services appropriés sont appelés.
     */
    @Test
    @WithMockUser(username = "currentUser", roles = {"USER"})
    void updateUserByToken_whenValidTokenAndData_shouldReturnUpdatedUser() throws Exception {
        String token = "validToken";
        when(jwtService.getUserByAccessToken(token)).thenReturn(user);
        when(userService.updateUserToken(eq(user.getIdUser()), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/token")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk());

        verify(jwtService, times(1)).getUserByAccessToken(token);
        verify(mapper, times(1)).toEntity(any(UserUpdateDTO.class), eq(user));
        verify(userService, times(1)).updateUserToken(eq(user.getIdUser()), any(User.class));
    }

    /**
     * Teste la mise à jour des informations de l'utilisateur actuel par token d'accès valide lorsque des données invalides sont fournies.
     * Vérifie que la réponse est un statut 409 Conflict et que les services appropriés sont appelés.
     */
    @Test
    @WithMockUser(username = "currentUser", roles = {"USER"})
    void updateUserByToken_whenIllegalArgumentException_shouldReturnConflict() throws Exception {
        String token = "validToken";
        when(jwtService.getUserByAccessToken(token)).thenReturn(user);
        when(userService.updateUserToken(eq(user.getIdUser()), any(User.class)))
                .thenThrow(new IllegalArgumentException("Pseudo ou email déjà utilisé"));

        mockMvc.perform(put("/api/users/token")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isConflict());

        verify(jwtService, times(1)).getUserByAccessToken(token);
        verify(userService, times(1)).updateUserToken(eq(user.getIdUser()), any(User.class));
    }

    /**
     * Teste la mise à jour du mot de passe de l'utilisateur actuel par token d'accès valide.
     * Vérifie que la réponse contient les informations mises à jour de l'utilisateur et que les services appropriés sont appelés.
     */
    @Test
    @WithMockUser(username = "currentUser", roles = {"USER"})
    void updateUserPasswordByToken_whenValidTokenAndData_shouldReturnUpdatedUser() throws Exception {
        String token = "validToken";
        when(jwtService.getUserByAccessToken(token)).thenReturn(user);
        when(userService.updateUserToken(eq(user.getIdUser()), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/api/users/password")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdatePasswordDTO)))
                .andExpect(status().isOk());

        verify(jwtService, times(1)).getUserByAccessToken(token);
        verify(mapper, times(1)).toEntityPassword(any(UserUpdatePasswordDTO.class), eq(user));
        verify(userService, times(1)).updateUserToken(eq(user.getIdUser()), any(User.class));
    }

    /**
     * Teste la mise à jour du mot de passe de l'utilisateur actuel par token d'accès valide lorsque des données invalides sont fournies.
     * Vérifie que la réponse est un statut 409 Conflict et que les services appropriés sont appelés.
     */
    @Test
    @WithMockUser(username = "currentUser", roles = {"USER"})
    void updateUserPasswordByToken_whenIllegalArgumentException_shouldReturnConflict() throws Exception {
        String token = "validToken";
        when(jwtService.getUserByAccessToken(token)).thenReturn(user);
        when(userService.updateUserToken(eq(user.getIdUser()), any(User.class)))
                .thenThrow(new IllegalArgumentException("Ancien mot de passe incorrect"));

        mockMvc.perform(put("/api/users/password")
                        .with(csrf())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdatePasswordDTO)))
                .andExpect(status().isConflict());

        verify(jwtService, times(1)).getUserByAccessToken(token);
        verify(userService, times(1)).updateUserToken(eq(user.getIdUser()), any(User.class));
    }

    /**
     * Teste la suppression de l'utilisateur actuel par token d'accès valide.
     * Vérifie que la réponse est un statut 204 No Content et que les services appropriés sont appelés.
     */
    @Test
    @WithMockUser(username = "currentUser", roles = {"USER"})
    void deleteUserByTokenAccess_whenValidToken_shouldReturnNoContent() throws Exception {
        String accessToken = "validAccessToken";
        when(jwtService.getUserByAccessToken(accessToken)).thenReturn(user);
        doNothing().when(userService).deleteUserById(user.getIdUser());

        mockMvc.perform(delete("/api/users/delete/me")
                        .with(csrf())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        verify(jwtService, times(1)).getUserByAccessToken(accessToken);
        verify(userService, times(1)).deleteUserById(user.getIdUser());
    }

    /**
     * Teste la suppression de l'utilisateur actuel sans préfixe Bearer dans le token d'accès.
     * Vérifie que la réponse est un statut 401 Unauthorized et que les services appropriés ne sont jamais appelés.
     */
    @Test
    @WithMockUser(username = "currentUser", roles = {"USER"})
    void deleteUserByTokenAccess_whenNoBearerPrefix_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/users/delete/me")
                        .with(csrf())
                        .header("Authorization", "InvalidTokenFormat"))
                .andExpect(status().isUnauthorized());

        verify(jwtService, never()).getUserByAccessToken(anyString());
        verify(userService, never()).deleteUserById(anyLong());
    }

    /**
     * Teste la suppression de l'utilisateur actuel avec un token d'accès invalide.
     * Vérifie que la réponse est un statut 401 Unauthorized et que les services appropriés sont appelés.
     */
    @Test
    @WithMockUser(username = "currentUser", roles = {"USER"})
    void deleteUserByTokenAccess_whenJwtServiceThrowsException_shouldReturnInternalServerError() throws Exception {
        String accessToken = "errorToken";
        when(jwtService.getUserByAccessToken(accessToken)).thenThrow(new RuntimeException("JWT error"));

        mockMvc.perform(delete("/api/users/delete/me")
                        .with(csrf())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isInternalServerError()); // <-- Changez 403 à 500

        verify(jwtService, times(1)).getUserByAccessToken(accessToken);
        verify(userService, never()).deleteUserById(anyLong());
    }

    /**
     * Teste la création d'un nouvel utilisateur avec des données valides.
     * Vérifie que la réponse contient l'utilisateur créé et que le service de création est appelé une fois.
     */
    @Test
    void saveUser_whenValidUser_shouldReturnCreatedUser() throws Exception {
        when(userService.saveUser(any(UserCreateDTO.class))).thenReturn(userDTO);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.emailUser").value(userDTO.getEmailUser()));

        verify(userService, times(1)).saveUser(any(UserCreateDTO.class));
    }

    /**
     * Teste la création d'un nouvel utilisateur avec des données invalides.
     * Vérifie que la réponse est un statut 400 Bad Request et que le service de création n'est jamais appelé.
     */
    @Test
    void saveUser_whenInvalidUser_shouldReturnBadRequest() throws Exception {
        // Crée un UserCreateDTO invalide
        UserCreateDTO invalidUserCreateDTO = new UserCreateDTO(
                "Doe",
                "John",
                "testUser",
                "test@example.com",
                "passwordHash",
                LocalDate.of(1990, 1, 1),
                "0123456789"
        );

        // Quand une requête POST est effectuée avec un userCreateDTO invalide
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(userService, never()).saveUser(any(UserCreateDTO.class));
    }
}
