package com.electricitybusiness.api.service;

import com.electricitybusiness.api.dto.address.AddressDTO;
import com.electricitybusiness.api.mapper.EntityMapper;
import com.electricitybusiness.api.model.Address;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.model.UserRole;
import com.electricitybusiness.api.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {
    @Mock
    private AddressRepository addressRepository;

    @Mock
    private EntityMapper entityMapper;

    @InjectMocks
    private AddressService addressService;

    private User testUser;
    private Address address1;
    private Address address2;
    private AddressDTO addressDTO1;
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

        address1 = new Address();
        address1.setIdAddress(id1);
        address1.setPublicId(publicId1);
        address1.setNameAddress("Domicile");
        address1.setAddress("10 Rue de la Paix");
        address1.setPostCode("12345");
        address1.setCity("Paris");
        address1.setRegion("Paris");
        address1.setCountry("United States");
        address1.setComplement("Complement");
        address1.setFloor("2nd Floor");
        address1.setMainAddress(true);
        address1.setUser(testUser);

        address2 = new Address();
        address2.setIdAddress(id2);
        address2.setPublicId(publicId2);
        address2.setNameAddress("Bureau");
        address2.setAddress("20 Avenue des Champs");
        address2.setPostCode("67890");
        address2.setCity("Lyon");
        address2.setRegion("Auvergne-Rhône-Alpes");
        address2.setCountry("France");
        address2.setComplement("Complement 2");
        address2.setFloor("3rd Floor");
        address2.setMainAddress(false);
        address2.setUser(testUser);

        addressDTO1 = new AddressDTO(
                address1.getPublicId(),
                address1.getNameAddress(),
                address1.getAddress(),
                address1.getPostCode(),
                address1.getCity(),
                address1.getCountry(),
                address1.getRegion(),
                address1.getComplement(),
                address1.getFloor(),
                address1.getMainAddress(),
                this.entityMapper.toListPLaceDTO(address1.getPlaces())
        );
    }

    /**
     * Teste la méthode getAllAddresses() du service AddressService.
     * Vérifie que la liste des adresses retournée correspond aux attentes.
     */
    @Test
    void getAllAddresses_shouldReturnListOfAddresses() {
        // Préparation du mock
        List<Address> addresses = Arrays.asList(address1, address2);
        when(addressRepository.findAll()).thenReturn(addresses);

        // Exécution
        List<Address> result = addressService.getAllAddresses();

        // Vérification
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(address1, result.get(0));
        assertEquals(address2, result.get(1));
        verify(addressRepository, times(1)).findAll();
    }

    /**
     * Teste la méthode getAddressById() du service AddressService.
     * Vérifie que l'adresse retournée correspond à l'adresse mockée.
     */
    @Test
    void getAddressById_shouldReturnAddressWhenFound() {
        // Preparation
        when(addressRepository.findById(id1)).thenReturn(Optional.of(address1));

        // Execution
        Optional<Address> result = addressService.getAddressById(id1);

        // Assertion
        assertTrue(result.isPresent());
        assertEquals(address1, result.get());
        verify(addressRepository, times(1)).findById(id1);
    }

    /**
     * Teste la méthode getAddressById() du service AddressService.
     * Vérifie que l'Optional retourné est vide lorsque l'adresse n'est pas trouvée.
     */
    @Test
    void getAddressById_shouldReturnEmptyOptionalWhenNotFound() {
        // Préparation du mock
        when(addressRepository.findById(99L)).thenReturn(Optional.empty());

        // Exécution
        Optional<Address> result = addressService.getAddressById(99L);

        // Vérification
        assertFalse(result.isPresent());
        verify(addressRepository, times(1)).findById(99L);
    }

    /**
     * Teste la méthode saveAddress() du service AddressService.
     * Vérifie que l'adresse sauvegardée est retournée correctement.
     */
    @Test
    void saveAddress_shouldReturnSavedAddress() {
        // Préparation du mock
        when(addressRepository.save(any(Address.class))).thenReturn(address1);

        // Exécution
        Address result = addressService.saveAddress(address1);

        // Vérification
        assertNotNull(result);
        assertEquals(address1, result);
        verify(addressRepository, times(1)).save(address1);
    }

    /**
     * Teste la méthode updateAddress() du service AddressService.
     * Vérifie que l'adresse mise à jour est retournée correctement.
     */
    @Test
    void updateAddress_byId_shouldReturnUpdatedAddress() {
        // Préparation du mock
        Address updatedAddressPayload = new Address();
        updatedAddressPayload.setAddress("Nouvelle Rue");
        updatedAddressPayload.setCity("Nouvelle Ville");
        updatedAddressPayload.setPublicId(UUID.randomUUID());

        Address expectedSavedAddress = new Address();
        expectedSavedAddress.setIdAddress(id1);
        expectedSavedAddress.setPublicId(updatedAddressPayload.getPublicId());
        expectedSavedAddress.setAddress("Nouvelle Rue");
        expectedSavedAddress.setCity("Nouvelle Ville");

        when(addressRepository.save(any(Address.class))).thenReturn(expectedSavedAddress);

        // Exécution
        Address result = addressService.updateAddress(id1, updatedAddressPayload);

        // Vérification
        assertNotNull(result);
        assertEquals(id1, result.getIdAddress());
        assertEquals("Nouvelle Rue", result.getAddress());
        assertEquals("Nouvelle Ville", result.getCity());
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    /**
     * Teste la méthode deleteAddressById() du service AddressService.
     * Vérifie que la méthode deleteById() du repository est appelée une fois.
     */
    @Test
    void deleteAddressById_shouldCallRepositoryDeleteById() {
        // Exécution
        addressService.deleteAddressById(id1);

        // Vérification
        verify(addressRepository, times(1)).deleteById(id1);
    }

    /**
     * Teste la méthode existsById() du service AddressService.
     * Vérifie que la méthode retourne true lorsque l'adresse existe.
     */
    @Test
    void existsById_shouldReturnTrueWhenAddressExists() {
        // Préparation du mock
        when(addressRepository.existsById(id1)).thenReturn(true);

        // Exécution
        boolean exists = addressService.existsById(id1);

        // Vérification
        assertTrue(exists);
        verify(addressRepository, times(1)).existsById(id1);
    }

    /**
     * Teste la méthode existsById() du service AddressService.
     * Vérifie que la méthode retourne false lorsque l'adresse n'existe pas.
     */
    @Test
    void existsById_shouldReturnFalseWhenAddressDoesNotExist() {
        // Préparation du mock
        when(addressRepository.existsById(99L)).thenReturn(false);

        // Exécution
        boolean exists = addressService.existsById(99L);

        // Vérification
        assertFalse(exists);
        verify(addressRepository, times(1)).existsById(99L);
    }

    /**
     * Teste la méthode getAddressesByUser() du service AddressService.
     * Vérifie que la liste des adresses retournée pour un utilisateur donné correspond aux attentes.
     */
    @Test
    void getAddressesByUser_shouldReturnListOfAddresses() {
        // Préparation du mock
        List<Address> userAddresses = Arrays.asList(address1, address2);
        when(addressRepository.findAddressesByUser(testUser)).thenReturn(userAddresses);

        // Exécution
        List<Address> result = addressService.getAddressesByUser(testUser);

        // Vérification
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(address1, result.get(0));
        assertEquals(address2, result.get(1));
        verify(addressRepository, times(1)).findAddressesByUser(testUser);
    }

    /**
     * Teste la méthode deleteAddressByPublicId() du service AddressService.
     * Vérifie que la méthode deleteAddressByPublicId() du repository est appelée une fois.
     */
    @Test
    void deleteAddressByPublicId_shouldCallRepositoryDeleteByPublicId() {
        // Exécution
        addressService.deleteAddressByPublicId(publicId1);

        // Vérification
        verify(addressRepository, times(1)).deleteAddressByPublicId(publicId1);
    }

    /**
     * Teste la méthode existsByPublicId() du service AddressService.
     * Vérifie que la méthode retourne true lorsque l'adresse existe.
     */
    @Test
    void existsByPublicId_shouldReturnTrueWhenAddressExists() {
        // Préparation du mock
        when(addressRepository.findByPublicId(publicId1)).thenReturn(Optional.of(address1));

        // Exécution
        boolean exists = addressService.existsByPublicId(publicId1);

        // Vérification
        assertTrue(exists);
        verify(addressRepository, times(1)).findByPublicId(publicId1);
    }

    /**
     * Teste la méthode existsByPublicId() du service AddressService.
     * Vérifie que la méthode retourne false lorsque l'adresse n'existe pas.
     */
    @Test
    void existsByPublicId_shouldReturnFalseWhenAddressDoesNotExist() {
        // Préparation du mock
        when(addressRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // Exécution
        boolean exists = addressService.existsByPublicId(UUID.randomUUID());

        // Vérification
        assertFalse(exists);
        verify(addressRepository, times(1)).findByPublicId(any(UUID.class));
    }

    /**
     * Teste la méthode updateAddress() du service AddressService en utilisant le publicId.
     * Vérifie que l'adresse mise à jour est retournée correctement et que l'utilisateur existant est conservé si le payload utilisateur est null.
     */
    @Test
    void updateAddress_byPublicId_shouldReturnUpdatedAddress_andRetainExistingUserIfPayloadUserIsNull() {
        Address existingAddress = new Address();
        existingAddress.setIdAddress(id1);
        existingAddress.setPublicId(publicId1);
        existingAddress.setAddress("Ancienne Rue");
        existingAddress.setCity("Ancienne Ville");
        existingAddress.setUser(testUser);

        when(addressRepository.findByPublicId(publicId1)).thenReturn(Optional.of(existingAddress));

        Address updatedAddressPayload = new Address();
        updatedAddressPayload.setAddress("Nouvelle Rue");
        updatedAddressPayload.setCity("Nouvelle Ville");
        updatedAddressPayload.setUser(null);

        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address savedAddress = invocation.getArgument(0);
            assertEquals(existingAddress.getIdAddress(), savedAddress.getIdAddress());
            assertEquals(existingAddress.getPublicId(), savedAddress.getPublicId());
            assertEquals(existingAddress.getUser(), savedAddress.getUser());
            return savedAddress;
        });

        // Exécution
        Address result = addressService.updateAddress(publicId1, updatedAddressPayload);

        // Vérification
        assertNotNull(result);
        assertEquals(existingAddress.getIdAddress(), result.getIdAddress());
        assertEquals(existingAddress.getPublicId(), result.getPublicId());
        assertEquals("Nouvelle Rue", result.getAddress());
        assertEquals("Nouvelle Ville", result.getCity());
        assertEquals(testUser, result.getUser());
        verify(addressRepository, times(1)).findByPublicId(publicId1);
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    /**
     * Teste la méthode updateAddress() du service AddressService en utilisant le publicId.
     * Vérifie que l'adresse mise à jour est retournée correctement et que l'utilisateur est mis à jour si le payload utilisateur n'est pas null.
     */
    @Test
    void updateAddress_byPublicId_shouldReturnUpdatedAddress_andUpdateUserIfPayloadUserIsNotNull() {
        Address existingAddress = new Address();
        existingAddress.setIdAddress(id1);
        existingAddress.setPublicId(publicId1);
        existingAddress.setAddress("Ancienne Rue");
        existingAddress.setCity("Ancienne Ville");
        existingAddress.setUser(testUser);

        when(addressRepository.findByPublicId(publicId1)).thenReturn(Optional.of(existingAddress));

        User newUser = new User();
        newUser.setIdUser(2L);
        newUser.setFirstName("Jane");
        newUser.setSurnameUser("Doe");
        newUser.setEmailUser("jane.doe@example.com");

        Address updatedAddressPayload = new Address();
        updatedAddressPayload.setAddress("Nouvelle Rue avec nouvel utilisateur");
        updatedAddressPayload.setCity("Nouvelle Ville avec nouvel utilisateur");
        updatedAddressPayload.setUser(newUser);

        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address savedAddress = invocation.getArgument(0);
            assertEquals(existingAddress.getIdAddress(), savedAddress.getIdAddress());
            assertEquals(existingAddress.getPublicId(), savedAddress.getPublicId());
            assertEquals(newUser, savedAddress.getUser());
            return savedAddress;
        });

        // Exécution
        Address result = addressService.updateAddress(publicId1, updatedAddressPayload);

        // Vérification
        assertNotNull(result);
        assertEquals(existingAddress.getIdAddress(), result.getIdAddress());
        assertEquals(existingAddress.getPublicId(), result.getPublicId());
        assertEquals("Nouvelle Rue avec nouvel utilisateur", result.getAddress());
        assertEquals("Nouvelle Ville avec nouvel utilisateur", result.getCity());
        assertEquals(newUser, result.getUser()); // L'utilisateur doit être le nouveau
        verify(addressRepository, times(1)).findByPublicId(publicId1);
        verify(addressRepository, times(1)).save(any(Address.class));
    }

    /**
     * Teste la méthode updateAddress() du service AddressService en utilisant le publicId.
     * Vérifie qu'une exception est lancée lorsque l'adresse n'est pas trouvée.
     */
    @Test
    void updateAddress_byPublicId_shouldThrowExceptionWhenAddressNotFound() {
        when(addressRepository.findByPublicId(publicId1)).thenReturn(Optional.empty());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () ->
                addressService.updateAddress(publicId1, new Address()));

        assertEquals("Address with publicId not found: " + publicId1, thrown.getMessage());
        verify(addressRepository, times(1)).findByPublicId(publicId1);
        verify(addressRepository, never()).save(any(Address.class)); // Vérifie que save n'a pas été appelé
    }

    /**
     * Teste la méthode getAddressDTOByPublicId() du service AddressService.
     * Vérifie que la réponse HTTP est correcte lorsque l'adresse est trouvée.
     */
    @Test
    void getAddressDTOByPublicId_shouldReturnOkWithDTOWhenAddressFound() {
        // Préparation du mock
        when(addressRepository.findByPublicId(publicId1)).thenReturn(Optional.of(address1));
        when(entityMapper.toAddressDTO(address1)).thenReturn(addressDTO1);

        // Exécution
        ResponseEntity<AddressDTO> response = addressService.getAddressDTOByPublicId(publicId1);

        // Vérification
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(addressDTO1, response.getBody());
        verify(addressRepository, times(1)).findByPublicId(publicId1);
        verify(entityMapper, times(1)).toAddressDTO(address1);
    }

    /**
     * Teste la méthode getAddressDTOByPublicId() du service AddressService.
     * Vérifie que la réponse HTTP est correcte lorsque l'adresse n'est pas trouvée.
     */
    @Test
    void getAddressDTOByPublicId_shouldReturnNotFoundWhenAddressNotFound() {
        // Préparation du mock
        when(addressRepository.findByPublicId(any(UUID.class))).thenReturn(Optional.empty());

        // Exécution
        ResponseEntity<AddressDTO> response = addressService.getAddressDTOByPublicId(UUID.randomUUID());

        // Vérification
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(addressRepository, times(1)).findByPublicId(any(UUID.class));
        verify(entityMapper, never()).toAddressDTO(any(Address.class)); // Mapper ne doit pas être appelé
    }
}
