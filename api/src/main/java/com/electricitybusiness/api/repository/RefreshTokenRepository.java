package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByIdRefreshToken(String idRefreshToken);

}
