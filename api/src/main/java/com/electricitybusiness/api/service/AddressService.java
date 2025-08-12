package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Address;
import com.electricitybusiness.api.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les opérations liées aux addresss.
 * Fournit des méthodes pour récupérer, créer, mettre à jour et supprimer des addresss.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AddressService {
    private final AddressRepository addressRepository;

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
}
