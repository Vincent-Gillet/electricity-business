package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.Repairer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepairerRepository extends JpaRepository<Repairer, Long> {
    Optional<Repairer> findByEmailRepairer(String email);
}
