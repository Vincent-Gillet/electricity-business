package com.electricitybusiness.api.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressCreateDTO {
    @NotBlank(message = "Le nom de l'adresse est obligatoire")
    private String nameAddress;

    @NotBlank(message = "Le numéro et rue sont obligatoires")
    private String address;

    @NotBlank(message = "Le code postal est obligatoire")
    @Size(min = 5, max = 5, message = "Le code postal doit contenir 5 caractères")
    private String postCode;

    @NotBlank(message = "La ville est obligatoire")
    private String city;

    @NotBlank(message = "Le pays est obligatoire")
    private String country;

    private String region;
    private String complement;
    private String floor;
/*
    private PlaceDTO place;
*/
    private Long idUser;
}
