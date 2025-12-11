package com.electricitybusiness.api.dto.booking;

import com.electricitybusiness.api.dto.address.AddressDTO;
import com.electricitybusiness.api.dto.terminal.TerminalDTO;
import com.electricitybusiness.api.dto.user.UserDTO;
import com.electricitybusiness.api.model.BookingStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO pour l'entité Reservation
 * Inclut des références simples aux entités liées sans relations circulaires
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private UUID publicId;

/*
    @NotBlank(message = "Le numéro de réservation est obligatoire")
*/
    private String numberBooking;

    @NotNull(message = "La date de début est obligatoire")
    @FutureOrPresent(message = "La date de début doit être actuelle ou dans le futur")
    private LocalDateTime startingDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endingDate;

    @NotNull(message = "L'état de la réservation est obligatoire")
    private BookingStatus statusBooking;

    private BigDecimal totalAmount;

    private LocalDateTime paymentDate;

/*    private String address;

    private String city;*/

    private UserDTO userClientDTO;

    private UserDTO userOwnerDTO;

    private AddressDTO addressDTO;

    private TerminalDTO terminalDTO;

    private Long idUser;

    private Long idTerminal;

    private Long idCar;

    private Long idOption;
}
