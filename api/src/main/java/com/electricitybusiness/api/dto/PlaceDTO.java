package com.electricitybusiness.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour l'entité Lieu
 * Représentation simplifiée sans relations circulaires
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDTO {
    @NotBlank(message = "Les instructions sont obligatoires")
    private String instructionPlace;
}
