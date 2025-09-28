package com.electricitybusiness.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private Long idBooking;

    @ManyToOne
    @JoinColumn(name = "user")
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "car")
    @JsonBackReference
    private Car car;

    @ManyToOne
    @JoinColumn(name = "terminal")
    @JsonBackReference
    private Terminal terminal;

    @ManyToOne
    @JoinColumn(name = "option_id")
    @JsonBackReference
    private Option option;

    @Column(name = "number_booking", length = 20, nullable = false, unique = true)
    @NotBlank(message = "Le numéro de réservation est obligatoire")
    private String numberBooking;

    @Column(name = "statut_booking")
    @NotNull(message = "Le statut de la réservation est obligatoire")
    @Enumerated(EnumType.STRING)
    private BookingStatus statusBooking;

    @Column(name = "total_amount")
    @NotNull(message = "Le montant payé est obligatoire")
    private BigDecimal totalAmount;

    @Column(name = "payment_date")
    @NotNull(message = "La date de paiement est obligatoire")
    private LocalDateTime paymentDate;

    @Column(name = "starting_date")
    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDateTime startingDate;

    @Column(name = "ending_date")
    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endingDate;


}
