package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface de gestion des opérations CRUD pour les options.
 * Hérite de JpaRepository pour les opérations de base de données.
 */
@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
}
