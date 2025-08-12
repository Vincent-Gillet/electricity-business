package com.electricitybusiness.api.mapper;

import com.electricitybusiness.api.dto.*;
import com.electricitybusiness.api.model.Address;
import com.electricitybusiness.api.model.Place;
import com.electricitybusiness.api.model.User;
import com.electricitybusiness.api.model.UserRole;
import com.electricitybusiness.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Mapper pour convertir entre entités JPA et DTOs
 * Évite les références circulaires en contrôlant la sérialisation
 */
@Component
public class EntityMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
    
    @Autowired
    private UserRepository userRepository;

    // === UTILISATEUR ===
    public UserDTO toDTO(User user) {
        if (user == null) return null;
        return new UserDTO(
                user.getSurnameUser(),
                user.getFirstName(),
                user.getUsername(),
                user.getEmailUser(),
                user.getRole(),
                user.getDateOfBirth(),
                user.getPhone(),
                user.getIban(),
                user.getBanished()
        );
    }

    public User toEntity(UserDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setSurnameUser(dto.getSurnameUser());
        user.setFirstName(dto.getFirstName());
        user.setUsername(dto.getUsername());
        user.setRole(dto.getRole());
        user.setEmailUser(dto.getEmailUser());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setPhone(dto.getPhone());
        user.setIban(dto.getIban());
        user.setBanished(dto.getBanished());
        return user;
    }

    // === UTILISATEUR CREATE ===

    public UserCreateDTO toCreateDTO(User user) {
        if (user == null) return null;
        return new UserCreateDTO(
                user.getSurnameUser(),
                user.getFirstName(),
                user.getUsername(),
                user.getEmailUser(),
                user.getPasswordUser(),
                user.getDateOfBirth(),
                user.getPhone()
        );
    }

    public User toEntity(UserCreateDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setSurnameUser(dto.getSurnameUser());
        user.setFirstName(dto.getFirstName());
        user.setUsername(dto.getUsername());
        user.setRole(UserRole.USER);
        user.setPasswordUser(passwordEncoder.encode(dto.getPasswordUser()));
        user.setEmailUser(dto.getEmailUser());
        user.setDateOfBirth(dto.getDateOfBirth());
        user.setPhone(dto.getPhone());
        user.setIban(null);
        user.setBanished(false);

        return user;
    }

    // === Mettre a jour UTILISATEUR ===

    public User toEntity(UserUpdateDTO dto, User existing) {
        existing.setSurnameUser(dto.getSurnameUser());
        existing.setFirstName(dto.getFirstName());
        existing.setUsername(dto.getUsername());
        existing.setEmailUser(dto.getEmailUser());
        existing.setDateOfBirth(dto.getDateOfBirth());
        existing.setPhone(dto.getPhone());
        existing.setIban(dto.getIban());
        return existing;
    }

    public UserUpdateDTO toUpdateDTO(User user) {
        if (user == null) return null;
        return new UserUpdateDTO(
                user.getSurnameUser(),
                user.getFirstName(),
                user.getUsername(),
                user.getEmailUser(),
                user.getDateOfBirth(),
                user.getPhone(),
                user.getIban()
        );
    }

    // === Mettre a jour le mot de passe UTILISATEUR ===

    public User toEntityPassword(UserUpdatePasswordDTO dto, User existing) {
        existing.setPasswordUser(passwordEncoder.encode(dto.getPasswordUser()));
        return existing;
    }

    // === Mettre a jour le statut BANNI UTILISATEUR ===

    public User toEntityBanished(UserUpdateBanishedDTO dto, User existing) {
        existing.setBanished(dto.getBanished());
        return existing;
    }

    // === Mettre a jour le role UTILISATEUR ===

    public User toEntityRole(UserUpdateRoleDTO dto, User existing) {
        existing.setRole(dto.getRole());
        return existing;
    }




    // === address ===
    public AddressDTO toDTO(Address address) {
        if (address == null) return null;
        return new AddressDTO(
                address.getNameAdress(),
                address.getAddress(),
                address.getPostCode(),
                address.getCity(),
                address.getCountry(),
                address.getRegion(),
                address.getComplement(),
                address.getFloor(),
                toDTO(address.getPlace())

        );
    }

    public Address toEntity(AddressDTO dto) {
        if (dto == null) return null;
        Address address = new Address();
        address.setNameAdress(dto.getNameAdress());
        address.setAddress(dto.getAddress());
        address.setPostCode(dto.getPostCode());
        address.setCity(dto.getCity());
        address.setCountry(dto.getCountry());
        address.setRegion(dto.getRegion());
        address.setComplement(dto.getComplement());
        address.setFloor(dto.getFloor());
        address.setPlace(toEntity(dto.getPlace()));
        return address;
    }


    // === LIEU ===
    public PlaceDTO toDTO(Place place) {
        if (place == null) return null;
        return new PlaceDTO(
                place.getInstructionPlace());
    }

    public Place toEntity(PlaceDTO dto) {
        if (dto == null) return null;
        Place place = new Place();
        place.setInstructionPlace(dto.getInstructionPlace());
        return place;
    }
}
