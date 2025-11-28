package com.electricitybusiness.api.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

/**
 * Entité représentant une adresse dans le système.
 * Une adresse peut être associée à un utilisateur et à un lieu.
 */
@Data
@Entity
@Table(name = "addresses")
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_address")
    private Long idAddress;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @Column(name = "name_adress")
    @NotBlank(message = "Le nom est obligatoire")
    private String nameAddress;

    @Column(name = "address", length = 200, nullable = false)
    @NotBlank(message = "L'adresse est obligatoire")
    private String address;

    @Column(name = "post_code", length = 10, nullable = false)
    @NotBlank(message = "Le code postal est obligatoire")
    private String postCode;

    @Column(name = "city", length = 100, nullable = false)
    @NotBlank(message = "La ville est obligatoire")
    private String city;

    @Column(name = "country", length = 100, nullable = false)
    @NotBlank(message = "Le pays est obligatoire")
    private String country;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "complement", length = 200)
    private String complement;

    @Column(name = "floor", length = 100)
    private String floor;

    @Column(name = "main_address")
    private Boolean mainAddress;

    @OneToMany(mappedBy = "address", fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<Place> places;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;
}
