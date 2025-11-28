package com.electricitybusiness.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entité représentant un lieu dans le système.
 * Un lieu peut contenir des instructions, être associé à un utilisateur,
 * et avoir plusieurs médias et bornes associés.
 */
@Data
@Entity
@Table(name = "places")
@NoArgsConstructor
@AllArgsConstructor
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_place")
    private Long idPlace;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @Column(name = "instruction_place", columnDefinition = "TEXT")
    private String instructionPlace;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "places_medias",
            joinColumns = @JoinColumn(name = "id_place"),
            inverseJoinColumns = @JoinColumn(name = "id_media")
    )
    private Set<Media> medias = new HashSet<>();

    @ManyToOne
    @JoinColumn(name="id_address", nullable=false)
    private Address address;
}
