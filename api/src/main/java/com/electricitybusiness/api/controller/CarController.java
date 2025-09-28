package com.electricitybusiness.api.controller;

import com.electricitybusiness.api.dto.CarCreateDTO;
import com.electricitybusiness.api.dto.CarDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Car;
import com.electricitybusiness.api.service.CarService;
import com.electricitybusiness.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.electricitybusiness.api.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des véhicules.
 * Expose les endpoints pour les opérations CRUD sur les véhicules.
 */
@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {
    private final CarService carService;
    private final EntityMapper mapper;
    private final UserService userService;


    /**
     * Récupère tous les Cars.
     * GET /api/cars
     * @return Une liste de tous les véhicules
     */
    @GetMapping
    public ResponseEntity<List<CarDTO>> getAllCars() {
        List<Car> cars = carService.getAllCars();
        List<CarDTO> CarsDTO = cars.stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(CarsDTO);
    }

    /**
     * Récupère un Car par son ID.
     * GET /api/cars/{id}
     * @param id L'identifiant du véhicule à récupérer
     * @return Le véhicule correspondant à l'ID, ou un statut HTTP 404 Not Found si non trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<CarDTO> getCarById(@PathVariable Long id) {
        return carService.getCarById(id)
                .map(Car -> ResponseEntity.ok(mapper.toDTO(Car)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crée un nouveau Car.
     * POST /api/cars
     * @param carDTO Le véhicule à créer
     * @return Le véhicule créé avec un statut HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<CarDTO> saveCar(@Valid @RequestBody CarCreateDTO carDTO) {
        System.out.println("=== saveCar() CALLED ===");


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Long idUser = userService.getIdByEmailUser(email);
        carDTO.setIdUser(idUser);

        System.out.println("Authentication: " + authentication);
        System.out.println("Email: " + email);

        System.out.println("ID User: " + idUser);
        System.out.println("CarCreateDTO: " + carDTO);

        Car car = mapper.toEntityCreate(carDTO, idUser);
        System.out.println("Car: " + car);
        Car savedCar = carService.saveCar(car);
        System.out.println("Saving Car: " + savedCar);
        CarDTO savedDTO = mapper.toDTO(savedCar);
        System.out.println("Saving savedDTO: " + savedDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDTO);
    }

    /**
     * Met à jour une voiture existante.
     * PUT /api/cars/{id}
     * @param id L'identifiant de la voiture à mettre à jour
     * @param carDTO La voiture avec les nouvelles informations
     * @return La voiture mis à jour, ou un statut HTTP 404 Not Found si l'ID n'existe pas
     */
    @PutMapping("/{id}")
    public ResponseEntity<CarDTO> updateCar(@PathVariable Long id, @Valid @RequestBody CarCreateDTO carDTO) {
        if (!carService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        Car Car = mapper.toEntityCreate(carDTO, null);
        Car updatedCar = carService.updateCar(id, Car);
        CarDTO updatedDTO = mapper.toDTO(updatedCar);
        return ResponseEntity.ok(updatedDTO);    }


    /**
     * Supprime un Car.
     * DELETE /api/cars/{id}
     * @param id L'identifiant de la voiture à supprimer
     * @return Une réponse vide avec le statut 204 No Content si la voiture a été supprimé, ou 404 Not Found si le véhicule n'existe pas
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        if (!carService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        carService.deleteCarById(id);
        return ResponseEntity.noContent().build();
    }
}
