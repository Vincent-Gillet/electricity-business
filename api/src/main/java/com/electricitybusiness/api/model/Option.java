package com.electricitybusiness.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant une option dans le système.
 * Une option peut être associée à des médias.
 */
@Data
@Entity
@Table(name = "options")
@NoArgsConstructor
@AllArgsConstructor
public class Option {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_option")
    private Long idOption;

    @Column(name = "name_option", length = 100, nullable = false)
    @NotBlank(message = "Le nom de l'option est obligatoire")
    private String nameOption;

    @Column(name = "price_option")
    @NotNull(message = "Le tarif de l'option est obligatoire")
    private BigDecimal priceOption;

    @Column(name = "description_option")
    @NotBlank(message = "La description de l'option est obligatoire")
    private String descriptionOption;

    @OneToMany(mappedBy = "options")
    private Set<Media> media = new HashSet<>();

}
