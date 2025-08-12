package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les opérations liées aux Users.
 * Fournit des méthodes pour récupérer, créer, mettre à jour et supprimer des Users.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

    /**
     * Récupère tous les Users.
     * @return Une liste de tous les Users
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Récupère un User par son ID.
     * @param id L'identifiant de l'User à récupérer
     * @return Un Optional contenant l'User si trouvé, sinon vide
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Crée un nouveau User.
     * @param User L'User à enregistrer
     * @return L'User enregistré
     */
    public User saveUser(User User) {
        return userRepository.save(User);
    }

    /**
     * Met à jour un User existant.
     * @param id L'identifiant de l'User à mettre à jour
     * @param User L'User avec les nouvelles informations
     * @return L'User mis à jour
     */
    public User updateUser(Long id, User User) {
        User.setIdUser(id);
        return userRepository.save(User);
    }

    /**
     * Supprime un User.
     * @param id L'identifiant de l'User à supprimer
     */
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Récupère un User par son pseudo.
     * @param username Le pseudo de l'User à récupérer
     * @return Un Optional contenant l'User si trouvé, sinon vide
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Récupère un User par son adresse email.
     * @param emailUser L'adresse email de l'User à récupérer
     * @return Un Optional contenant l'User si trouvé, sinon vide
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUserEmail(String emailUser) {
        return userRepository.findByEmailUser(emailUser);
    }

    /**
     * Vérifie si un User existe.
     * @param id L'identifiant de l'User à vérifier
     * @return true si l'User existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    /**
     * Vérifie si un pseudo est déjà utilisé par un User.
     * @param username Le pseudo à vérifier
     * @return true si le pseudo existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Vérifie si une adresse email est déjà utilisée par un User.
     * @param emailUser L'adresse email à vérifier
     * @return true si l'adresse email existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsByEmailUser(String emailUser) {
        return userRepository.existsByEmailUser(emailUser);
    }

    /**
     * Récupère l'identifiant d'un User par son adresse email.
     * @param emailUser L'adresse email de l'User
     * @return L'identifiant de l'User si trouvé, sinon null
     */
    @Transactional(readOnly = true)
    public Long getIdByEmailUser(String emailUser) {
        return userRepository.findIdByEmailUser(emailUser);
    }
}
