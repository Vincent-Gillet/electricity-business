package com.electricitybusiness.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @Column(name = "name_option", length = 100, nullable = false)
    @NotBlank(message = "Le nom de l'option est obligatoire")
    private String nameOption;

    @Column(name = "price_option")
    @NotNull(message = "Le tarif de l'option est obligatoire")
    private BigDecimal priceOption;

    @Column(name = "description_option")
    @NotBlank(message = "La description de l'option est obligatoire")
    private String descriptionOption;

/*    @OneToMany(mappedBy = "options")
    private Set<Media> media = new HashSet<>();*/

    @ManyToMany
    @JoinTable(
            name = "options_medias",
            joinColumns = @JoinColumn(name = "id_option"),
            inverseJoinColumns = @JoinColumn(name = "id_media")
    )
    private Set<Media> medias = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "id_place")
    @JsonBackReference
    private Place place;
}
