package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Interface de gestion des opérations CRUD pour les utilisateurs.
 * Hérite de JpaRepository pour les opérations de base de données.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPseudo(String pseudo);

    Optional<User> findByEmailUser(String emailUser);

    boolean existsByPseudo(String pseudo);

    boolean existsByEmailUser(String emailUser);

    Long findIdByEmailUser(String emailUser);
}
