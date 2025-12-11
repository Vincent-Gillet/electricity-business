package com.electricitybusiness.api.dto.terminal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TerminalSearchDTO {
    @NotNull(message = "La latitude est obligatoire")
    @DecimalMin(value = "-90.0", message = "La latitude doit être comprise entre -90 et 90")
    @DecimalMax(value = "90.0", message = "La latitude doit être comprise entre -90 et 90")
    private BigDecimal latitude;

    @NotNull(message = "La longitude est obligatoire")
    @DecimalMin(value = "-180.0", message = "La longitude doit être comprise entre -180 et 180")
    @DecimalMax(value = "180.0", message = "La longitude doit être comprise entre -180 et 180")
    private BigDecimal longitude;


    private Double radius;
    private Boolean occupied;
    private LocalDateTime startingDate;
    private LocalDateTime endingDate;


/*    // ═══════════════════════════════════════════════════════════════
    // MÉTHODES DE VALIDATION MÉTIER
    // ═══════════════════════════════════════════════════════════════

    *//**
     * Vérifie si une recherche géographique est demandée.
     *
     * @return true si latitude ET longitude sont fournies
     *//*
    public boolean hasGeographicCriteria() {
        return latitude != null && longitude != null;
    }

    *//**
     * Vérifie si une recherche par disponibilité est demandée.
     *
     * @return true si startingDate ET endingDate sont fournies
     *//*
    public boolean hasAvailabilityCriteria() {
        return startingDate != null && endingDate != null;
    }

    *//**
     * Vérifie la cohérence des dates.
     *
     * @return true si :
     *   - Aucune date fournie (OK)
     *   - Les deux dates fournies ET startingDate < endingDate
     *//*
    public boolean isValidDateRange() {
        if (startingDate == null && endingDate == null) {
            return true; // Pas de critère de date = OK
        }

        if (startingDate == null || endingDate == null) {
            return false; // Une seule date fournie = KO
        }

        return startingDate.isBefore(endingDate); // startingDate doit être avant endingDate
    }

    *//**
     * Retourne le rayon par défaut si non spécifié.
     *
     * @return radius ou 10.0 km par défaut
     *//*
    public Double getRadiusOrDefault() {
        return radius != null ? radius : 10.0;
    }

    *//**
     * Vérifie si au moins un critère de recherche est fourni.
     *
     * @return true si au moins un critère est présent
     *//*
    public boolean hasAnyCriteria() {
        return hasGeographicCriteria()
                || hasAvailabilityCriteria()
                || occupied != null;
    }*/
}
