package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.terminal.TerminalCreateDTO;
import com.electricitybusiness.api.dto.terminal.TerminalDTO;
import com.electricitybusiness.api.dto.terminal.TerminalSearchDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Terminal;
import com.electricitybusiness.api.model.TerminalStatus;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.service.TerminalService;
import com.electricitybusiness.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des Terminals de recharge.
 * Expose les endpoints pour les opérations CRUD sur les Terminals.
 */
@RestController
@RequestMapping("/api/terminals")
@RequiredArgsConstructor
public class TerminalController {

    private final TerminalService terminalService;
    private final EntityMapper mapper;
    private final UserService userService;


    /**
     * Récupère toutes les Terminals de recharge.
     * GET /api/terminals
     * @return Une liste de toutes les Terminals
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<TerminalDTO>> getAllTerminals() {
        List<Terminal> terminals = terminalService.getAllTerminals();
        List<TerminalDTO> TerminalDTO = terminals.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(TerminalDTO);
    }

    /**
     * Récupère une Terminal de recharge par son ID.
     * GET /api/terminals/{id}
     * @param id L'identifiant de la Terminal à récupérer
     * @return La Terminal correspondante à l'ID, ou un statut HTTP 404 Not Found si non trouvée
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TerminalDTO> getTerminalById (@PathVariable Long id) {
        return terminalService.getTerminalById(id)
                .map(Terminal -> ResponseEntity.ok(mapper.toDTO(Terminal)))
                .orElse(ResponseEntity.notFound().build());
    }


    /**
     * Crée une nouvelle Terminal de recharge.
     * POST /api/terminals
     * @param TerminalDTO La Terminal à créer
     * @return La Terminal créée avec un statut HTTP 201 Created
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TerminalDTO> saveTerminal (@Valid @RequestBody TerminalDTO TerminalDTO) {
        Terminal Terminal = mapper.toEntity(TerminalDTO);
        Terminal savedTerminal = terminalService.saveTerminal(Terminal);
        TerminalDTO savedDTO = mapper.toDTO(savedTerminal);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }

    /**
     * Met à jour une Terminal de recharge existante.
     * PUT /api/terminals/{id}
     * @param id L'identifiant de la Terminal à mettre à jour
     * @param TerminalDTO La Terminal avec les nouvelles informations
     * @return La Terminal mise à jour ou un statut HTTP 404 Not Found si non trouvée
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<TerminalDTO> updateTerminal(@PathVariable Long id, @Valid @RequestBody TerminalDTO TerminalDTO) {
        if (!terminalService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Terminal Terminal = mapper.toEntity(TerminalDTO);
        Terminal updatedTerminal = terminalService.updateTerminal(id, Terminal);
        TerminalDTO updatedDTO = mapper.toDTO(updatedTerminal);
        return ResponseEntity.ok(updatedDTO);
    }

    /**
     * Supprime une Terminal de recharge par son ID.
     * DELETE /api/terminals/{id}
     * @param id L'identifiant de la Terminal à supprimer
     * @return Un statut HTTP 204 No Content si la suppression est réussie, ou 404 Not Found si non trouvée
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteTerminalById(@PathVariable Long id) {
        if (!terminalService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        terminalService.deleteTerminalById(id);
        return ResponseEntity.noContent().build();
    }



/*
    Test désactivation pour voir utilité réelle plus tard

    @GetMapping("/terminals-free")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TerminalDTO>> getAllTerminalsNoOccupieds() {
        List<Terminal> terminals = terminalService.findAvailableTerminals();
        List<TerminalDTO> TerminalDTO = terminals.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(TerminalDTO);
    }
*/

/*
    Test désactivation pour voir utilité réelle plus tard

    @GetMapping("/search-terminals-avaible-radius")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TerminalDTO>> findTerminalsAvaibleInRadius(
            @RequestParam BigDecimal longitude,
            @RequestParam BigDecimal latitude,
            @RequestParam double radius,
            @RequestParam(defaultValue = "false") boolean occupied
    ) {
        List<Terminal> terminals = terminalService.findTerminalsAvailableInRadius(longitude, latitude, radius, occupied);
        List<TerminalDTO> TerminalDTO = terminals.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(TerminalDTO);
    }*/

/*
    Test désactivation pour voir utilité réelle plus tard

    @GetMapping("/search-terminals-dates-avaible")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TerminalDTO>> findTerminalsAvaibleInRadiusAndPeriod(
            @RequestParam(defaultValue = "false") boolean occupied,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startingDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endingDate
    ) {
        List<Terminal> terminals = terminalService.findTerminalsAvailableInPeriod(
                occupied, startingDate, endingDate);
        List<TerminalDTO> TerminalDTO = terminals.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(TerminalDTO);
    }*/

/*
    Test désactivation pour voir utilité réelle plus tard

    @GetMapping("/recherche-terminals-dates-disponibilites-radius")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TerminalDTO>> findterminalsDisponiblesInRadiusAndPeriod(
            @RequestParam BigDecimal longitude,
            @RequestParam BigDecimal latitude,
            @RequestParam double radius,
            @RequestParam(defaultValue = "false") boolean occupied,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startingDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endingDate
    ) {
        List<Terminal> terminals = terminalService.findTerminalsAvailableInRadiusAndPeriod(
                longitude, latitude, radius, occupied, startingDate, endingDate);
        List<TerminalDTO> TerminalDTO = terminals.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(TerminalDTO);
    }
*/


/*    @PostMapping("/recherche")
    public ResponseEntity<List<TerminalDTO>> searchTerminals(
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) double radius,
            @RequestParam(defaultValue = "false", required = false) boolean occupied,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startingDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endingDate
    ) {
        List<Terminal> terminals = terminalService.searchTerminals(
                longitude, latitude, radius, occupied, startingDate, endingDate);
        List<TerminalDTO> TerminalDTO = terminals.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(TerminalDTO);
    }*/




    @GetMapping("/search-terminals")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TerminalDTO>> searchTerminals(
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) Double radius,
            @RequestParam(required = false) Boolean occupied,
            @RequestParam(required = false) LocalDateTime startingDate,
            @RequestParam(required = false) LocalDateTime endingDate
    ) {
/*
        List<Terminal> terminals = terminalService.searchTerminalsWithCriteria(
                longitude, latitude, radius, occupied, startingDate, endingDate);
*/

        System.out.println("longitude terminal : " + longitude);
        System.out.println("latitude terminal : " + latitude);
        System.out.println("radius terminal : " + radius);
        System.out.println("occupied terminal : " + occupied);
        System.out.println("startingDate terminal : " + startingDate);
        System.out.println("endingDate terminal : " + endingDate);

/*
        TerminalSearchDTO terminalSearchDTO = new TerminalSearchDTO(
                longitude,
                latitude,
                radius,
                occupied,
                startingDate,
                endingDate
        );

        List<Terminal> terminals = terminalService.searchTerminals(terminalSearchDTO);
*/


        List<Terminal> terminals = terminalService.searchTerminals(
                longitude, latitude, radius, occupied, startingDate, endingDate);

        List<TerminalDTO> terminalDTO = terminals.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(terminalDTO);
    }
    
    
    
    
    // Requete pour le user connecté

    /**
     * Récupère tous les lieux associés à l'utilisateur authentifié.
     * GET /api/terminals/user
     * @return Une liste de lieux associés à l'utilisateur
     */
    @GetMapping("/place/{idPlace}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TerminalDTO>> getAllTerminalsByUser(
            @PathVariable UUID idPlace
    ) {
        // Récupérer les voitures de l'utilisateur
        List<Terminal> terminals = terminalService.getTerminalsByPlace(idPlace);
        List<TerminalDTO> terminalsDTO = terminals.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(terminalsDTO);
    }

    @PostMapping("/place")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TerminalDTO> saveTerminalByToken(@Valid @RequestBody TerminalCreateDTO terminalDTO) {
        try {
            Terminal terminal = mapper.toEntityCreate(terminalDTO, terminalDTO.getPublicIdPlace());
            Terminal savedTerminal = terminalService.saveTerminal(terminal);

            TerminalDTO savedDTO = mapper.toDTO(savedTerminal);

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
    public ResponseEntity<Void> deleteTerminal(@PathVariable UUID publicId) {
        if (!terminalService.existsByPublicId(publicId)) {
            return ResponseEntity.notFound().build();
        }
        terminalService.deleteTerminalByPublicId(publicId);
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/publicId/{publicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TerminalDTO> updateTerminal(
            @PathVariable UUID publicId,
            @Valid @RequestBody TerminalCreateDTO terminalDTO
    ) {
        if (!terminalService.existsByPublicId(publicId)) {
            return ResponseEntity.notFound().build();
        }

        // Mettre à jour le borne
        Terminal terminal = mapper.toEntityCreate(terminalDTO, publicId);
        Terminal updatedTerminal = terminalService.updateTerminal(publicId, terminal);
        TerminalDTO updatedDTO = mapper.toDTO(updatedTerminal);
        return ResponseEntity.ok(updatedDTO);
    }
    
    @GetMapping("/statuses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TerminalStatus>> getAllTerminalStatus() {
        List<TerminalStatus> statuses = terminalService.getAllTerminalStatuses();
        return ResponseEntity.ok(statuses);
    }
}
