package com.electricitybusiness.api.dto;

import com.electricitybusiness.api.model.BookingStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour l'entité Reservation
 * Inclut des références simples aux entités liées sans relations circulaires
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    @NotBlank(message = "Le numéro de réservation est obligatoire")
    private String numberBooking;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDateTime startingDate;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endingDate;

    @NotNull(message = "L'état de la réservation est obligatoire")
    private BookingStatus statusBooking;

    private BigDecimal totalAmount;

    private LocalDateTime paymentDate;

    private Long idUser;

    private Long idTerminal;

    private Long idCar;

    private Long idOption;
}
