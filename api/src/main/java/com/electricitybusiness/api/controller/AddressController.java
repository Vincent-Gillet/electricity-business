package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.address.AddressCreateDTO;
import com.electricitybusiness.api.dto.address.AddressDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Address;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.service.AddressService;
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

/**
 * Contrôleur REST pour la gestion des adresses.
 * Expose les endpoints pour les opérations CRUD sur les adresses.
 */
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final EntityMapper mapper;
    private final UserService userService;

    /**
     * Récupère toutes les adresses.
     * GET /api/addresses
     * @return Une liste de toutes les adresses
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<AddressDTO>> getAllAdresses() {
        // Récupérer toutes les adresses
        List<Address> adresses = addressService.getAllAddresses();
        // Convertir les adresses en DTOs
        List<AddressDTO> adressesDTO = adresses.stream()
                .map(mapper::toAddressDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(adressesDTO);
    }

    /**
     * Récupère une adresse par son ID.
     * GET /api/addresses/{id}
     * @param id L'identifiant de l'adresse à récupérer
     * @return L'adresse correspondante à l'ID, ou un statut HTTP 404 Not Found si non trouvée
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AddressDTO> getAdresseById(@PathVariable Long id) {
        return addressService.getAddressById(id)
                .map(adresse -> ResponseEntity.ok(mapper.toAddressDTO(adresse)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée une nouvelle adresse.
     * POST /api/addresses
     * @param addressDTO L'adresse à créer
     * @return L'adresse créée avec un statut HTTP 201 Created
     */
/*    @PostMapping
    public ResponseEntity<AddressDTO> saveAdresse (@Valid @RequestBody AddressDTO addressDTO) {
        Address address = mapper.toEntity(addressDTO);
        Address savedAddress = addressService.saveAddress(address);
        AddressDTO savedDTO = mapper.toAddressDTO(savedAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }*/
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressDTO> saveAdresse(@Valid @RequestBody AddressCreateDTO addressDTO) {
        try {
            // Récupérer l'utilisateur authentifié
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Long idUser = userService.getIdByEmailUser(email);

            // Créer et enregistrer l'adresse
            Address address = mapper.toEntityCreate(addressDTO, idUser);
            Address savedAddress = addressService.saveAddress(address);
            AddressDTO savedDTO = mapper.toAddressDTO(savedAddress);

            // Retourner la réponse avec le statut CREATED
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
        } catch (Exception e) {
            // Gérer les exceptions et retourner une réponse d'erreur appropriée
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met à jour une adresse existante.
     * PUT /api/addresses/{id}
     * @param id L'identifiant de l'adresse à mettre à jour
     * @param addressDTO L'adresse avec les nouvelles informations
     * @return L'adresse mise à jour, ou un statut HTTP 404 Not Found si l'adresse n'existe pas
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressDTO addressDTO) {
        // Vérifier si l'adresse existe
        if (!addressService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // Mettre à jour l'adresse
        Address address = mapper.toEntity(addressDTO);
        Address updatedAdresse = addressService.updateAddress(id, address);
        AddressDTO updatedDTO = mapper.toAddressDTO(updatedAdresse);
        return ResponseEntity.ok(updatedDTO);
    }

    /**
     * Supprime une adresse par son ID.
     * DELETE /api/addresses/{id}
     * @param id L'identifiant de l'adresse à supprimer
     * @return Un statut HTTP 204 No Content si la suppression est réussie, ou 404 Not Found si l'adresse n'existe pas
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteAddressById(@PathVariable Long id) {
        // Vérifier si l'adresse existe
        if (!addressService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        // Supprimer l'adresse
        addressService.deleteAddressById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère tous les Addresss d'un utilisateur.
     * GET /api/addresss/user/{idUser}
     * @return Une liste de tous les adresses
     */
    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AddressDTO>> getAllAddresssByUser() {
        // Récupérer l'utilisateur authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long idUser = userService.getIdByEmailUser(email);
        User user = userService.getUserById(idUser);

        // Récupérer les adresses associées à l'utilisateur
        List<Address> addresss = addressService.getAddressesByUser(user);
        List<AddressDTO> AddresssDTO = addresss.stream()
                .map(mapper::toAddressDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(AddresssDTO);
    }

    /**
     * Supprime un Address.
     * DELETE /api/addresss/{id}
     * @param publicId L'identifiant de l'adresse à supprimer
     * @return Une réponse vide avec le statut 204 No Content si l'adresse a été supprimé, ou 404 Not Found si le véhicule n'existe pas
     */
    @DeleteMapping("publicId/{publicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID publicId) {
        // Vérifier si l'adresse existe
        if (!addressService.existsByPublicId(publicId)) {
            return ResponseEntity.notFound().build();
        }
        // Supprimer l'adresse
        addressService.deleteAddressByPublicId(publicId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Met à jour une adresse existante.
     * PUT /api/addresss/{id}
     * @param publicId L'identifiant de la voiture à mettre à jour
     * @param addressDTO L'adresse avec les nouvelles informations
     * @return L'adresse mis à jour, ou un statut HTTP 404 Not Found si l'ID n'existe pas
     */
    @PutMapping("/publicId/{publicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable UUID publicId,
            @Valid @RequestBody AddressCreateDTO addressDTO
    ) {
        // Vérifier si l'adresse existe
        if (!addressService.existsByPublicId(publicId)) {
            return ResponseEntity.notFound().build();
        }
        // Récupérer l'utilisateur authentifié
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long idUser = userService.getIdByEmailUser(email);

        // Mettre à jour l'adresse
        Address address = mapper.toEntityCreate(addressDTO, idUser);
        Address updatedAddress = addressService.updateAddress(publicId, address);
        AddressDTO updatedDTO = mapper.toAddressDTO(updatedAddress);
        return ResponseEntity.ok(updatedDTO);
    }

    @GetMapping("/publicId/{publicId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AddressDTO> getAddressByPublicId(@PathVariable UUID publicId) {
        return addressService.getAddressDTOByPublicId(publicId);
    }
}
