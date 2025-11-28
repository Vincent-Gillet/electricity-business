package com.electricitybusiness.api.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateBanishedDTO {
    @NotNull(message = "Le statut de bannissement est obligatoire")
    private Boolean banished;
}
