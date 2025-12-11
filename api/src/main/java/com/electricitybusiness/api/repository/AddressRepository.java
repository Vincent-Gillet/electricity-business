package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.Address;
import com.electricitybusiness.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de gestion des opérations CRUD pour les adresses.
 * Hérite de JpaRepository pour les opérations de base de données.
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAddressesByUser(User user);

    void deleteAddressByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    Optional<Address> findByPublicId(UUID publicId);
}
