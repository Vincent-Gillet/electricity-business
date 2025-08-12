package com.electricitybusiness.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "name_adress")
    @NotBlank(message = "Le nom est obligatoire")
    private String nameAdress;

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

    @Column(name = "floor", length = 10)
    private String floor;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_place")
    private Place place;

    @ManyToOne
    @JoinColumn(name = "id_user")
    private User user;
}
