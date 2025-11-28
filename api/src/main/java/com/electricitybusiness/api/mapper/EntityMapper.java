package com.electricitybusiness.api.mapper;

import com.electricitybusiness.api.dto.*;
import com.electricitybusiness.api.dto.address.AddressCreateDTO;
import com.electricitybusiness.api.dto.address.AddressDTO;
import com.electricitybusiness.api.dto.booking.BookingCreateDTO;
import com.electricitybusiness.api.dto.booking.BookingDTO;
import com.electricitybusiness.api.dto.booking.BookingStatusDTO;
import com.electricitybusiness.api.dto.car.CarCreateDTO;
import com.electricitybusiness.api.dto.car.CarDTO;
import com.electricitybusiness.api.dto.option.OptionCreateDTO;
import com.electricitybusiness.api.dto.option.OptionDTO;
import com.electricitybusiness.api.dto.place.PlaceCreateDTO;
import com.electricitybusiness.api.dto.place.PlaceDTO;
import com.electricitybusiness.api.dto.place.PlaceUpdateDTO;
import com.electricitybusiness.api.dto.terminal.TerminalCreateDTO;
import com.electricitybusiness.api.dto.terminal.TerminalDTO;
import com.electricitybusiness.api.dto.user.*;
import com.electricitybusiness.api.model.*;
import com.electricitybusiness.api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * Mapper pour convertir entre entités JPA et DTOs
 * Évite les références circulaires en contrôlant la sérialisation
 */
@Component
@RequiredArgsConstructor
public class EntityMapper implements IEntityMapper {

    private final PasswordEncoder passwordEncoder;

    BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
    
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final PlaceRepository placeRepository;
    private final OptionRepository optionRepository;
    private final CarRepository carRepository;
    private final TerminalRepository terminalRepository;

    // === User ===
    @Override
    public UserDTO toDTO(User user) {
        if (user == null) return null;
        return new UserDTO(
                user.getSurnameUser(),
                user.getFirstName(),
                user.getPseudo(),
                user.getEmailUser(),
                user.getRole(),
                user.getDateOfBirth(),
                user.getPhone(),
                user.getIban(),
                user.getBanished()
        );
    }

    @Override
    public User toEntity(UserDTO dto) {
        if (dto == null) return null;
        User user = new User();
        user.setSurnameUser(dto.getSurnameUser());
        user.setFirstName(dto.getFirstName());
        user.setPseudo(dto.getPseudo());
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
                user.getPseudo(),
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
        user.setPseudo(dto.getPseudo());
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
        existing.setPseudo(dto.getPseudo());
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
                user.getPseudo(),
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
                address.getPublicId(),
                address.getNameAddress(),
                address.getAddress(),
                address.getPostCode(),
                address.getCity(),
                address.getCountry(),
                address.getRegion(),
                address.getComplement(),
                address.getFloor(),
                address.getMainAddress(),
                toDTO(address.getPlaces())
        );
    }

    public Address toEntity(AddressDTO dto) {
        if (dto == null) return null;
        Address address = new Address();
        address.setNameAddress(dto.getNameAddress());
        address.setAddress(dto.getAddress());
        address.setPostCode(dto.getPostCode());
        address.setCity(dto.getCity());
        address.setCountry(dto.getCountry());
        address.setRegion(dto.getRegion());
        address.setComplement(dto.getComplement());
        address.setFloor(dto.getFloor());
        address.setPlaces(toEntity(dto.getPlaces()));
        return address;
    }

    public Address toEntityCreate(AddressCreateDTO dto, Long idUser) {
        if (dto == null) return null;
        Address address = new Address();
        address.setNameAddress(dto.getNameAddress());
        address.setAddress(dto.getAddress());
        address.setPostCode(dto.getPostCode());
        address.setCity(dto.getCity());
        address.setCountry(dto.getCountry());
        address.setRegion(dto.getRegion());
        address.setComplement(dto.getComplement());
        address.setFloor(dto.getFloor());
/*
        address.setPlaces(toEntity(dto.getPlaces()));
*/
        System.out.println("Long idUser : " + idUser);
        address.setUser(idUser != null ? userRepository.getReferenceById(idUser) : null);
        return address;
    }

    public List<PlaceDTO> toDTO(List<Place> places) {
        if (places == null) return null;
        return places.stream()
                .map(this::toDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Place> toEntity(List<PlaceDTO> placeDTOs) {
        if (placeDTOs == null) return null;
        return placeDTOs.stream()
                .map(this::toEntity)
                .collect(java.util.stream.Collectors.toList());
    }

/*    public Car toEntityCreate(CarCreateDTO dto, Long idUser) {
        if (dto == null) return null;
        Car car = new Car();
        car.setLicensePlate(dto.getLicensePlate());
        car.setBrand(dto.getBrand());
        car.setModel(dto.getModel());
        car.setYear(dto.getYear() != null ? Year.of(dto.getYear()) : null);
        car.setBatteryCapacity(dto.getBatteryCapacity());

        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + idUser));
        car.setUser(user);

        return car;
    }*/


    // === LIEU ===
    public PlaceDTO toDTO(Place place) {
        if (place == null) return null;
        String addressName = place.getAddress() != null ? place.getAddress().getNameAddress() : null;
        return new PlaceDTO(
                place.getPublicId(),
                place.getInstructionPlace(),
                addressName,
                place.getAddress().getPublicId()
        );
    }

    public Place toEntity(PlaceDTO dto) {
        if (dto == null) return null;
        Place place = new Place();
            place.setPublicId(dto.getPublicId());
            place.setInstructionPlace(dto.getInstructionPlace());
        return place;
    }

    public Place toEntityCreate(PlaceCreateDTO dto, Long idUser, UUID idAddress) {
        if (dto == null) return null;
        Place place = new Place();
        place.setInstructionPlace(dto.getInstructionPlace());
        place.setUser(idUser != null ? userRepository.getReferenceById(idUser) : null);
        place.setAddress(idAddress != null ? addressRepository.findByPublicId(idAddress)
                .orElseThrow(() -> new RuntimeException("Adresse introuvable : " + idAddress)) : null);
        return place;
    }

    public Place toEntityUpdate(PlaceUpdateDTO dto, Long idUser, UUID idAddress) {
        if (dto == null) return null;
        Place place = new Place();
        place.setInstructionPlace(dto.getInstructionPlace());
        place.setUser(idUser != null ? userRepository.getReferenceById(idUser) : null);
        place.setAddress(idAddress != null ? addressRepository.findByPublicId(idAddress)
                .orElseThrow(() -> new RuntimeException("Adresse introuvable : " + idAddress)) : null);
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
        car.setBatteryCapacity(dto.getBatteryCapacity());
        User user = userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable : " + idUser));
        car.setUser(user);

        return car;
    }


    // === terminal ===
    public TerminalDTO toDTO(Terminal terminal) {
        if (terminal == null) return null;
        return new TerminalDTO(
                terminal.getPublicId(),
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

    public Terminal toEntityCreate (TerminalCreateDTO dto, UUID idPlace) {
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
        terminal.setOccupied(dto.getStatusTerminal() == TerminalStatus.LIBRE ? false: true);
/*        terminal.setDateCreation(dto.getDateCreation());
        terminal.setLastMaintenance(dto.getLastMaintenance());*/
/*
        terminal.setPlace(idPlace != null ? addressRepository.getReferenceById(idPlace) : null);
*/
        terminal.setPlace(idPlace != null ? placeRepository.findByPublicId(idPlace)
                .orElseThrow(() -> new RuntimeException("Adresse introuvable : " + idPlace)) : null);

        return terminal;
    }


    // === SERVICE SUPPLEMENTAIRE ===

    public OptionDTO toDTO(Option option) {
        if (option == null) return null;
        String addressName = option.getPlace() != null ? option.getPlace().getAddress().getNameAddress() : null;
        return new OptionDTO(
                option.getPublicId(),
                option.getNameOption(),
                option.getPriceOption(),
                option.getDescriptionOption(),
                addressName
        );
    }

    public Option toEntity(OptionDTO dto) {
        if (dto == null) return null;
        Option option = new Option();
        option.setNameOption(dto.getNameOption());
        option.setPriceOption(dto.getPriceOption());
        option.setDescriptionOption(dto.getDescriptionOption());
        return option;
    }

    public Option toEntityCreate (OptionCreateDTO dto, UUID idPlace) {
        if (dto == null) return null;
        Option option = new Option();
        option.setNameOption(dto.getNameOption());
        option.setPriceOption(dto.getPriceOption());
        option.setDescriptionOption(dto.getDescriptionOption());
        option.setPlace(idPlace != null ? placeRepository.findByPublicId(idPlace)
                .orElseThrow(() -> new RuntimeException("Adresse introuvable : " + idPlace)) : null);
        return option;
    }

    // === booking ===
    public BookingDTO toDTO(Booking booking) {
        if (booking == null) return null;
        UserDTO userClientDTO = booking.getUser() != null ? toDTO(booking.getUser()) : null;
        UserDTO userOwnerDTO = booking.getTerminal() != null ? toDTO(booking.getTerminal().getPlace().getUser()) : null;
        AddressDTO addressDTO = booking.getTerminal() != null ?
                toDTO(booking.getTerminal().getPlace().getAddress()) : null;
        TerminalDTO terminalDTO = booking.getTerminal() != null ? toDTO(booking.getTerminal()) : null;
        return new BookingDTO(
                booking.getPublicId(),
                booking.getNumberBooking(),
                booking.getStartingDate(),
                booking.getEndingDate(),
                booking.getStatusBooking(),
                booking.getTotalAmount(),
                booking.getPaymentDate(),
                userClientDTO,
                userOwnerDTO,
                addressDTO,
                terminalDTO,
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

    public Booking toEntityCreate(BookingCreateDTO dto, Long idUser, UUID idTerminal, UUID idCar, UUID idOption) {
        if (dto == null) return null;
        BigDecimal terminalPrice = terminalRepository.findByPublicId(idTerminal)
                .orElseThrow(() -> new RuntimeException("Terminal introuvable : " + idTerminal))
                .getPrice();
        terminalPrice = terminalPrice != null ? terminalPrice : BigDecimal.ZERO;

        BigDecimal optionPrice = BigDecimal.ZERO;
        if (idOption != null) {
            optionPrice = optionRepository.findByPublicId(idOption)
                .orElseThrow(() -> new RuntimeException("Option introuvable : " + idOption))
                .getPriceOption();
            optionPrice = optionPrice != null ? optionPrice : BigDecimal.ZERO;
        }

        Booking booking = new Booking();
        booking.setStartingDate(dto.getStartingDate());
        booking.setEndingDate(dto.getEndingDate());
        booking.setStatusBooking(dto.getStatusBooking());
        booking.setTotalAmount(terminalPrice.add(optionPrice));
        booking.setUser(idUser != null ? userRepository.findById(idUser)
                .orElseThrow(() -> new RuntimeException("User introuvable : " + idUser)): null);
        booking.setTerminal(idTerminal != null ? terminalRepository.findByPublicId(idTerminal)
                .orElseThrow(() -> new RuntimeException("Terminal introuvable : " + idTerminal)) : null);
        booking.setCar(idCar != null ? carRepository.findByPublicId(idCar)
                .orElseThrow(() -> new RuntimeException("Car introuvable : " + idCar)) : null);
        booking.setOption(idOption != null ? optionRepository.findByPublicId(idOption)
                .orElseThrow(() -> new RuntimeException("Option introuvable : " + idOption)) : null);
        return booking;
    }

    public Booking toEntityUpdateStatus(BookingStatusDTO dto) {
        if (dto == null) return null;
        Booking booking = new Booking();
        booking.setStatusBooking(dto.getStatusBooking());
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
