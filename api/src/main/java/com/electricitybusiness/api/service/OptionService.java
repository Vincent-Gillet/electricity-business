package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Car;
import com.electricitybusiness.api.model.Option;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.repository.OptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour gérer les opérations liées aux options.
 * Fournit des méthodes pour récupérer, créer, mettre à jour et supprimer des options.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OptionService {
    private final OptionRepository optionRepository;

    /**
     * Récupère tous les options.
     * @return Une liste de tous les options
     */
    public List<Option> getAllOptions() {
        return optionRepository.findAll();
    }

    /**
     * Récupère une option par son ID.
     * @param id L'identifiant de l'option à récupérer
     * @return Un Optional contenant l'option si trouvé, sinon vide
     */
    public Optional<Option> getOptionById(Long id) {
        return optionRepository.findById(id);
    }

    /**
     * Crée une nouvelle option.
     * @param option L'option à enregistrer
     * @return L'option enregistré
     */
    @Transactional
    public Option saveOption(Option option) {
        return optionRepository.save(option);
    }

    /**
     * Met à jour une option existant.
     * @param id L'identifiant de l'option à mettre à jour
     * @param option L'option avec les nouvelles informations
     * @return L'option mis à jour
     */
    public Option updateOption(Long id, Option option) {
        option.setIdOption(id);
        return optionRepository.save(option);
    }

    /**
     * Supprime une option par son ID.
     * @param id L'identifiant de l'option à supprimer
     */
    public void deleteOptionById(Long id) {
        optionRepository.deleteById(id);
    }


    /**
     * Vérifie si une option existe par son ID.
     * @param id L'identifiant de l'option à vérifier
     * @return true si l'option existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return optionRepository.existsById(id);
    }

    // Méthode user

    @Transactional(readOnly = true)
    public List<Option> getOptionsByPlace(UUID place) { return optionRepository.findOptionsByPlace_PublicId(place); }

    @Transactional(readOnly = true)
    public List<Option> getOptionsByUser(User user) { return optionRepository.findOptionByPlace_User(user); }

    @Transactional(readOnly = true)
    public List<Option> getOptionsByTerminal(UUID terminalId) { return optionRepository.findByTerminalPublicId(terminalId); }

    /**
     * Supprime une voiture.
     * @param publicId L'identifiant de la voiture à supprimer
     */
    public void deleteOptionByPublicId(UUID publicId) {
        optionRepository.deleteOptionByPublicId(publicId);
    }

    /**
     * Vérifie si une voiture existe.
     * @param publicId L'identifiant de la voiture à vérifier
     * @return true si la voiture existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsByPublicId(UUID publicId) {
        return optionRepository.findByPublicId(publicId).isPresent();
    }

    /**
     * Met à jour une voiture existant.
     * @param publicId L'identifiant de la voiture à mettre à jour
     * @param option La voiture avec les nouvelles informations
     * @return La voiture mis à jour
     */
    public Option updateOption(UUID publicId, Option option) {
        Option existing = optionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Option with publicId not found: " + publicId));

        option.setIdOption(existing.getIdOption());
        option.setPublicId(existing.getPublicId());

        return optionRepository.save(option);
    }

}
