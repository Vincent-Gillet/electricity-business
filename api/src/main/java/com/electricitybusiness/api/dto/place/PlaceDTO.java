package com.electricitybusiness.api.dto.place;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO pour l'entité Lieu
 * Représentation simplifiée sans relations circulaires
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDTO {
    private UUID publicId;

    @NotBlank(message = "Les instructions sont obligatoires")
    private String instructionPlace;

    private String nameAddress;

    private UUID publicIdAddress;
}
