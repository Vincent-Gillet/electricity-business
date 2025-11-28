package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.option.OptionCreateDTO;
import com.electricitybusiness.api.dto.option.OptionDTO;
import com.electricitybusiness.api.dto.terminal.TerminalDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Option;
import com.electricitybusiness.api.model.Terminal;
import com.electricitybusiness.api.model.TerminalStatus;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.service.OptionService;
import com.electricitybusiness.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/options")
@RequiredArgsConstructor
public class OptionController {
    private final OptionService optionService;
    private final EntityMapper mapper;
    private final UserService userService;

    /**
     * Récupère toutes les options.
     * GET /api/options
     * @return Une liste de toutes les options
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
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
    @PreAuthorize("hasAuthority('ADMIN')")
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
    @PreAuthorize("hasAuthority('ADMIN')")
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
    @PreAuthorize("hasAuthority('ADMIN')")
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
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteOption(@PathVariable Long id) {
        if (!optionService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        optionService.deleteOptionById(id);
        return ResponseEntity.noContent().build();
    }

    // Requete pour le user connecté

    @GetMapping("/place/{idPlace}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OptionDTO>> getAllOptionsByUser(
            @PathVariable UUID idPlace
    ) {
        // Récupérer les voitures de l'utilisateur
        List<Option> options = optionService.getOptionsByPlace(idPlace);
        List<OptionDTO> optionsDTO = options.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(optionsDTO);
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OptionDTO>> getAllOptionsByUser() {
        // Récupérer l'utilisateur authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long idUser = userService.getIdByEmailUser(email);
        User user = userService.getUserById(idUser);

        // Récupérer les voitures de l'utilisateur
        List<Option> options = optionService.getOptionsByUser(user);
        List<OptionDTO> optionsDTO = options.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(optionsDTO);
    }

    @GetMapping("/terminal/{idTerminal}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OptionDTO>> getAllOptionsByTerminal(
            @PathVariable UUID idTerminal
    ) {
        // Récupérer les voitures de l'utilisateur
        List<Option> options = optionService.getOptionsByTerminal(idTerminal);
        List<OptionDTO> optionsDTO = options.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(optionsDTO);
    }

    @PostMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OptionDTO> saveTerminalByToken(@Valid @RequestBody OptionCreateDTO optionDTO) {
        try {
            Option option = mapper.toEntityCreate(optionDTO, optionDTO.getPublicIdPlace());
            Option savedOption = optionService.saveOption(option);

            OptionDTO savedDTO = mapper.toDTO(savedOption);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime un lieu par son identifiant public.
     * DELETE /api/terminals/publicId/{publicId}
     * @param publicId L'identifiant public du lieu à supprimer
     * @return Un statut HTTP 204 No Content si la suppression est réussie, ou 404 Not Found si le lieu n'existe pas
     */
    @DeleteMapping("publicId/{publicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteOption(@PathVariable UUID publicId) {
        if (!optionService.existsByPublicId(publicId)) {
            return ResponseEntity.notFound().build();
        }
        optionService.deleteOptionByPublicId(publicId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/publicId/{publicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OptionDTO> updateOption(
            @PathVariable UUID publicId,
            @Valid @RequestBody OptionCreateDTO optionDTO
    ) {
        if (!optionService.existsByPublicId(publicId)) {
            return ResponseEntity.notFound().build();
        }

        // Mettre à jour le borne
        Option option = mapper.toEntityCreate(optionDTO, publicId);
        Option updatedOption = optionService.updateOption(publicId, option);
        OptionDTO updatedDTO = mapper.toDTO(updatedOption);
        return ResponseEntity.ok(updatedDTO);
    }
}
