package com.electricitybusiness.api.service;

import com.electricitybusiness.api.model.Media;
import com.electricitybusiness.api.repository.CarRepository;
import com.electricitybusiness.api.repository.MediaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MediaServiceTest {
    @Mock
    private MediaRepository mediaRepository; // Dépendance de MediaService

    @Mock
    private CarService carService;

    @InjectMocks
    private MediaService mediaService; // Le service que nous allons tester

    // Données de test communes
    private Media media1;
    private Media media2;
    private Long id1 = 1L;
    private Long id2 = 2L;

    @BeforeEach
    void setUp() {
        // Initialisation des médias de test
        media1 = new Media(
                id1,
                "ImageProfil",
                "http://example.com/profile.jpg",
                "image/jpeg",
                "Photo de profil",
                "150KB",
                LocalDateTime.now(),
                null,
                null,
                null,
                null
        );

        media2 = new Media(
                id2,
                "VideoPromo",
                "http://example.com/promo.mp4",
                "video/mp4", "Vidéo promotionnelle",
                "5MB",
                LocalDateTime.now(),
                null,
                null,
                null,
                null
        );
    }

    @Test
    void getAllMedias_shouldReturnListOfMedias() {
        // Préparation du mock: quand findAll() est appelé, il doit retourner nos médias de test
        List<Media> medias = Arrays.asList(media1, media2);
        when(mediaRepository.findAll()).thenReturn(medias);

        // Exécution: appel de la méthode du service
        List<Media> result = mediaService.getAllMedias();

        // Vérification
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(media1, result.get(0));
        assertEquals(media2, result.get(1));
        // Vérifie que la méthode findAll du repository a été appelée exactement une fois
        verify(mediaRepository, times(1)).findAll();
    }

    @Test
    void getAllMedias_shouldReturnEmptyList_whenNoMediasExist() {
        // Préparation du mock: quand findAll() est appelé, il doit retourner une liste vide
        when(mediaRepository.findAll()).thenReturn(Collections.emptyList());

        // Exécution
        List<Media> result = mediaService.getAllMedias();

        // Vérification
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mediaRepository, times(1)).findAll();
    }

    @Test
    void getMediaById_shouldReturnMedia_whenFound() {
        // Préparation du mock: quand findById(id1) est appelé, il doit retourner un Optional contenant media1
        when(mediaRepository.findById(id1)).thenReturn(Optional.of(media1));

        // Exécution
        Optional<Media> result = mediaService.getMediaById(id1);

        // Vérification
        assertTrue(result.isPresent());
        assertEquals(media1, result.get());
        verify(mediaRepository, times(1)).findById(id1);
    }

    @Test
    void getMediaById_shouldReturnEmptyOptional_whenNotFound() {
        // Préparation du mock: quand findById(unIdInexistant) est appelé, il doit retourner un Optional vide
        when(mediaRepository.findById(99L)).thenReturn(Optional.empty());

        // Exécution
        Optional<Media> result = mediaService.getMediaById(99L);

        // Vérification
        assertFalse(result.isPresent());
        verify(mediaRepository, times(1)).findById(99L);
    }

    @Test
    void saveMedia_shouldReturnSavedMedia() {
        // Média sans ID pour simuler une nouvelle création
        Media newMedia = new Media(null, "DocumentPDF", "http://example.com/doc.pdf", "application/pdf", "Fiche technique", "1MB", LocalDateTime.now(), null, null, null, null);
        // Le repository doit retourner le média avec un ID après la sauvegarde
        when(mediaRepository.save(any(Media.class))).thenReturn(new Media(3L, newMedia.getNameMedia(), newMedia.getUrl(), newMedia.getType(), newMedia.getDescriptionMedia(), newMedia.getSize(), newMedia.getDateCreation(), null, null, null, null));

        // Exécution
        Media savedMedia = mediaService.saveMedia(newMedia);

        // Vérification
        assertNotNull(savedMedia.getIdMedia()); // L'ID doit avoir été assigné
        assertEquals("DocumentPDF", savedMedia.getNameMedia());
        verify(mediaRepository, times(1)).save(any(Media.class));
    }

    @Test
    void updateMedia_shouldReturnUpdatedMedia() {
        // Créer une version modifiée de media1
        Media updatedMediaDetails = new Media(id1, "ImageProfilV2", "http://example.com/profile_v2.jpg", "image/png", "Nouvelle photo de profil", "200KB", LocalDateTime.now(), null, null, null, null);

        // Le repository doit retourner le média mis à jour
        when(mediaRepository.save(any(Media.class))).thenReturn(updatedMediaDetails);

        // Exécution
        Media result = mediaService.updateMedia(id1, updatedMediaDetails);

        // Vérification
        assertNotNull(result);
        assertEquals(id1, result.getIdMedia());
        assertEquals("ImageProfilV2", result.getNameMedia());
        assertEquals("image/png", result.getType());
        verify(mediaRepository, times(1)).save(any(Media.class));
    }

    @Test
    void deleteMediaById_shouldCallRepositoryDelete() {
        // Exécution
        carService.deleteCarById(id1); // Correction: Doit appeler mediaService

        // Exécution corrigée
        mediaService.deleteMediaById(id1);

        // Vérification: s'assure que la méthode deleteById du repository a été appelée
        verify(mediaRepository, times(1)).deleteById(id1);
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        when(mediaRepository.existsById(id1)).thenReturn(true);

        boolean exists = mediaService.existsById(id1);

        assertTrue(exists);
        verify(mediaRepository, times(1)).existsById(id1);
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        when(mediaRepository.existsById(99L)).thenReturn(false);

        boolean exists = mediaService.existsById(99L);

        assertFalse(exists);
        verify(mediaRepository, times(1)).existsById(99L);
    }

    @Test
    void findByType_shouldReturnListOfMedias_whenFound() {
        List<Media> imageMedias = Collections.singletonList(media1);
        when(mediaRepository.findByType("image/jpeg")).thenReturn(imageMedias);

        List<Media> result = mediaService.findByType("image/jpeg");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(media1, result.get(0));
        verify(mediaRepository, times(1)).findByType("image/jpeg");
    }

    @Test
    void findByType_shouldReturnEmptyList_whenNotFound() {
        when(mediaRepository.findByType("application/json")).thenReturn(Collections.emptyList());

        List<Media> result = mediaService.findByType("application/json");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(mediaRepository, times(1)).findByType("application/json");
    }
}
