package com.electricitybusiness.api.dto.booking;

import com.electricitybusiness.api.model.BookingStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreateDTO {
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

    private Long idUser;

    private UUID publicIdTerminal;

    private UUID publicIdCar;

    private UUID publicIdOption;
}
