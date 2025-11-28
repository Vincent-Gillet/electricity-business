package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Place;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour gérer les opérations liées aux Placex.
 * Fournit des méthodes pour récupérer, créer, mettre à jour et supprimer des Placex.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PlaceService {
    private final PlaceRepository placeRepository;

    /**
     * Récupère tous les vehicules.
     * @return Une liste de tous les Placex
     */
    public List<Place> getAllPlaces() {
        return placeRepository.findAll();
    }

    /**
     * Récupère un Place par son ID.
     * @param id L'identifiant du Place à récupérer
     * @return Un Optional contenant le Place si trouvé, sinon vide
     */
    public Optional<Place> getPlaceById(Long id) {
        return placeRepository.findById(id);
    }

    /**
     * Crée un nouveau Place.
     * @param Place Le Place à enregistrer
     * @return Le Place enregistré
     */
    @Transactional
    public Place savePlace(Place Place) {
        return placeRepository.save(Place);
    }

    /**
     * Met à jour un Place existant.
     * @param id L'identifiant du Place à mettre à jour
     * @param place Le Place avec les nouvelles informations
     * @return Le Place mis à jour
     */
    public Place updatePlace(Long id, Place place) {
        place.setIdPlace(id);
        return placeRepository.save(place);
    }

    /**
     * Supprime un utilisateur.
     * @param id L'identifiant du Place à supprimer
     */
    public void deletePlaceById(Long id) {
        placeRepository.deleteById(id);
    }


    /**
     * Vérifie si un vehicule existe.
     * @param id L'identifiant du vehicule à vérifier
     * @return true si le Place existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return placeRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public List<Place> getPlacesByUser(User user) { return placeRepository.findPlacesByUser(user); }

    public void deletePlaceByPublicId(UUID publicId) {
        placeRepository.deletePlaceByPublicId(publicId);
    }

    @Transactional(readOnly = true)
    public boolean existsByPublicId(UUID publicId) {
        return placeRepository.findByPublicId(publicId).isPresent();
    }

    public Place updatePlace(UUID publicId, Place place) {
        Place existing = placeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Place with publicId not found: " + publicId));

        place.setIdPlace(existing.getIdPlace());
        place.setPublicId(existing.getPublicId());

        User existingUser = existing.getUser();
        if (place.getUser() == null) {
            place.setUser(existingUser);
        }
        return placeRepository.save(place);
    }
}
