package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Media;
import com.electricitybusiness.api.repository.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les opérations liées aux médias.
 * Fournit des méthodes pour récupérer, créer, mettre à jour et supprimer des médias.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MediaService {

    private final MediaRepository mediaRepository;

    /**
     * Récupère tous les medias.
     * @return Une liste de tous les medias
     */
    public List<Media> getAllMedias() { return mediaRepository.findAll(); }

    /**
     * Récupère un media par son ID.
     * @param id L'identifiant du media à récupérer
     * @return Un Optional contenant le media si trouvé, sinon vide
     */
    public Optional<Media> getMediaById(Long id) { return mediaRepository.findById(id); }

    /**
     * Crée un nouveau media.
     * @param media Le media à enregistrer
     * @return Le media enregistré
     */
    @Transactional
    public Media saveMedia(Media media) {
        return mediaRepository.save(media);
    }

    /**
     * Met à jour un media existant.
     * @param id L'identifiant du media à mettre à jour
     * @param media Le media avec les nouvelles informations
     * @return Le media mis à jour
     */
    public Media updateMedia(Long id, Media media) {
        media.setIdMedia(id);
        return mediaRepository.save(media);
    }

    /**
     * Supprime un media.
     * @param id L'identifiant du media à supprimer
     */
    public void deleteMediaById(Long id) {
        mediaRepository.deleteById(id);
    }

    /**
     * Vérifie si un media existe.
     * @param id L'identifiant du media à vérifier
     * @return true si le media existe, false sinon
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return mediaRepository.existsById(id);
    }

    /**
     * Récupère tous les médias d'un type spécifique.
     * @param type Le type de média à rechercher
     * @return Une liste de médias correspondant au type spécifié
     */
    @Transactional(readOnly = true)
    public List<Media> findByType(String type) {
        return mediaRepository.findByType(type);
    }
}
