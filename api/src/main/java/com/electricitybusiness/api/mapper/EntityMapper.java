package com.electricitybusiness.api.mapper;

import com.electricitybusiness.api.dto.*;
import com.electricitybusiness.api.model.*;
import com.electricitybusiness.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Year;

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

    // === User ===
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

    // === User CREATE ===

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

    // === Mettre a jour User ===

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

    // === Mettre a jour le mot de passe User ===

    public User toEntityPassword(UserUpdatePasswordDTO dto, User existing) {
        existing.setPasswordUser(passwordEncoder.encode(dto.getPasswordUser()));
        return existing;
    }

    // === Mettre a jour le statut BANNI User ===

    public User toEntityBanished(UserUpdateBanishedDTO dto, User existing) {
        existing.setBanished(dto.getBanished());
        return existing;
    }

    // === Mettre a jour le role User ===

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


    // === MEDIA ===
    public MediaDTO toDTO(Media media) {
        if (media == null) return null;
        return new MediaDTO(
                media.getNameMedia(),
                media.getType(),
                media.getUrl(),
                media.getDescriptionMedia(),
                media.getSize(),
                media.getDateCreation(),
                media.getUser() != null ? media.getUser().getIdUser() : null
        );
    }

    public Media toEntity(MediaDTO dto) {
        if (dto == null) return null;
        Media media = new Media();
        media.setNameMedia(dto.getNameMedia());
        media.setType(dto.getType());
        media.setUrl(dto.getUrl());
        media.setDescriptionMedia(dto.getDescriptionMedia());
        media.setSize(dto.getSize());
        media.setDateCreation(dto.getDateCreation());
        media.setUser(dto.getIdUser() != null ? userRepository.getReferenceById(dto.getIdUser()) : null);

        return media;
    }

    public Media toEntity(MediaDTO dto, Media existing) {
        existing.setNameMedia(dto.getNameMedia());
        existing.setType(dto.getType());
        existing.setUrl(dto.getUrl());
        existing.setDescriptionMedia(dto.getDescriptionMedia());
        existing.setSize(dto.getSize());
        existing.setDateCreation(dto.getDateCreation());

        return existing;
    }


    // === Car ===

    public CarDTO toDTO(Car car) {
        if (car == null) return null;
        return new CarDTO(
                car.getPublicId(),
                car.getLicensePlate(),
                car.getBrand(),
                car.getModel(),
                car.getYear().getValue(),
                car.getBatteryCapacity(),
                car.getUser() != null ? car.getUser().getIdUser() : null
        );
    }

    public Car toEntity(CarDTO dto) {
        if (dto == null) return null;
        Car car = new Car();
        car.setLicensePlate(dto.getLicensePlate());
        car.setBrand(dto.getBrand());
        car.setModel(dto.getModel());
        car.setYear(Year.of(dto.getYear()));
        car.setBatteryCapacity(dto.getBatteryCapacity());
        car.setUser(dto.getIdUser() != null ? userRepository.getReferenceById(dto.getIdUser()) : null);
        return car;
    }

    public CarCreateDTO toDTOCreate(Car car) {
        if (car == null) return null;
        return new CarCreateDTO(
                car.getLicensePlate(),
                car.getBrand(),
                car.getModel(),
                car.getYear().getValue(),
                car.getBatteryCapacity(),
                car.getUser() != null ? car.getUser().getIdUser() : null
        );
    }

    public Car toEntityCreate(CarCreateDTO dto, Long idUser) {
        if (dto == null) return null;
        Car car = new Car();
        car.setLicensePlate(dto.getLicensePlate());
        car.setBrand(dto.getBrand());
        car.setModel(dto.getModel());
        car.setYear(dto.getYear() != null ? Year.of(dto.getYear()) : null);

/*
        car.setYear(Year.of(dto.getYear()));
*/
        car.setBatteryCapacity(dto.getBatteryCapacity());

        // Set the user directly using the ID
/*        User user = new User();
        user.setIdUser(idUser);
        car.setUser(user);*/

/*
        User user = userRepository.getReferenceById(idUser);
*/
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + idUser));
        car.setUser(user);

        System.out.println("[DEBUG] Car before save -> " + car);
        System.out.println("[DEBUG] User linked -> " + user.getIdUser());

/*        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        car.setUser(user);*/

        return car;
    }


    // === terminal ===
    public TerminalDTO toDTO(Terminal terminal) {
        if (terminal == null) return null;
        return new TerminalDTO(
                terminal.getNameTerminal(),
                terminal.getLatitude(),
                terminal.getLongitude(),
                terminal.getPrice(),
                terminal.getPower(),
                terminal.getInstructionTerminal(),
                terminal.getStanding(),
                terminal.getStatusTerminal(),
                terminal.getOccupied(),
                terminal.getDateCreation(),
                terminal.getLastMaintenance()
        );
    }

    public Terminal toEntity(TerminalDTO dto) {
        if (dto == null) return null;
        Terminal terminal = new Terminal();
        terminal.setNameTerminal(dto.getNameTerminal());
        terminal.setLatitude(dto.getLatitude());
        terminal.setLongitude(dto.getLongitude());
        terminal.setPrice(dto.getPrice());
        terminal.setPower(dto.getPower());
        terminal.setInstructionTerminal(dto.getInstructionTerminal());
        terminal.setStanding(dto.getStanding());
        terminal.setStatusTerminal(dto.getStatusTerminal());
        terminal.setOccupied(dto.getOccupied());
        terminal.setDateCreation(dto.getDateCreation());
        terminal.setLastMaintenance(dto.getLastMaintenance());
        return terminal;
    }


    // === SERVICE SUPPLEMENTAIRE ===

    public OptionDTO toDTO(Option serviceSup) {
        if (serviceSup == null) return null;
        return new OptionDTO(
                serviceSup.getNameOption(),
                serviceSup.getPriceOption(),
                serviceSup.getDescriptionOption()
        );
    }

    public Option toEntity(OptionDTO dto) {
        if (dto == null) return null;
        Option serviceSup = new Option();
        serviceSup.setNameOption(dto.getNameOption());
        serviceSup.setPriceOption(dto.getPriceOption());
        serviceSup.setDescriptionOption(dto.getDescriptionOption());
        return serviceSup;
    }

    // === booking ===
    public BookingDTO toDTO(Booking booking) {
        if (booking == null) return null;
        return new BookingDTO(
                booking.getNumberBooking(),
                booking.getStartingDate(),
                booking.getEndingDate(),
                booking.getStatusBooking(),
                booking.getTotalAmount(),
                booking.getPaymentDate(),
                booking.getUser() != null ? booking.getUser().getIdUser() : null,
                booking.getTerminal() != null ? booking.getTerminal().getIdTerminal() : null,
                booking.getCar() != null ? booking.getCar().getIdCar() : null,
                booking.getOption() != null ? booking.getOption().getIdOption() : null
        );
    }

    public Booking toEntity(BookingDTO dto) {
        if (dto == null) return null;
        Booking booking = new Booking();
        booking.setStartingDate(dto.getStartingDate());
        booking.setEndingDate(dto.getEndingDate());
        booking.setStatusBooking(dto.getStatusBooking());
        booking.setTotalAmount(dto.getTotalAmount());
        return booking;
    }


    // === REPARATEUR ===

    public RepairerDTO toDTO(Repairer repairer) {
        if (repairer == null) return null;
        return new RepairerDTO(
                repairer.getNameRepairer(),
                repairer.getEmailRepairer()
        );
    }

    public Repairer toEntity(RepairerDTO dto) {
        if (dto == null) return null;
        Repairer repairer = new Repairer();
        repairer.setNameRepairer(dto.getNameRepairer());
        repairer.setEmailRepairer(dto.getEmailRepairer());
        return repairer;
    }
    
}
