package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.user.UserCreateDTO;
import com.electricitybusiness.api.dto.user.UserDTO;
import com.electricitybusiness.api.dto.user.UserUpdateDTO;
import com.electricitybusiness.api.exception.ConflictException;
import com.electricitybusiness.api.exception.ResourceNotFoundException;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service pour gérer les opérations liées aux Users.
 * Fournit des méthodes pour récupérer, créer, mettre à jour et supprimer des Users.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;

    private final EntityMapper entityMapper;

    /**
     * Récupère tous les Users.
     * @return Une liste de tous les Users
     */
/*    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }*/

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(entityMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un User par son ID.
     * @param id L'identifiant de l'User à récupérer
     * @return Le User trouvé
     * @throws ResourceNotFoundException si l'User n'est pas trouvé
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));
    }

    /**
     * Crée un nouveau User.
     * @param userDTO L'User à enregistrer
     * @return L'User enregistré
     */
/*    public User saveUser(User user) {
        return userRepository.save(user);
    }*/
    public UserDTO saveUser(UserCreateDTO userDTO) {
        User user = entityMapper.toEntity(userDTO);
        User savedUser = userRepository.save(user);
        return entityMapper.toDTO(savedUser);
    }

    /**
     * Met à jour un User existant.
     * @param id L'identifiant de l'User à mettre à jour
     * @param user L'User avec les nouvelles informations
     * @return L'User mis à jour
     */
    public User updateUser(Long id, User user) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        user.setIdUser(id);
        return userRepository.save(user);
    }

    /**
     * Supprime un User.
     * @param id L'identifiant de l'User à supprimer
     * @throws ResourceNotFoundException si l'User n'est pas trouvé
     */
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    /**
     * Récupère un User par son pseudo.
     * @param pseudo Le pseudo de l'User à récupérer
     * @return Un Optional contenant l'User si trouvé, sinon vide
     */
    @Transactional(readOnly = true)
    public Optional<User> findByPseudo(String pseudo) {
        return userRepository.findByPseudo(pseudo);
    }

    /**
     * Récupère un User par son adresse email.
     * @param emailUser L'adresse email de l'User à récupérer
     * @return Le User trouvé
     * @throws ResourceNotFoundException si l'User n'est pas trouvé
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String emailUser) {
        return userRepository.findByEmailUser(emailUser)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + emailUser));
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
     * @param pseudo Le pseudo à vérifier
     * @return true si le pseudo existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsByPseudo(String pseudo) {
        return userRepository.existsByPseudo(pseudo);
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
/*    @Transactional(readOnly = true)
    public Long getIdByEmailUser(String emailUser) {
        return userRepository.findIdByEmailUser(emailUser);
    }*/

    /**
     * Récupère l'identifiant d'un User par son adresse email.
     * @param emailUser L'adresse email de l'User
     * @return L'identifiant de l'User
     * @throws ResourceNotFoundException si l'User n'est pas trouvé
     */
    @Transactional(readOnly = true)
    public Long getIdByEmailUser(String emailUser) {
        return userRepository.findByEmailUser(emailUser)
                .map(User::getIdUser)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email : " + emailUser));
    }

    /**
     * Met à jour les informations d'un User de manière partielle.
     * Seules les propriétés non nulles de l'User entrant seront mises à jour.
     * @param id L'identifiant de l'User à mettre à jour
     * @param incoming L'User avec les nouvelles informations
     * @return L'User mis à jour
     * @throws ResourceNotFoundException si l'User n'est pas trouvé
     * @throws ConflictException si l'adresse email est déjà utilisée par un autre User
     */
    @Transactional
/*    public User updateUserToken(Long id, User incoming) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : " + id));

        if (incoming.getSurnameUser() != null) existing.setSurnameUser(incoming.getSurnameUser());
        if (incoming.getFirstName() != null) existing.setFirstName(incoming.getFirstName());
        if (incoming.getPseudo() != null) existing.setPseudo(incoming.getPseudo());
        if (incoming.getDateOfBirth() != null) existing.setDateOfBirth(incoming.getDateOfBirth());
        if (incoming.getPhone() != null) existing.setPhone(incoming.getPhone());
        if (incoming.getIban() != null) existing.setIban(incoming.getIban());

        String newEmail = incoming.getEmailUser();
        if (newEmail != null && !newEmail.equals(existing.getEmailUser())) {
            if (userRepository.existsByEmailUser(newEmail)) {
                throw new ConflictException("Email already in use : " + newEmail);
            }
            existing.setEmailUser(newEmail);
        }

        if (incoming.getPasswordUser() != null && !incoming.getPasswordUser().isBlank()) {
            existing.setPasswordUser(incoming.getPasswordUser());
        }

        return userRepository.save(existing);
    }*/
    public User updateUserToken(Long id, User incoming) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : " + id));

        String newEmail = incoming.getEmailUser();
        if (newEmail != null && !newEmail.equals(existing.getEmailUser())) {
            if (userRepository.existsByEmailUser(newEmail)) {
                throw new ConflictException("Email already in use : " + newEmail);
            }
            existing.setEmailUser(newEmail);
        }

        return userRepository.save(existing);
    }
}
