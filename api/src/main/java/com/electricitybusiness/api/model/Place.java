package com.electricitybusiness.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

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

    @Column(name = "instruction_place", columnDefinition = "TEXT")
    private String instructionPlace;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToMany
    @JoinTable(
            name = "place_media",
            joinColumns = @JoinColumn(name = "id_place"),
            inverseJoinColumns = @JoinColumn(name = "id_media")
    )
    private Set<Media> medias = new HashSet<>();


    @OneToMany(mappedBy = "place")
    private Set<Terminal> terminals = new HashSet<>();

}
