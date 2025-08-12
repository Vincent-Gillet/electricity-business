package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.AddressDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Address;
import com.electricitybusiness.api.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    /**
     * Récupère toutes les adresses.
     * GET /api/addresses
     * @return Une liste de toutes les adresses
     */
    @GetMapping
    public ResponseEntity<List<AddressDTO>> getAllAdresses() {
        List<Address> adresses = addressService.getAllAddresses();
        List<AddressDTO> adressesDTO = adresses.stream()
                .map(mapper::toDTO)
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
    public ResponseEntity<AddressDTO> getAdresseById(@PathVariable Long id) {
        return addressService.getAddressById(id)
                .map(adresse -> ResponseEntity.ok(mapper.toDTO(adresse)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée une nouvelle adresse.
     * POST /api/addresses
     * @param addressDTO L'adresse à créer
     * @return L'adresse créée avec un statut HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<AddressDTO> saveAdresse (@Valid @RequestBody AddressDTO addressDTO) {
        Address address = mapper.toEntity(addressDTO);
        Address savedAddress = addressService.saveAddress(address);
        AddressDTO savedDTO = mapper.toDTO(savedAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }

    /**
     * Met à jour une adresse existante.
     * PUT /api/addresses/{id}
     * @param id L'identifiant de l'adresse à mettre à jour
     * @param addressDTO L'adresse avec les nouvelles informations
     * @return L'adresse mise à jour, ou un statut HTTP 404 Not Found si l'adresse n'existe pas
     */
    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressDTO addressDTO) {
        if (!addressService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Address address = mapper.toEntity(addressDTO);
        Address updatedAdresse = addressService.updateAddress(id, address);
        AddressDTO updatedDTO = mapper.toDTO(updatedAdresse);
        return ResponseEntity.ok(updatedDTO);
    }

    /**
     * Supprime une adresse par son ID.
     * DELETE /api/addresses/{id}
     * @param id L'identifiant de l'adresse à supprimer
     * @return Un statut HTTP 204 No Content si la suppression est réussie, ou 404 Not Found si l'adresse n'existe pas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddressById(@PathVariable Long id) {
        if (!addressService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        addressService.deleteAddressById(id);
        return ResponseEntity.noContent().build();
    }
}
