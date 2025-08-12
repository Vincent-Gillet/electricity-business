package com.electricitybusiness.api.model;

/**
 * Classe représentant l'identifiant d'une réservation.
 * Cette classe est utilisée pour créer une clé composite dans la base de données.
 */

public class BookingId {
    private Long idUser;
    private Long idTerminal;
    private Long idOption;
    private Long idCar;
}
