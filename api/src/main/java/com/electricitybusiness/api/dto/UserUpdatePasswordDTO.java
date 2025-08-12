package com.electricitybusiness.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatePasswordDTO {
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String passwordUser;
}
