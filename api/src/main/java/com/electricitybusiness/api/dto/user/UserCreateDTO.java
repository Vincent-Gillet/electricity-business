package com.electricitybusiness.api.dto.user;

import com.electricitybusiness.api.annotation.MinAge;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDTO {
    @NotBlank(message = "Le nom de l'utilisateur est obligatoire")
    private String surnameUser;

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le pseudo est obligatoire")
    private String pseudo;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String passwordUser;

    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "L'adresse email doit être valide")
    private String emailUser;

    @Past(message = "La date de naissance doit être dans le passé")
    @MinAge(minAge = 18, message = "L'utilisateur doit avoir au moins 18 ans")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Length(min = 10, max = 15)
    private String phone;
}
