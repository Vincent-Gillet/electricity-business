package com.electricitybusiness.api.dto.option;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO pour l'entité Option
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionDTO {
    private UUID publicId;

    @NotNull(message = "Le nom de l'option est obligatoire")
    private String nameOption;

    @NotNull(message = "Le tarif de l'option est obligatoire")
    private BigDecimal priceOption;

    @NotNull(message = "La description de l'option est obligatoire")
    private String descriptionOption;

    private String nameAddress;
}
