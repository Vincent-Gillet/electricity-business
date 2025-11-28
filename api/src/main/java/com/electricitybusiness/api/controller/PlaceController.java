package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.place.PlaceCreateDTO;
import com.electricitybusiness.api.dto.place.PlaceDTO;
import com.electricitybusiness.api.dto.place.PlaceUpdateDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Place;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.service.PlaceService;
import com.electricitybusiness.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
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
    private final UserService userService;

    /**
     * Récupère tous les lieux.
     * GET /api/places
     * @return Une liste de tous les lieux
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
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
    @PreAuthorize("hasAuthority('ADMIN')")
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
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("hasAuthority('ADMIN')")
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
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deletePlaceById(@PathVariable Long id) {
        if (!placeService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        placeService.deletePlaceById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère tous les lieux associés à l'utilisateur authentifié.
     * GET /api/places/user
     * @return Une liste de lieux associés à l'utilisateur
     */
    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PlaceDTO>> getAllPlacesByUser() {
        // Récupérer l'utilisateur authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long idUser = userService.getIdByEmailUser(email);
        User user = userService.getUserById(idUser);

        // Récupérer les voitures de l'utilisateur
        List<Place> places = placeService.getPlacesByUser(user);
        List<PlaceDTO> PlacesDTO = places.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(PlacesDTO);
    }

    @PostMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlaceDTO> savePlaceByToken(@Valid @RequestBody PlaceCreateDTO placeDTO) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Long idUser = userService.getIdByEmailUser(email);

            Place place = mapper.toEntityCreate(placeDTO, idUser, placeDTO.getPublicIdAddress());
            Place savedPlace = placeService.savePlace(place);

            PlaceDTO savedDTO = mapper.toDTO(savedPlace);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime un lieu par son identifiant public.
     * DELETE /api/places/publicId/{publicId}
     * @param publicId L'identifiant public du lieu à supprimer
     * @return Un statut HTTP 204 No Content si la suppression est réussie, ou 404 Not Found si le lieu n'existe pas
     */
    @DeleteMapping("publicId/{publicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePlace(@PathVariable UUID publicId) {
        if (!placeService.existsByPublicId(publicId)) {
            return ResponseEntity.notFound().build();
        }
        placeService.deletePlaceByPublicId(publicId);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/publicId/{publicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PlaceDTO> updatePlace(
            @PathVariable UUID publicId,
            @Valid @RequestBody PlaceUpdateDTO placeDTO
    ) {
        if (!placeService.existsByPublicId(publicId)) {
            return ResponseEntity.notFound().build();
        }
        // Récupérer l'utilisateur authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long idUser = userService.getIdByEmailUser(email);

        // Mettre à jour la voiture
        Place place = mapper.toEntityUpdate(placeDTO, idUser, placeDTO.getPublicIdAddress());
        Place updatedPlace = placeService.updatePlace(publicId, place);
        PlaceDTO updatedDTO = mapper.toDTO(updatedPlace);
        return ResponseEntity.ok(updatedDTO);
    }

}
