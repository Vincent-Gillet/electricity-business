package com.electricitybusiness.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité représentant une réservation dans le système.
 * Une réservation est associée à un utilisateur, un véhicule, une borne et éventuellement une option.
 */

@Data
@Entity
@Table(name = "bookings")
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_booking")
    @EqualsAndHashCode.Include
    private Long idBooking;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "car")
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Car car;

    @ManyToOne
    @JoinColumn(name = "terminal")
    @JsonBackReference
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Terminal terminal;

    @ManyToOne
    @JoinColumn(name = "option_id")
    @JsonBackReference
    @Nullable
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Option option;

    @Column(name = "number_booking", length = 20, unique = true)
    private String numberBooking;

    @Column(name = "statut_booking")
    @NotNull(message = "Le statut de la réservation est obligatoire")
    @Enumerated(EnumType.STRING)
    private BookingStatus statusBooking;

    @Column(name = "total_amount", precision = 8, scale = 2)
    @NotNull(message = "Le montant payé est obligatoire")
    private BigDecimal totalAmount;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "starting_date")
    @NotNull(message = "La date de début est obligatoire")
    @FutureOrPresent(message = "La date de début doit être actuelle ou dans le futur")
    private LocalDateTime startingDate;

    @Column(name = "ending_date")
    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endingDate;


}
