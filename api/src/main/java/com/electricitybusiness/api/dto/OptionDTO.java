package com.electricitybusiness.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO pour l'entité Option
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionDTO {
    @NotNull(message = "Le nom de l'option est obligatoire")
    private String nameOption;

    @NotNull(message = "Le tarif de l'option est obligatoire")
    private BigDecimal priceOption;

    @NotNull(message = "La description de l'option est obligatoire")
    private String descriptionOption;

}
