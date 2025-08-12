package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Car;
import com.electricitybusiness.api.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les opérations liées aux voitures.
 * Fournit des méthodes pour récupérer, créer, mettre à jour et supprimer des voitures.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CarService {
    
    private final CarRepository carRepository;

    /**
     * Récupère tous les voitures.
     * @return Une liste de toutes les voitures
     */
    @Transactional(readOnly = true)
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    /**
     * Récupère une nouvelle voiture par son ID.
     * @param id L'identifiant du Car à récupérer
     * @return Un Optional contenant le Car si trouvé, sinon vide
     */
    public Optional<Car> getCarById(Long id) {
        return carRepository.findById(id);
    }


    /**
     * Crée une nouvelle voiture.
     * @param car Le Car à enregistrer
     * @return Le Car enregistré
     */
    public Car saveCar(Car car) {
        return carRepository.save(car);
    }


    /**
     * Met à jour une voiture existant.
     * @param id L'identifiant de la voiture à mettre à jour
     * @param car La voiture avec les nouvelles informations
     * @return La voiture mis à jour
     */
    public Car updateCar(Long id, Car car) {
        car.setIdCar(id);
        return carRepository.save(car);
    }

    /**
     * Supprime une voiture.
     * @param id L'identifiant de la voiture à supprimer
     */
    public void deleteCarById(Long id) {
        carRepository.deleteById(id);
    }

    /**
     * Vérifie si une voiture existe.
     * @param id L'identifiant de la voiture à vérifier
     * @return true si la voiture existe, sinon false
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return carRepository.existsById(id);
    }
}
