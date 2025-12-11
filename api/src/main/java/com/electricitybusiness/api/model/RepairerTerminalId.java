package com.electricitybusiness.api.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe représentant l'identifiant composite pour la relation entre Borne et Reparateur.
 * Utilisée pour gérer les associations entre les bornes et les réparateurs.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class RepairerTerminalId {
    private Long idTerminal;
    private Long idRepairer;
}
