package com.electricitybusiness.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour l'entité Media
 * Représentation simplifiée sans relations circulaires
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaDTO {
    @NotBlank(message = "Le nom du média est obligatoire")
    private String nameMedia;

    @NotBlank(message = "Le type de média est obligatoire")
    private String type;

    @NotBlank(message = "L'URL du média est obligatoire")
    private String url;

    private String descriptionMedia;
    private String size;

    @NotBlank(message = "La date de création est obligatoire")
    private LocalDateTime dateCreation;
}
