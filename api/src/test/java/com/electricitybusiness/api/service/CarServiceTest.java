package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Car;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.model.UserRole;
import com.electricitybusiness.api.repository.CarRepository;
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
        testUser = new User(
                1L, "John", "Doe", "johndoe",
                "john.doe@example.com", "password",
                UserRole.USER, LocalDate.of(2003, 1, 1),
                "0123456789", "FR1111111111111111111111153",
                false, null, null, null, null, null
        );

        publicId1 = UUID.randomUUID();
        publicId2 = UUID.randomUUID();

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

    /**
     * Test de la méthode getAllCars du service CarService.
     * Vérifie que la liste des voitures retournée par le service correspond à celle fournie par le repository.
     */
    @Test
    void getAllCars_shouldReturnListOfCars() {
        List<Car> cars = Arrays.asList(car1, car2);
        when(carRepository.findAll()).thenReturn(cars);

        List<Car> result = carService.getAllCars();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(car1, result.get(0));
        assertEquals(car2, result.get(1));
        verify(carRepository, times(1)).findAll();
    }

    /**
     * Test de la méthode getCarById du service CarService.
     * Vérifie que la voiture retournée par le service correspond à celle trouvée par le repository.
     */
    @Test
    void getCarById_shouldReturnCar_whenFound() {
        when(carRepository.findById(id1)).thenReturn(Optional.of(car1));

        Optional<Car> result = carService.getCarById(id1);

        assertTrue(result.isPresent());
        assertEquals(car1, result.get());
        verify(carRepository, times(1)).findById(id1);
    }

    /**
     * Test de la méthode getCarById du service CarService.
     * Vérifie que le service retourne un Optional vide lorsque la voiture n'est pas trouvée.
     */
    @Test
    void getCarById_shouldReturnEmptyOptional_whenNotFound() {
        when(carRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Car> result = carService.getCarById(99L);

        assertFalse(result.isPresent());
        verify(carRepository, times(1)).findById(99L);
    }

    /**
     * Test de la méthode saveCar du service CarService.
     * Vérifie que la voiture sauvegardée est bien retournée avec un ID assigné.
     */
    @Test
    void saveCar_shouldReturnSavedCar() {
        Car newCar = new Car(null, UUID.randomUUID(), "IJKL789", "Peugeot", "e-208", Year.of(2021), 22, testUser);
        when(carRepository.save(any(Car.class)))
                .thenReturn(
                        new Car(3L,
                                newCar.getPublicId(),
                                newCar.getLicensePlate(),
                                newCar.getBrand(),
                                newCar.getModel(),
                                newCar.getYear(),
                                newCar.getBatteryCapacity(),
                                newCar.getUser()
                        )
                );

        Car savedCar = carService.saveCar(newCar);

        assertNotNull(savedCar.getIdCar());
        assertEquals("Peugeot", savedCar.getBrand());
        verify(carRepository, times(1)).save(any(Car.class));
    }

    /**
     * Test de la méthode updateCarById du service CarService.
     * Vérifie que la voiture mise à jour est bien retournée avec les nouvelles informations.
     */
    @Test
    void updateCarById_shouldReturnUpdatedCar() {
        Car updatedCarDetails = new Car(id1, publicId1, "ABCD123", "Tesla", "Model Y", Year.of(2022), 22, testUser);

        when(carRepository.save(any(Car.class))).thenReturn(updatedCarDetails);

        Car result = carService.updateCar(id1, updatedCarDetails);

        assertNotNull(result);
        assertEquals(id1, result.getIdCar());
        assertEquals("Model Y", result.getModel());
        assertEquals(Year.of(2022), result.getYear());
        verify(carRepository, times(1)).save(any(Car.class));
    }

    /**
     * Test de la méthode deleteCarById du service CarService.
     * Vérifie que la méthode deleteById du repository est bien appelée.
     */
    @Test
    void deleteCarById_shouldCallRepositoryDelete() {
        carService.deleteCarById(id1);

        verify(carRepository, times(1)).deleteById(id1);
    }

    /**
     * Test de la méthode existsById du service CarService.
     * Vérifie que la méthode retourne true lorsque la voiture existe.
     */
    @Test
    void existsById_shouldReturnTrue_whenExists() {
        when(carRepository.existsById(id1)).thenReturn(true);

        boolean exists = carService.existsById(id1);

        assertTrue(exists);
        verify(carRepository, times(1)).existsById(id1);
    }

    /**
     * Test de la méthode existsById du service CarService.
     * Vérifie que la méthode retourne false lorsque la voiture n'existe pas.
     */
    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        when(carRepository.existsById(99L)).thenReturn(false);

        boolean exists = carService.existsById(99L);

        assertFalse(exists);
        verify(carRepository, times(1)).existsById(99L);
    }

    /**
     * Test de la méthode getCarsByUser du service CarService.
     * Vérifie que la liste des voitures retournée pour un utilisateur donné est correcte.
     */
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

    /**
     * Test de la méthode getCarsByUser du service CarService.
     * Vérifie que la méthode retourne une liste vide lorsque l'utilisateur n'a pas de voitures.
     */
    @Test
    void getCarsByUser_shouldReturnEmptyList_whenNoCarsFound() {
        when(carRepository.findCarsByUser(testUser)).thenReturn(Collections.emptyList());

        List<Car> result = carService.getCarsByUser(testUser);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(carRepository, times(1)).findCarsByUser(testUser);
    }

    /**
     * Test de la méthode getCarByPublicId du service CarService.
     * Vérifie que la voiture retournée correspond à celle trouvée par le repository.
     */
    @Test
    void deleteCarByPublicId_shouldCallRepositoryDelete() {
        // Exécution
        carService.deleteCarByPublicId(publicId1);

        // Vérification
        verify(carRepository, times(1)).deleteCarByPublicId(publicId1);
    }

    /**
     * Test de la méthode existsByPublicId du service CarService.
     * Vérifie que la méthode retourne true lorsque la voiture existe.
     */
    @Test
    void existsByPublicId_shouldReturnTrue_whenExists() {
        when(carRepository.findByPublicId(publicId1)).thenReturn(Optional.of(car1));

        boolean exists = carService.existsByPublicId(publicId1);

        assertTrue(exists);
        verify(carRepository, times(1)).findByPublicId(publicId1);
    }

    /**
     * Test de la méthode existsByPublicId du service CarService.
     * Vérifie que la méthode retourne false lorsque la voiture n'existe pas.
     */
    @Test
    void existsByPublicId_shouldReturnFalse_whenNotExists() {
        when(carRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        boolean exists = carService.existsByPublicId(UUID.randomUUID());

        assertFalse(exists);
        verify(carRepository, times(1)).findByPublicId(any(UUID.class));
    }

    /**
     * Test de la méthode updateCarByPublicId du service CarService.
     * Vérifie que la voiture est mise à jour correctement lorsque trouvée par son publicId.
     */
    @Test
    void updateCarByPublicId_shouldReturnUpdatedCar_whenFound() {
        Car updatedCarDetails = new Car(null, null, "NEWPLATE", "Tesla", "Model S", Year.of(2023), 22, null);

        when(carRepository.findByPublicId(publicId1)).thenReturn(Optional.of(car1));
        when(carRepository.save(any(Car.class))).thenReturn(
                new Car(car1.getIdCar(), car1.getPublicId(), updatedCarDetails.getLicensePlate(), updatedCarDetails.getBrand(), updatedCarDetails.getModel(),
                        updatedCarDetails.getYear(), updatedCarDetails.getBatteryCapacity(), car1.getUser())
        );

        Car result = carService.updateCar(publicId1, updatedCarDetails);

        assertNotNull(result);
        assertEquals(car1.getIdCar(), result.getIdCar());
        assertEquals(publicId1, result.getPublicId());
        assertEquals("Model S", result.getModel());
        assertEquals(Year.of(2023), result.getYear());
        assertEquals("NEWPLATE", result.getLicensePlate());
        assertEquals(testUser, result.getUser());
        verify(carRepository, times(1)).findByPublicId(publicId1);
        verify(carRepository, times(1)).save(any(Car.class));
    }

    /**
     * Test de la méthode updateCarByPublicId du service CarService.
     * Vérifie qu'une exception est lancée lorsque la voiture n'est pas trouvée par son publicId.
     */
    @Test
    void updateCarByPublicId_shouldThrowException_whenNotFound() {
        Car updatedCarDetails = new Car(null, null, "XXXX000", "Ford", "Focus", Year.of(2020), 22, null);
        UUID nonExistentPublicId = UUID.randomUUID();

        when(carRepository.findByPublicId(nonExistentPublicId)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                carService.updateCar(nonExistentPublicId, updatedCarDetails)
        );

        assertTrue(thrown.getMessage().contains("Car with publicId not found: " + nonExistentPublicId));
        verify(carRepository, times(1)).findByPublicId(nonExistentPublicId);
        verify(carRepository, never()).save(any(Car.class));
    }

    /**
     * Test de la méthode updateCarByPublicId du service CarService.
     * Vérifie que l'utilisateur existant est conservé lorsque la nouvelle voiture n'a pas d'utilisateur.
     */
    @Test
    void updateCarByPublicId_shouldRetainExistingUser_ifNewCarUserIsNull() {
        Car updatedCarDetails = new Car(null, null, "ABCD123", "Tesla", "Model 3 Performance", Year.of(2021), 22, null);

        when(carRepository.findByPublicId(publicId1)).thenReturn(Optional.of(car1));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car carToSave = invocation.getArgument(0);
            assertEquals(testUser, carToSave.getUser());
            return carToSave;
        });

        Car result = carService.updateCar(publicId1, updatedCarDetails);

        assertNotNull(result.getUser());
        assertEquals(testUser, result.getUser());
        assertEquals("Model 3 Performance", result.getModel());
        verify(carRepository, times(1)).findByPublicId(publicId1);
        verify(carRepository, times(1)).save(any(Car.class));
    }

    /**
     * Test de la méthode updateCarByPublicId du service CarService.
     * Vérifie que l'utilisateur est mis à jour lorsque la nouvelle voiture a un utilisateur différent.
     */
    @Test
    void updateCarByPublicId_shouldUpdateUser_ifNewCarUserIsProvided() {
        User newUser = new User(
                2L, "Jane", "Doe", "janedoe",
                "jane.doe@example.com", "password",
                UserRole.USER, LocalDate.of(2000, 5, 10),
                "9876543210", "FR2222222222222222222222253",
                false, null, null, null, null, null
        );

        Car updatedCarDetails = new Car(null, null, "NEWLIC", "Tesla", "Model X", Year.of(2024), 22, newUser);

        when(carRepository.findByPublicId(publicId1)).thenReturn(Optional.of(car1));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
            Car carToSave = invocation.getArgument(0);
            assertEquals(newUser, carToSave.getUser());
            return carToSave;
        });

        Car result = carService.updateCar(publicId1, updatedCarDetails);

        assertNotNull(result.getUser());
        assertEquals(newUser, result.getUser());
        assertEquals("Model X", result.getModel());
        verify(carRepository, times(1)).findByPublicId(publicId1);
        verify(carRepository, times(1)).save(any(Car.class));
    }
}
