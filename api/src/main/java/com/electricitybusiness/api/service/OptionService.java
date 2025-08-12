package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Option;
import com.electricitybusiness.api.repository.OptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
}
