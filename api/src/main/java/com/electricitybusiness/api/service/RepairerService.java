package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Repairer;
import com.electricitybusiness.api.repository.RepairerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les opérations liées aux Repairers.
 * Fournit des méthodes pour récupérer, créer, mettre à jour et supprimer des Repairers.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class RepairerService {
    private final RepairerRepository repairerRepository;

    /**
     * Récupère tous les Repairers.
     * @return Une liste de tous les Repairers
     */
    public List<Repairer> getAllRepairers() {
        return repairerRepository.findAll();
    }

    /**
     * Récupère un Repairer par son ID.
     * @param id L'identifiant du Repairer à récupérer
     * @return Un Optional contenant le Repairer si trouvé, sinon vide
     */
    public Optional<Repairer> getRepairerById(Long id) {
        return repairerRepository.findById(id);
    }

    /**
     * Crée un nouveau Repairer.
     * @param repairer Le Repairer à enregistrer
     * @return Le Repairer enregistré
     */
    @Transactional
    public Repairer saveRepairer(Repairer repairer) {
        return repairerRepository.save(repairer);
    }

    /**
     * Met à jour un Repairer existant.
     * @param id L'identifiant du Repairer à mettre à jour
     * @param repairer Le Repairer avec les nouvelles informations
     * @return Le Repairer mis à jour
     */
    public Repairer updateRepairer(Long id, Repairer repairer) {
        repairer.setIdRepairer(id);
        return repairerRepository.save(repairer);
    }

    /**
     * Supprime un Repairer.
     * @param id L'identifiant du Repairer à supprimer
     */
    public void deleteRepairerById(Long id) {
        repairerRepository.deleteById(id);
    }

    /**
     * Vérifie si un Repairer existe.
     * @param id L'identifiant du Repairer à vérifier
     * @return true si le Repairer existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return repairerRepository.existsById(id);
    }
}
