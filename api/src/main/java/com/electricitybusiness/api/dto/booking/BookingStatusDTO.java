package com.electricitybusiness.api.dto.booking;

import com.electricitybusiness.api.model.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingStatusDTO {
    @NotNull(message = "L'état de la réservation est obligatoire")
    private BookingStatus statusBooking;
}