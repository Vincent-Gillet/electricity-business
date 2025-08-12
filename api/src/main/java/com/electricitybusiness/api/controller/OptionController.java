package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.OptionDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Option;
import com.electricitybusiness.api.service.OptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/options")
@RequiredArgsConstructor
public class OptionController {
    private final OptionService optionService;
    private final EntityMapper mapper;

    /**
     * Récupère toutes les options.
     * GET /api/options
     * @return Une liste de toutes les options
     */
    @GetMapping
    public ResponseEntity<List<OptionDTO>> getAllOptions() {
        List<Option> options = optionService.getAllOptions();
        List<OptionDTO> optionsDTO = options.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(optionsDTO);
    }

    /**
     * Récupère une option par son ID.
     * GET /api/options/{id}
     * @param id L'identifiant de l'option à récupérer
     * @return L'option correspondant à l'ID, ou un statut HTTP 404 Not Found si non trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<OptionDTO> getOptionById(@PathVariable Long id) {
        return optionService.getOptionById(id)
                .map(optionService -> ResponseEntity.ok(mapper.toDTO(optionService)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée une nouvelle option.
     * POST /api/options
     * @param optionDTO L'option à créer
     * @return L'option créé avec un statut HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<OptionDTO> saveOption(@Valid @RequestBody OptionDTO optionDTO) {
        Option option = mapper.toEntity(optionDTO);
        Option savedOption = optionService.saveOption(option);
        OptionDTO savedDTO = mapper.toDTO(savedOption);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }

    /**
     * Met à jour une option existant.
     * PUT /api/options/{id}
     * @param id L'identifiant de l'option à mettre à jour
     * @param optionDTO L'option avec les nouvelles informations
     * @return L'option mis à jour, ou un statut HTTP 404 Not Found si non trouvé
     */
    @PutMapping("/{id}")
    public ResponseEntity<OptionDTO> updateOption(@PathVariable Long id, @Valid @RequestBody OptionDTO optionDTO) {
        if (!optionService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Option option = mapper.toEntity(optionDTO);
        Option updatedOption = optionService.updateOption(id, option);
        OptionDTO updatedDTO = mapper.toDTO(updatedOption);
        return ResponseEntity.ok(updatedDTO);
    }

    /**
     * Supprime une option par son ID.
     * DELETE /api/options/{id}
     * @param id L'identifiant de l'option à supprimer
     * @return Une réponse vide avec le HTTP 204 No Content si la suppression est réussie, ou 404 Not Found si l'ID n'existe pas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOption(@PathVariable Long id) {
        if (!optionService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        optionService.deleteOptionById(id);
        return ResponseEntity.noContent().build();
    }
}
