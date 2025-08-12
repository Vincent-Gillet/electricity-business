package com.electricitybusiness.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un utilisateur du système.
 * Un utilisateur peut effectuer des réservations et appartient à un lieu.
 */
@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_user")
    private Long idUser;

    @JsonProperty("surname_user")
    @NotBlank(message = "Le nom de l'utilisateur est obligatoire")
    @Length(min = 2, max = 50)
    private String surnameUser;

    @NotNull(message = "Le prénom est obligatoire")
    @Length(min = 2, max = 50)
    private String firstName;

    @Column(unique = true)
    @NotNull(message = "Le pseudo est obligatoire")
    private String username;

    @Column(unique = true)
    @Email(message = "L'adresse email doit être valide")
    @NotBlank(message = "L'adresse email est obligatoire")
    private String emailUser;

    @Column(name = "password_user")
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String passwordUser;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le rôle est obligatoire")
    private UserRole role = UserRole.USER;

    @Column(name = "date_of_birth")
    @NotNull(message = "La date de naissance est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Column(name = "phone")
    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Length(min = 10, max = 15)
    private String phone;

    @Column(name = "iban", unique = true)
    @Length(min = 27, max = 27)
    private String iban;

    @Column(name = "banished")
    @NotNull(message = "Le statut de bannissement est obligatoire")
    private Boolean banished = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Media media;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    private List<Terminal> terminals = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    @ToString.Exclude
    private List<Car> car = new ArrayList<>();


}
