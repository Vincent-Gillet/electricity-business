package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.repairer.RepairerDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Repairer;
import com.electricitybusiness.api.service.RepairerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des réparateurs.
 * Expose les endpoints pour les opérations CRUD sur les réparateurs.
 */
@RestController
@RequestMapping("/api/repairers")
@RequiredArgsConstructor
public class RepairerController {

    private final RepairerService repairerService;
    private final EntityMapper mapper;

    /**
     * Récupère tous les réparateurs.
     * GET /api/repairers
     * @return Une liste de tous les réparateurs
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<RepairerDTO>> getAllRepairers() {
        List<Repairer> repairers = repairerService.getAllRepairers();
        List<RepairerDTO> repairersDTO = repairers.stream()
                .map(mapper::toRepairerDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(repairersDTO);
    }

    /**
     * Récupère un réparateur par son ID.
     * GET /api/repairers/{id}
     * @param id L'identifiant du réparateur à récupérer
     * @return Le réparateur correspondant à l'ID, ou un statut HTTP 404 Not Found si non trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<RepairerDTO> getRepaireById(@PathVariable Long id) {
        return repairerService.getRepairerById(id)
                .map(reparateur -> ResponseEntity.ok(mapper.toRepairerDTO(reparateur)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée un nouveau réparateur.
     * POST /api/repairers
     * @param repairerDTO Le réparateur à créer
     * @return Le réparateur créé avec un statut HTTP 201 Created
     */
    @PostMapping("/repairers")
    public ResponseEntity<RepairerDTO> saveRepaire(@Valid @RequestBody RepairerDTO repairerDTO) {
        Repairer repairer = mapper.toEntity(repairerDTO);
        Repairer savedRepairer = repairerService.saveRepairer(repairer);
        RepairerDTO savedDTO = mapper.toRepairerDTO(savedRepairer);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }

    /**
     * Met à jour un réparateur existant.
     * PUT /api/repairers/{id}
     * @param id L'identifiant du réparateur à mettre à jour
     * @param repairerDTO Le réparateur avec les nouvelles informations
     * @return Le réparateur mis à jour, ou un statut HTTP 404 Not Found si l'ID n'existe pas
     */
    @PutMapping("/{id}")
    public ResponseEntity<RepairerDTO> updateReparateur(@PathVariable Long id, @Valid @RequestBody RepairerDTO repairerDTO) {
        if (!repairerService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Repairer repairer = mapper.toEntity(repairerDTO);
        Repairer updatedRepairer = repairerService.updateRepairer(id, repairer);
        RepairerDTO updatedDTO = mapper.toRepairerDTO(updatedRepairer);
        return ResponseEntity.ok(updatedDTO);
    }

    /**
     * Supprime un réparateur par son ID.
     * DELETE /api/repairers/{id}
     * @param id L'identifiant du réparateur à supprimer
     * @return Un statut HTTP 204 No Content si la suppression est réussie, ou 404 Not Found si l'ID n'existe pas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReparateurById(@PathVariable Long id) {
        if (!repairerService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repairerService.deleteRepairerById(id);
        return ResponseEntity.noContent().build();
    }
}
