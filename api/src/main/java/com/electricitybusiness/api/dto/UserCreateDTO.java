package com.electricitybusiness.api.dto;

import com.electricitybusiness.api.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {
    @NotBlank(message = "Le nom de l'utilisateur est obligatoire")
    private String surnameUser;

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le pseudo est obligatoire")
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String passwordUser;

    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "L'adresse email doit être valide")
    private String emailUser;

    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Length(min = 10, max = 15)
    private String phone;

}
