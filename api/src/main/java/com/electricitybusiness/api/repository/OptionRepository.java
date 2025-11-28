package com.electricitybusiness.api.repository;

import com.electricitybusiness.api.model.Option;
import com.electricitybusiness.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de gestion des opérations CRUD pour les options.
 * Hérite de JpaRepository pour les opérations de base de données.
 */
@Repository
public interface OptionRepository extends JpaRepository<Option, Long> {
    List<Option> findOptionsByPlace_PublicId(UUID publicId);

    List<Option> findOptionByPlace_User(User user);

    void deleteOptionByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    Optional<Option> findByPublicId(UUID publicId);

/*
    List<Option> findOptionsByPlace_Id_Place_TerminalId_UUID(UUID terminalId);
*/
/*
    List<Option> findOptionsByPlace_Terminal_PublicId(UUID terminalId);
*/
/*
    @Query("SELECT o FROM Option o JOIN Place p ON o.place.idPlace = p.idPlace JOIN Terminal t ON p.idPlace = t.place.idPlace WHERE t.publicId = :idTerminal")
*/
/*
    @Query("SELECT o FROM Option o, Terminal t WHERE t.place = o.place AND t.publicId = :terminalId")
*/
    @Query("SELECT o FROM Option o WHERE o.place = (SELECT t.place FROM Terminal t WHERE t.publicId = :terminalId)")
    List<Option> findByTerminalPublicId(@Param("terminalId") UUID terminalId);

}
