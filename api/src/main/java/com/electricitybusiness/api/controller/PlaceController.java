package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.PlaceDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Place;
import com.electricitybusiness.api.service.PlaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des lieux.
 * Expose les endpoints pour les opérations CRUD sur les lieux.
 */
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final EntityMapper mapper;

    /**
     * Récupère tous les lieux.
     * GET /api/places
     * @return Une liste de tous les lieux
     */
    @GetMapping
    public ResponseEntity<List<PlaceDTO>> getAllLieux() {
        List<Place> places = placeService.getAllPlaces();
        List<PlaceDTO> lieuxDTO = places.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(lieuxDTO);
    }

    /**
     * Récupère un lieu par son ID.
     * GET /api/places/{id}
     * @param id L'identifiant du lieu à récupérer
     * @return Le lieu correspondant à l'ID, ou un statut HTTP 404 Not Found si non trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlaceDTO> getLieuById(@PathVariable Long id) {
        return placeService.getPlaceById(id)
                .map(lieu -> ResponseEntity.ok(mapper.toDTO(lieu)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée un nouveau lieu.
     * POST /api/places
     * @param placeDTO Le lieu à créer
     * @return Le lieu créé avec un statut HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<PlaceDTO> saveLieu(@Valid @RequestBody PlaceDTO placeDTO) {
        Place place = mapper.toEntity(placeDTO);
        Place savedPlace = placeService.savePlace(place);
        PlaceDTO savedDTO = mapper.toDTO(savedPlace);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }

    /**
     * Met à jour un lieu existant.
     * PUT /api/places/{id}
     * @param id L'identifiant du lieu à mettre à jour
     * @param placeDTO Le lieu avec les nouvelles informations
     * @return Le lieu mis à jour, ou un statut HTTP 404 Not Found si non trouvé
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlaceDTO> updatePlace(@PathVariable Long id, @Valid @RequestBody PlaceDTO placeDTO) {
        if (!placeService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Place place = mapper.toEntity(placeDTO);
        Place updatedPlace = placeService.updatePlace(id, place);
        PlaceDTO updatedDTO = mapper.toDTO(updatedPlace);
        return ResponseEntity.ok(updatedDTO);
    }

    /**
     * Supprime un lieu par son ID.
     * DELETE /api/places/{id}
     * @param id L'identifiant du lieu à supprimer
     * @return Un statut HTTP 204 No Content si la suppression est réussie, ou 404 Not Found si le lieu n'existe pas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlaceById(@PathVariable Long id) {
        if (!placeService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        placeService.deletePlaceById(id);
        return ResponseEntity.noContent().build();
    }
}
