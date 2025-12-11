package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Place;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.repository.PlaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlaceServiceTest {
    @Mock
    private PlaceRepository placeRepository;

    @InjectMocks
    private PlaceService placeService;

    private Place place1;
    private Place place2;
    private User user1;
    private UUID publicId1;
    private UUID publicId2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setIdUser(1L);
        user1.setSurnameUser("user1");

        publicId1 = UUID.randomUUID();
        publicId2 = UUID.randomUUID();

        place1 = new Place();
        place1.setIdPlace(1L);
        place1.setPublicId(publicId1);
        place1.setUser(user1);

        place2 = new Place();
        place2.setIdPlace(2L);
        place2.setPublicId(publicId2);
        place2.setUser(user1);
    }

    /**
     * Test de la méthode getAllPlaces pour vérifier qu'elle retourne toutes les places.
     */
    @Test
    void testGetAllPlaces() {
        // Arrange
        List<Place> expectedPlaces = List.of(place1, place2);
        when(placeRepository.findAll()).thenReturn(expectedPlaces);

        // Act
        List<Place> actualPlaces = placeService.getAllPlaces();

        // Assert
        assertThat(actualPlaces).isNotNull();
        assertThat(actualPlaces).hasSize(2);
        assertThat(actualPlaces).containsExactlyInAnyOrder(place1, place2);
        verify(placeRepository, times(1)).findAll();
    }

    /**
     * Test de la méthode getPlaceById pour vérifier qu'elle retourne la place correcte lorsqu'elle existe.
     */
    @Test
    void testGetPlaceById_WhenPlaceExists() {
        // Arrange
        when(placeRepository.findById(1L)).thenReturn(Optional.of(place1));

        // Act
        Optional<Place> result = placeService.getPlaceById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(place1);
        verify(placeRepository, times(1)).findById(1L);
    }

    /**
     * Test de la méthode getPlaceById pour vérifier qu'elle retourne un Optional vide lorsqu'elle n'existe pas.
     */
    @Test
    void testGetPlaceById_WhenPlaceDoesNotExist() {
        // Arrange
        when(placeRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Place> result = placeService.getPlaceById(999L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.isPresent()).isFalse();
        verify(placeRepository, times(1)).findById(999L);
    }

    /**
     * Test de la méthode savePlace pour vérifier qu'elle enregistre correctement une nouvelle place.
     */
    @Test
    void testSavePlace() {
        // Arrange
        Place newPlace = new Place();
        newPlace.setPublicId(UUID.randomUUID());
        newPlace.setUser(user1);

        Place savedPlace = new Place();
        savedPlace.setIdPlace(3L);
        savedPlace.setPublicId(newPlace.getPublicId());
        savedPlace.setUser(newPlace.getUser());

        when(placeRepository.save(any(Place.class))).thenReturn(savedPlace);

        // Act
        Place result = placeService.savePlace(newPlace);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIdPlace()).isEqualTo(savedPlace.getIdPlace());
        assertThat(result.getPublicId()).isEqualTo(savedPlace.getPublicId());
        assertThat(result.getUser()).isEqualTo(savedPlace.getUser());
        verify(placeRepository, times(1)).save(any(Place.class));
    }

    /**
     * Test de la méthode updatePlace pour vérifier qu'elle met à jour correctement une place existante.
     */
    @Test
    void testUpdatePlace_WhenPlaceExists() {
        // Arrange
        Place updatedPlace = new Place();
        updatedPlace.setPublicId(publicId1);
        updatedPlace.setUser(user1);

        Place existingPlace = new Place();
        existingPlace.setIdPlace(1L);
        existingPlace.setPublicId(publicId1);
        existingPlace.setUser(user1);

        when(placeRepository.findByPublicId(publicId1)).thenReturn(Optional.of(existingPlace));
        when(placeRepository.save(any(Place.class))).thenReturn(existingPlace);

        // Act
        Place result = placeService.updatePlace(publicId1, updatedPlace);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIdPlace()).isEqualTo(existingPlace.getIdPlace());
        assertThat(result.getPublicId()).isEqualTo(existingPlace.getPublicId());
        assertThat(result.getUser()).isEqualTo(existingPlace.getUser());
        verify(placeRepository, times(1)).findByPublicId(publicId1);
        verify(placeRepository, times(1)).save(any(Place.class));
    }

    /**
     * Test de la méthode updatePlace pour vérifier qu'elle lance une exception lorsqu'on tente de mettre à jour une place inexistante.
     */
    @Test
    void testUpdatePlace_WhenPlaceDoesNotExist() {
        // Arrange
        Place updatedPlace = new Place();
        updatedPlace.setPublicId(publicId1);
        updatedPlace.setUser(user1);

        when(placeRepository.findByPublicId(publicId1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> placeService.updatePlace(publicId1, updatedPlace));
        verify(placeRepository, times(1)).findByPublicId(publicId1);
        verify(placeRepository, never()).save(any(Place.class));
    }

    /**
     * Test de la méthode deletePlaceById pour vérifier qu'elle supprime correctement une place existante.
     */
    @Test
    void testDeletePlaceById_WhenPlaceExists() {
        // Arrange
        doNothing().when(placeRepository).deleteById(1L);

        // Act
        placeService.deletePlaceById(1L);

        // Assert
        verify(placeRepository, times(1)).deleteById(1L);
    }

    /**
     * Test de la méthode deletePlaceById pour vérifier qu'elle gère correctement la suppression d'une place inexistante.
     */
    @Test
    void testDeletePlaceById_WhenPlaceDoesNotExist() {
        // Arrange
        doNothing().when(placeRepository).deleteById(999L);

        // Act
        placeService.deletePlaceById(999L);

        // Assert
        verify(placeRepository, times(1)).deleteById(999L);
    }

    /**
     * Test de la méthode existsById pour vérifier son comportement.
     */
    @Test
    void testExistsById_WhenPlaceExists() {
        // Arrange
        when(placeRepository.existsById(1L)).thenReturn(true);

        // Act
        boolean result = placeService.existsById(1L);

        // Assert
        assertThat(result).isTrue();
        verify(placeRepository, times(1)).existsById(1L);
    }

    /**
     * Test de la méthode existsById pour vérifier son comportement lorsque la place n'existe pas.
     */
    @Test
    void testExistsById_WhenPlaceDoesNotExist() {
        // Arrange
        when(placeRepository.existsById(999L)).thenReturn(false);

        // Act
        boolean result = placeService.existsById(999L);

        // Assert
        assertThat(result).isFalse();
        verify(placeRepository, times(1)).existsById(999L);
    }

    /**
     * Test de la méthode getPlacesByUser pour vérifier qu'elle retourne les places associées à un utilisateur donné.
     */
    @Test
    void testGetPlacesByUser() {
        // Arrange
        List<Place> expectedPlaces = List.of(place1, place2);
        when(placeRepository.findPlacesByUser(user1)).thenReturn(expectedPlaces);

        // Act
        List<Place> result = placeService.getPlacesByUser(user1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(place1, place2);
        verify(placeRepository, times(1)).findPlacesByUser(user1);
    }

    /**
     * Test de la méthode deletePlaceByPublicId pour vérifier qu'elle supprime correctement une place par son identifiant public.
     */
    @Test
    void testDeletePlaceByPublicId() {
        // Arrange
        doNothing().when(placeRepository).deletePlaceByPublicId(publicId1);

        // Act
        placeService.deletePlaceByPublicId(publicId1);

        // Assert
        verify(placeRepository, times(1)).deletePlaceByPublicId(publicId1);
    }

    /**
     * Test de la méthode existsByPublicId pour vérifier son comportement.
     */
    @Test
    void testExistsByPublicId_WhenPlaceExists() {
        // Arrange
        when(placeRepository.findByPublicId(publicId1)).thenReturn(Optional.of(place1));

        // Act
        boolean result = placeService.existsByPublicId(publicId1);

        // Assert
        assertThat(result).isTrue();
        verify(placeRepository, times(1)).findByPublicId(publicId1);
    }

    /**
     * Test de la méthode existsByPublicId pour vérifier son comportement lorsque la place n'existe pas.
     */
    @Test
    void testExistsByPublicId_WhenPlaceDoesNotExist() {
        // Arrange
        UUID publicId = UUID.randomUUID();
        when(placeRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // Act
        boolean result = placeService.existsByPublicId(publicId);

        // Assert
        assertThat(result).isFalse();
        verify(placeRepository, times(1)).findByPublicId(publicId);
    }

    /**
     * Test de la méthode updatePlaceByPublicId pour vérifier qu'elle met à jour correctement une place existante.
     */
    @Test
    void testUpdatePlaceByPublicId_WhenPlaceExists() {
        // Arrange
        Place updatedPlace = new Place();
        updatedPlace.setPublicId(publicId1);
        updatedPlace.setUser(user1);

        when(placeRepository.findByPublicId(publicId1)).thenReturn(Optional.of(place1));
        when(placeRepository.save(any(Place.class))).thenReturn(place1);

        // Act
        Place result = placeService.updatePlace(publicId1, updatedPlace);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIdPlace()).isEqualTo(place1.getIdPlace());
        assertThat(result.getPublicId()).isEqualTo(place1.getPublicId());
        assertThat(result.getUser()).isEqualTo(place1.getUser());
        verify(placeRepository, times(1)).findByPublicId(publicId1);
        verify(placeRepository, times(1)).save(any(Place.class));
    }

    /**
     * Test de la méthode updatePlaceByPublicId pour vérifier qu'elle lance une exception lorsqu'on tente de mettre à jour une place inexistante.
     */
    @Test
    void testUpdatePlaceByPublicId_WhenPlaceDoesNotExist() {
        // Arrange
        Place updatedPlace = new Place();
        updatedPlace.setPublicId(publicId1);
        updatedPlace.setUser(user1);

        when(placeRepository.findByPublicId(publicId1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> placeService.updatePlace(publicId1, updatedPlace));
        verify(placeRepository, times(1)).findByPublicId(publicId1);
        verify(placeRepository, never()).save(any(Place.class));
    }

    /**
     * Test de la méthode updatePlaceByPublicId pour vérifier qu'elle gère correctement la mise à jour
     * lorsque l'utilisateur de la place mise à jour est null.
     */
    @Test
    void testUpdatePlaceByPublicId_WhenUserIsNull() {
        // Arrange
        Place updatedPlace = new Place();
        updatedPlace.setPublicId(publicId1);
        updatedPlace.setUser(null);

        when(placeRepository.findByPublicId(publicId1)).thenReturn(Optional.of(place1));
        when(placeRepository.save(any(Place.class))).thenAnswer(invocation -> {
            Place placeToSave = invocation.getArgument(0);
            placeToSave.setIdPlace(place1.getIdPlace());
            return placeToSave;
        });

        // Act
        Place result = placeService.updatePlace(publicId1, updatedPlace);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIdPlace()).isEqualTo(place1.getIdPlace());
        assertThat(result.getPublicId()).isEqualTo(place1.getPublicId());
        assertThat(result.getUser()).isEqualTo(place1.getUser());
        verify(placeRepository, times(1)).findByPublicId(publicId1);
        verify(placeRepository, times(1)).save(any(Place.class));
    }
}
