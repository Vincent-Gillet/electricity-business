package com.electricitybusiness.api.service;

import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Car;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.model.UserRole;
import com.electricitybusiness.api.repository.CarRepository;
import com.electricitybusiness.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarService carService;

    private User testUser;
    private Car car1;
    private Car car2;
    private UUID publicId1;
    private UUID publicId2;
    private Long id1 = 1L;
    private Long id2 = 2L;

    @BeforeEach
    void setUp() {
        // Initialisation de l'utilisateur de test
        testUser = new User(
                1L, "John", "Doe", "johndoe",
                "john.doe@example.com", "password",
                UserRole.USER, LocalDate.of(2003, 1, 1),
                "0123456789", "FR1111111111111111111111153",
                false, null, null, null, null, null
        );

        // Initialisation des IDs publics
        publicId1 = UUID.randomUUID();
        publicId2 = UUID.randomUUID();

        // Initialisation des voitures de test
        car1 = new Car(
                id1,
                publicId1,
                "ABCD123",
                "Tesla",
                "Model 3",
                Year.of(2020),
                22,
                testUser);
        car2 = new Car(
                id2,
                publicId2,
                "EFGH456",
                "Renault",
                "Zoé",
                Year.of(2018),
                11,
                testUser
        );
    }

    @Test
    void getAllCars_shouldReturnListOfCars() {
        // Préparation du mock: quand findAll() est appelé, il doit retourner nos voitures de test
        List<Car> cars = Arrays.asList(car1, car2);
        when(carRepository.findAll()).thenReturn(cars);

        // Exécution: appel de la méthode du service
        List<Car> result = carService.getAllCars();

        // Vérification
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(car1, result.get(0));
        assertEquals(car2, result.get(1));
        // Vérifie que la méthode findAll du repository a été appelée exactement une fois
        verify(carRepository, times(1)).findAll();
    }

    @Test
    void getCarById_shouldReturnCar_whenFound() {
        // Préparation du mock: quand findById(id1) est appelé, il doit retourner un Optional contenant car1
        when(carRepository.findById(id1)).thenReturn(Optional.of(car1));

        // Exécution
        Optional<Car> result = carService.getCarById(id1);

        // Vérification
        assertTrue(result.isPresent());
        assertEquals(car1, result.get());
        verify(carRepository, times(1)).findById(id1);
    }

    @Test
    void getCarById_shouldReturnEmptyOptional_whenNotFound() {
        // Préparation du mock: quand findById(unIdInexistant) est appelé, il doit retourner un Optional vide
        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        // Exécution
        Optional<Car> result = carService.getCarById(99L);

        // Vérification
        assertFalse(result.isPresent());
        verify(carRepository, times(1)).findById(99L);
    }

    @Test
    void saveCar_shouldReturnSavedCar() {
        // Car sans ID pour simuler une nouvelle création
        Car newCar = new Car(null, UUID.randomUUID(), "IJKL789", "Peugeot", "e-208", Year.of(2021), 22, testUser);
        // Le repository doit retourner la voiture avec un ID après la sauvegarde
        when(carRepository.save(any(Car.class))).thenReturn(new Car(3L, newCar.getPublicId(), newCar.getLicensePlate(), newCar.getBrand(), newCar.getModel(), newCar.getYear(), newCar.getBatteryCapacity(), newCar.getUser()));

        // Exécution
        Car savedCar = carService.saveCar(newCar);

        // Vérification
        assertNotNull(savedCar.getIdCar()); // L'ID doit avoir été assigné
        assertEquals("Peugeot", savedCar.getBrand());
        verify(carRepository, times(1)).save(any(Car.class));
    }

    @Test
    void updateCarById_shouldReturnUpdatedCar() {
        // Créer une version modifiée de car1
        Car updatedCarDetails = new Car(id1, publicId1, "ABCD123", "Tesla", "Model Y", Year.of(2022), 22, testUser);

        // Le repository doit retourner la voiture mise à jour
        when(carRepository.save(any(Car.class))).thenReturn(updatedCarDetails);

        // Exécution
        Car result = carService.updateCar(id1, updatedCarDetails);

        // Vérification
        assertNotNull(result);
        assertEquals(id1, result.getIdCar());
        assertEquals("Model Y", result.getModel());
        assertEquals(Year.of(2022), result.getYear());
        verify(carRepository, times(1)).save(any(Car.class));
    }

    @Test
    void deleteCarById_shouldCallRepositoryDelete() {
        // Exécution
        carService.deleteCarById(id1);

        // Vérification: s'assure que la méthode deleteById du repository a été appelée
        verify(carRepository, times(1)).deleteById(id1);
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        when(carRepository.existsById(id1)).thenReturn(true);

        boolean exists = carService.existsById(id1);

        assertTrue(exists);
        verify(carRepository, times(1)).existsById(id1);
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        when(carRepository.existsById(99L)).thenReturn(false);

        boolean exists = carService.existsById(99L);

        assertFalse(exists);
        verify(carRepository, times(1)).existsById(99L);
    }

    @Test
    void getCarsByUser_shouldReturnListOfCars() {
        List<Car> userCars = Arrays.asList(car1, car2);
        when(carRepository.findCarsByUser(testUser)).thenReturn(userCars);

        List<Car> result = carService.getCarsByUser(testUser);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(car1, result.get(0));
        verify(carRepository, times(1)).findCarsByUser(testUser);
    }

    @Test
    void getCarsByUser_shouldReturnEmptyList_whenNoCarsFound() {
        when(carRepository.findCarsByUser(testUser)).thenReturn(Collections.emptyList());

        List<Car> result = carService.getCarsByUser(testUser);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(carRepository, times(1)).findCarsByUser(testUser);
    }

    @Test
    void deleteCarByPublicId_shouldCallRepositoryDelete() {
        // Exécution
        carService.deleteCarByPublicId(publicId1);

        // Vérification
        verify(carRepository, times(1)).deleteCarByPublicId(publicId1);
    }

    @Test
    void existsByPublicId_shouldReturnTrue_whenExists() {
        when(carRepository.findByPublicId(publicId1)).thenReturn(Optional.of(car1));

        boolean exists = carService.existsByPublicId(publicId1);

        assertTrue(exists);
        verify(carRepository, times(1)).findByPublicId(publicId1);
    }

    @Test
    void existsByPublicId_shouldReturnFalse_whenNotExists() {
        when(carRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        boolean exists = carService.existsByPublicId(UUID.randomUUID());

        assertFalse(exists);
        verify(carRepository, times(1)).findByPublicId(any(UUID.class));
    }

    @Test
    void updateCarByPublicId_shouldReturnUpdatedCar_whenFound() {
        Car updatedCarDetails = new Car(null, null, "NEWPLATE", "Tesla", "Model S", Year.of(2023), 22, null);

        // Simule l'existence de la voiture par son publicId
        when(carRepository.findByPublicId(publicId1)).thenReturn(Optional.of(car1));
        // Simule la sauvegarde de la voiture mise à jour
        when(carRepository.save(any(Car.class))).thenReturn(
                new Car(car1.getIdCar(), car1.getPublicId(), updatedCarDetails.getLicensePlate(), updatedCarDetails.getBrand(), updatedCarDetails.getModel(),
                        updatedCarDetails.getYear(), updatedCarDetails.getBatteryCapacity(), car1.getUser())
        );

        // Exécution
        Car result = carService.updateCar(publicId1, updatedCarDetails);

        // Vérification
        assertNotNull(result);
        assertEquals(car1.getIdCar(), result.getIdCar()); // L'ID original doit être conservé
        assertEquals(publicId1, result.getPublicId()); // Le publicId original doit être conservé
        assertEquals("Model S", result.getModel()); // Le modèle doit être mis à jour
        assertEquals(Year.of(2023), result.getYear());
        assertEquals("NEWPLATE", result.getLicensePlate());
        assertEquals(testUser, result.getUser()); // L'utilisateur original doit être conservé
        verify(carRepository, times(1)).findByPublicId(publicId1);
        verify(carRepository, times(1)).save(any(Car.class));
    }

    @Test
    void updateCarByPublicId_shouldThrowException_whenNotFound() {
        Car updatedCarDetails = new Car(null, null, "XXXX000", "Ford", "Focus", Year.of(2020), 22, null);
        UUID nonExistentPublicId = UUID.randomUUID();

        // Simule qu'aucune voiture n'est trouvée pour ce publicId
        when(carRepository.findByPublicId(nonExistentPublicId)).thenReturn(Optional.empty());

        // Exécution et Vérification d'exception
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                carService.updateCar(nonExistentPublicId, updatedCarDetails)
        );

        assertTrue(thrown.getMessage().contains("Car with publicId not found: " + nonExistentPublicId));
        verify(carRepository, times(1)).findByPublicId(nonExistentPublicId);
        verify(carRepository, never()).save(any(Car.class)); // S'assurer que save n'est pas appelé
    }

    @Test
    void updateCarByPublicId_shouldRetainExistingUser_ifNewCarUserIsNull() {
        // Créez une nouvelle voiture avec des détails mis à jour mais un utilisateur nul
        Car updatedCarDetails = new Car(null, null, "ABCD123", "Tesla", "Model 3 Performance", Year.of(2021), 22, null);

        // Simulez l'existence de la voiture originale
        when(carRepository.findByPublicId(publicId1)).thenReturn(Optional.of(car1));

        // Simulez la sauvegarde, en retournant une nouvelle instance avec l'utilisateur existant
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car carToSave = invocation.getArgument(0);
            // Assurez-vous que l'utilisateur de carToSave est bien celui de car1
            assertEquals(testUser, carToSave.getUser());
            return carToSave; // Retourne la voiture modifiée avec l'utilisateur correct
        });

        // Exécution
        Car result = carService.updateCar(publicId1, updatedCarDetails);

        // Vérification
        assertNotNull(result.getUser());
        assertEquals(testUser, result.getUser());
        assertEquals("Model 3 Performance", result.getModel());
        verify(carRepository, times(1)).findByPublicId(publicId1);
        verify(carRepository, times(1)).save(any(Car.class));
    }

    @Test
    void updateCarByPublicId_shouldUpdateUser_ifNewCarUserIsProvided() {
        // Créez un nouvel utilisateur
        User newUser = new User(
                2L, "Jane", "Doe", "janedoe",
                "jane.doe@example.com", "password",
                UserRole.USER, LocalDate.of(2000, 5, 10),
                "9876543210", "FR2222222222222222222222253",
                false, null, null, null, null, null
        );

        // Créez une nouvelle voiture avec des détails mis à jour et un nouvel utilisateur
        Car updatedCarDetails = new Car(null, null, "NEWLIC", "Tesla", "Model X", Year.of(2024), 22, newUser);

        // Simulez l'existence de la voiture originale
        when(carRepository.findByPublicId(publicId1)).thenReturn(Optional.of(car1));

        // Simulez la sauvegarde, en retournant une nouvelle instance avec le nouvel utilisateur
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car carToSave = invocation.getArgument(0);
            // Assurez-vous que l'utilisateur de carToSave est bien le nouvel utilisateur
            assertEquals(newUser, carToSave.getUser());
            return carToSave; // Retourne la voiture modifiée avec l'utilisateur correct
        });

        // Exécution
        Car result = carService.updateCar(publicId1, updatedCarDetails);

        // Vérification
        assertNotNull(result.getUser());
        assertEquals(newUser, result.getUser());
        assertEquals("Model X", result.getModel());
        verify(carRepository, times(1)).findByPublicId(publicId1);
        verify(carRepository, times(1)).save(any(Car.class));
    }
}
