package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.MediaDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Media;
import com.electricitybusiness.api.service.MediaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<MediaDTO> saveMedia(@Valid @RequestBody MediaDTO mediaDTO) {
        Media media = mapper.toEntity(mediaDTO);
        Media savedMedia = mediaService.saveMedia(media);
        MediaDTO savedDTO = mapper.toDTO(savedMedia);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
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
