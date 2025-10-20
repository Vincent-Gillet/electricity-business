package com.electricitybusiness.api.dto;

import com.electricitybusiness.api.model.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.Year;
import java.util.UUID;

/**
 * DTO pour l'entité Vehicule
 * Actuellement vide, mais peut être étendu pour inclure des propriétés spécifiques
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarDTO {

    private UUID publicId;

    @JsonProperty("licensePlate")
    @NotBlank(message = "La plaque d'immatriculation est obligatoire")
    @Length(min = 7, max = 7)
    private String licensePlate;

    @NotBlank(message = "La marque est obligatoire")
    private String brand;

    @NotBlank(message = "Le modèle est obligatoire")
    private String model;

    private Integer year;

    @Min(value = 15, message = "La capacité de la batterie doit être comprise entre 15 et 100")
    @Max(value = 100, message = "La capacité de la batterie doit être comprise entre 15 et 100")
    @NotNull(message = "La capacité de la batterie est obligatoire")
    private Integer batteryCapacity; // en kWh

    private Long idUser;
}
