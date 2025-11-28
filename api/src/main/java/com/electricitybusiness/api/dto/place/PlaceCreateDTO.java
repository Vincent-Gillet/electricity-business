package com.electricitybusiness.api.dto.place;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceCreateDTO {
    @NotBlank(message = "Les instructions sont obligatoires")
    private String instructionPlace;

    private Long idUser;

    private UUID publicIdAddress;
}
