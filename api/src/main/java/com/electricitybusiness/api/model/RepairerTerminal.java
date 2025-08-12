package com.electricitybusiness.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entité représentant une borne de réparation.
 * Une borne de réparation est associée à un réparateur et à une borne.
 */

@Data
@Entity
@Table(name = "bornes_reparateurs")
@NoArgsConstructor
@AllArgsConstructor
public class RepairerTerminal {
    @EmbeddedId
    private RepairerTerminalId id = new RepairerTerminalId();

    @ManyToOne
    @JoinColumn(name = "idRepairer", referencedColumnName = "id_reparateur", insertable= false, updatable = false)
    private Repairer repairer;

    @ManyToOne
    @JoinColumn(name = "idTerminal", referencedColumnName = "id_borne", insertable= false, updatable = false)
    private Terminal terminal;

    @Column(name = "reference", length = 100, nullable = false)
    @NotBlank(message = "La référence est obligatoire")
    private String reference;

    @Column(name = "date_reparation")
    @NotBlank(message = "La date de réparation est obligatoire")
    private LocalDate date_reparation;

    @Column(name = "description")
    @NotBlank(message = "La description est obligatoire")
    private String description;

}
