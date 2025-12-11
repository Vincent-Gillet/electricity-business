package com.electricitybusiness.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entité représentant une borne électrique dans le système.
 * Une borne appartient à un lieu et peut avoir des réservations et des tarifs.
 */
@Data
@Entity
@Table(name = "terminals")
@NoArgsConstructor
@AllArgsConstructor
public class Terminal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_terminal")
    @EqualsAndHashCode.Include
    private Long idTerminal;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @Column(name = "name_terminal", length = 100, nullable = false)
    @NotBlank(message = "Le nom de la borne est obligatoire")
    private String nameTerminal;

    @Column(name = "latitude", precision = 10, scale = 8, nullable = false)
    @DecimalMin(value = "-90.0", message = "La latitude doit être entre -90 et 90")
    @DecimalMax(value = "90.0", message = "La latitude doit être entre -90 et 90")
    @NotNull(message = "La latitude est obligatoire")
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8, nullable = false)
    @DecimalMin(value = "-180.0", message = "La longitude doit être entre -180 et 180")
    @DecimalMax(value = "180.0", message = "La longitude doit être entre -180 et 180")
    @NotNull(message = "La longitude est obligatoire")
    private BigDecimal longitude;

    @Column(name = "price", nullable = false, precision = 8, scale = 2)
    @DecimalMin(value = "0.0", message = "Le tarif doit être positive")
    @NotNull(message = "Le tarif est obligatoire")
    private BigDecimal price;

    @Column(name = "power", precision = 10, scale = 2, nullable = false)
    @DecimalMin(value = "0.0", message = "La puissance doit être positive")
    @NotNull(message = "La puissance est obligatoire")
    private BigDecimal power;

    @Column(name = "instruction_terminal", columnDefinition = "TEXT")
    private String instructionTerminal;

    @Column(name = "standing", nullable = false)
    private Boolean standing;

    @Column(name = "status_terminal", nullable = false)
    @Enumerated(EnumType.STRING)
    private TerminalStatus statusTerminal;

    @Column(name = "occupied", nullable = false)
/*
    private Boolean occupied = "LIBRE".equals(statusTerminal) ? false : true;
*/
    private Boolean occupied;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "last_maintenance")
    private LocalDateTime lastMaintenance;

    @ManyToOne
    @JoinColumn(name = "id_user")
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private User user;

/*    @OneToMany(mappedBy = "terminal")
    private Set<Media> medias = new HashSet<>();*/

    @ManyToMany
    @JoinTable(
            name = "terminals_medias",
            joinColumns = @JoinColumn(name = "id_terminal"),
            inverseJoinColumns = @JoinColumn(name = "id_media")
    )
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<Media> medias = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "id_place")
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Place place;

    @ManyToMany
    private Set<Repairer> repairers = new HashSet<>();

    @OneToMany(mappedBy = "terminal")
    private Set<Booking> bookings = new HashSet<>();
}
