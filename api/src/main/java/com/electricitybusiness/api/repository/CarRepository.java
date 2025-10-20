package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.Car;
import com.electricitybusiness.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de gestion des opérations CRUD pour les voitures.
 * Hérite de JpaRepository pour les opérations de base de données.
 */
@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findCarsByUser(User user);

    void deleteCarByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    Optional<Car> findByPublicId(UUID publicId);

}
