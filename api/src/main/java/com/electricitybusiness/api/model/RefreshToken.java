package com.electricitybusiness.api.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe représentant un refresh tokken pour l'authentification.
 * Un jeton de rafraîchissement est utilisé pour obtenir un nouveau jeton d'accès sans nécessiter une nouvelle authentification.
 */
@Data
@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    private String idRefreshToken;

    @ManyToOne
    @JoinColumn(name = "user")
    @JsonBackReference
    private User user;
}
