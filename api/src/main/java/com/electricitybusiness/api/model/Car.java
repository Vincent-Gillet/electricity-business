package com.electricitybusiness.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.Year;
import java.util.UUID;

/**
 * Entité représentant une voiture dans le système.
 * Une voiture est associé à un utilisateur.
 */
@Data
@Entity
@Table(name = "cars")
@NoArgsConstructor
@AllArgsConstructor
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_car")
    private Long idCar;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @NotBlank(message = "La plaque d'immatriculation est obligatoire")
    @Length(min = 7, max = 7)
    private String licensePlate;

    @NotBlank(message = "La marque est obligatoire")
    private String brand;

    @NotBlank(message = "Le modèle est obligatoire")
    private String model;

    @Past(message = "L'année de fabrication doit être dans le passé")
/*
    @Column(columnDefinition = "YEAR")
*/
    @Column(name = "\"year\"")
    private Year year;

    @Column(name = "battery_capacity")
    @Min(value = 15, message = "La capacité de la batterie doit être comprise entre 15 et 100")
    @Max(value = 100, message = "La capacité de la batterie doit être comprise entre 15 et 100")
    @NotNull(message = "La capacité de la batterie est obligatoire")
    private int batteryCapacity; // en kWh

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    @JsonBackReference
    private User user;

}
