package com.electricitybusiness.api.model;

import com.electricitybusiness.api.annotation.MinAge;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Entité représentant un utilisateur du système.
 * Un utilisateur peut effectuer des réservations et appartient à un lieu.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
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
    private String pseudo;

    @Column(unique = true)
    @Email(message = "L'adresse email doit être valide")
    @NotBlank(message = "L'adresse email est obligatoire")
    private String emailUser;

    @Column(name = "password_user", length = 60)
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String passwordUser;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le rôle est obligatoire")
    private UserRole role = UserRole.USER;

    @Column(name = "date_of_birth")
    @NotNull(message = "La date de naissance est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Past(message = "La date de naissance doit être dans le passé")
    @MinAge(minAge = 18, message = "L'utilisateur doit avoir au moins 18 ans")
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

/*    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Media media;*/

/*    @OneToOne
    @JoinColumn(name = "id_media")
    @ToString.Exclude
    private Media media;*/

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_media", referencedColumnName = "id_media")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Media media;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Terminal> terminals = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Car> car = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Address> addresses = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return passwordUser;
    }

    @Override
    public String getUsername() {
        return emailUser;
    }
}
