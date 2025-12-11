package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.address.AddressDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Address;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour gérer les opérations liées aux addresss.
 * Fournit des méthodes pour récupérer, créer, mettre à jour et supprimer des addresss.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {
    private final AddressRepository addressRepository;
    private final EntityMapper entityMapper;

    /**
     * Récupère toutes les addresss.
     * @return Une liste de toutes les addresss
     */
    @Transactional(readOnly = true)
    public List<Address> getAllAddresses() {
        return addressRepository.findAll();
    }

    /**
     * Récupère une adress par son ID.
     * @param id L'identifiant de l'address à récupérer
     * @return Une address si elle existe, sinon un Optional vide
     */
    public Optional<Address> getAddressById(Long id) {
        return addressRepository.findById(id);
    }


    /**
     * Crée une nouvelle address.
     * @param address L'address à enregistrer
     * @return L'address enregistrée
     */
    public Address saveAddress(Address address) {
        return addressRepository.save(address);
    }

    /**
     * Met à jour une address existant.
     * @param id L'identifiant de l'address à mettre à jour
     * @param address L'address avec les nouvelles informations
     */
    public Address updateAddress(Long id, Address address) {
        address.setIdAddress(id);
        return addressRepository.save(address);
    }

    /**
     * Supprime une address.
     * @param id L'identifiant de l'address à supprimer
     */
    public void deleteAddressById(Long id) {
        addressRepository.deleteById(id);
    }


    /**
     * Vérifie si une address existe.
     * @param id L'identifiant de l'address à vérifier
     * @return true si l'address existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return addressRepository.existsById(id);
    }

    /**
     * Récupère toutes les adresses d'un utilisateur.
     * @param user L'utilisateur dont on veut récupérer les adresses
     * @return Une liste des adresses associées à l'utilisateur
     */
    @Transactional(readOnly = true)
    public List<Address> getAddressesByUser(User user) { return addressRepository.findAddressesByUser(user); }

    /**
     * Supprime une adresse.
     * @param publicId L'identifiant de la adresse à supprimer
     */
    public void deleteAddressByPublicId(UUID publicId) {
        addressRepository.deleteAddressByPublicId(publicId);
    }

    /**
     * Vérifie si une adresse existe.
     * @param publicId L'identifiant de la adresse à vérifier
     * @return true si la adresse existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsByPublicId(UUID publicId) {
        return addressRepository.findByPublicId(publicId).isPresent();
    }

    /**
     * Met à jour une voiture existant.
     * @param publicId L'identifiant de la voiture à mettre à jour
     * @param address La voiture avec les nouvelles informations
     * @return La voiture mis à jour
     */
    public Address updateAddress(UUID publicId, Address address) {
        Address existing = addressRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Address with publicId not found: " + publicId));

        address.setIdAddress(existing.getIdAddress());
        address.setPublicId(existing.getPublicId());

        User existingUser = existing.getUser();
        if (address.getUser() == null) {
            address.setUser(existingUser);
        }
        return addressRepository.save(address);
    }

    public ResponseEntity<AddressDTO> getAddressDTOByPublicId(UUID publicId) {
        return addressRepository.findByPublicId(publicId)
                .map(address -> ResponseEntity.ok(entityMapper.toAddressDTO(address)))
                .orElse(ResponseEntity.notFound().build());
    }
}
