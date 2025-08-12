package com.electricitybusiness.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.Year;

/**
 * DTO pour l'entité Vehicule
 * Actuellement vide, mais peut être étendu pour inclure des propriétés spécifiques
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarDTO {

    @JsonProperty("plaqueImmatriculation")
    @NotBlank(message = "La plaque d'immatriculation est obligatoire")
    @Length(min = 7, max = 7)
    private String licensePlate;

    @NotBlank(message = "La marque est obligatoire")
    private String brand;

    @NotBlank(message = "Le modèle est obligatoire")
    private String model;

    @Past(message = "L'année de fabrication doit être dans le passé")
    private Year year;

    @DecimalMin(value = "15", message = "La capacité de la batterie doit être comprise entre 15 et 100")
    @DecimalMax(value = "100", message = "La capacité de la batterie doit être comprise entre 15 et 100")
    @NotNull(message = "La capacité de la batterie est obligatoire")
    private int batteryCapacity; // en kWh

}
