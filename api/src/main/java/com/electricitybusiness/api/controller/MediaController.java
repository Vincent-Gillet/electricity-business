package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.MediaDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Media;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.service.MediaService;
import com.electricitybusiness.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des médias.
 * Expose les endpoints pour les opérations CRUD sur les médias.
 */
@RestController
@RequestMapping("/api/medias")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaService;
    private final EntityMapper mapper;

    /**
     * Récupère tous les médias.
     * GET /api/medias
     * @return Une liste de tous les médias
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<MediaDTO>> getAllMedias() {
        List<Media> medias = mediaService.getAllMedias();
        List<MediaDTO> mediasDTO = medias.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mediasDTO);    }

    /**
     * Récupère un média par son ID.
     * GET /api/medias/{id}
     * @param id L'identifiant du média à récupérer
     * @return Le média correspondant à l'ID, ou un statut HTTP 404 Not Found si non trouvé
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<MediaDTO> getMediaById(@PathVariable Long id) {
        return mediaService.getMediaById(id)
                .map(medias -> ResponseEntity.ok(mapper.toDTO(medias)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée un nouveau média.
     * POST /api/medias
     * @param mediaDTO Le média à créer
     * @return Le média créé avec un statut HTTP 201 Created
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<MediaDTO> saveMedia(@Valid @RequestBody MediaDTO mediaDTO) {
        Media media = mapper.toEntity(mediaDTO);
        Media savedMedia = mediaService.saveMedia(media);
        MediaDTO savedDTO = mapper.toDTO(savedMedia);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }

    private final UserService userService;

    @PostMapping("/profil")
    public ResponseEntity<MediaDTO> createMedia(@RequestBody MediaDTO mediaDTO, Principal principal) {
        String email = principal.getName(); // Get email from JWT
        User user = userService.getUserByEmail(email);
        mediaDTO.setIdUser(user.getIdUser()); // Associate media with user
        Media media = mapper.toEntity(mediaDTO); // Convert DTO to entity
        media.setUser(user); // Associate with user
        Media savedMedia = mediaService.saveMedia(media); // Save entity
        MediaDTO responseDTO = mapper.toDTO(savedMedia); // Convert back to DTO for response
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    /**
     * Met à jour un média existant.
     * PUT /api/medias/{id}
     * @param id L'identifiant du média à mettre à jour
     * @param mediaDTO Le média avec les nouvelles informations
     * @return Le média mis à jour, ou un statut HTTP 404 Not Found si non trouvé
     */
    @PutMapping("/{id}")
    public ResponseEntity<MediaDTO> updateMedia(@PathVariable Long id, @Valid @RequestBody MediaDTO mediaDTO) {
        if (!mediaService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Media media = mapper.toEntity(mediaDTO);
        Media updatedMedia = mediaService.updateMedia(id, media);
        MediaDTO updatedDTO = mapper.toDTO(updatedMedia);
        return ResponseEntity.ok(updatedDTO);
    }

    @PutMapping("/profil/update/{id}")
    public ResponseEntity<MediaDTO> updateMedia(@PathVariable Long id, @RequestBody MediaDTO mediaDTO, Principal principal) {
        String email = principal.getName();
        userService.getUserByEmail(email);
        Media existingMedia = mediaService.getMediaById(id)
                .orElseThrow(() -> new RuntimeException("Media not found"));
        Media updatedMediaEntity = mapper.toEntity(mediaDTO, existingMedia);
        // Do NOT set updatedMediaEntity.setUser(user);
        Media updatedMedia = mediaService.updateMedia(id, updatedMediaEntity);
        MediaDTO responseDTO = mapper.toDTO(updatedMedia);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Supprime un média par son ID.
     * DELETE /api/medias/{id}
     * @param id L'identifiant du média à supprimer
     * @return Un statut HTTP 204 No Content si la suppression est réussie, ou 404 Not Found si l'ID n'existe pas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMediaById(@PathVariable Long id) {
        if (!mediaService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        mediaService.deleteMediaById(id);
        return ResponseEntity.noContent().build();
    }
}
