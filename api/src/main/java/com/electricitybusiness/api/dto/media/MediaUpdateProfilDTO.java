package com.electricitybusiness.api.dto.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class MediaUpdateProfilDTO {
    @NotBlank(message = "Le nom du média est obligatoire")
    private String nameMedia;

    @NotBlank(message = "Le type de média est obligatoire")
    private String type;

    @NotBlank(message = "L'URL du média est obligatoire")
    private String url;

    private String descriptionMedia;
    private String size;

    @NotNull(message = "La date de création est obligatoire")
    private LocalDateTime dateCreation;
}
