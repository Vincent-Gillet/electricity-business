package com.electricitybusiness.api.dto.user;

import com.electricitybusiness.api.model.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRoleDTO {
    @NotNull(message = "Le rôle est obligatoire")
    private UserRole role;
}
