package com.electricitybusiness.api.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

/**
 * Validateur pour l'annotation @MinAge.
 * Vérifie qu'une date de naissance correspond à un âge minimum spécifié.
 */
public class MinAgeValidator implements ConstraintValidator<MinAge, LocalDate> {
    private int minAge;

    /**
     * Initialise le validateur avec l'âge minimum spécifié dans l'annotation.
     *
     * @param constraintAnnotation L'annotation @MinAge contenant l'âge minimum.
     */
    @Override
    public void initialize(MinAge constraintAnnotation) {
        this.minAge = constraintAnnotation.minAge();
    }

    /**
     * Valide que la date de naissance correspond à l'âge minimum requis.
     *
     * @param dateOfBirth La date de naissance à valider.
     * @param context Le contexte de validation.
     * @return true si l'âge est supérieur ou égal à l'âge minimum, false sinon.
     */
    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext context) {
        if (dateOfBirth == null) {
            return true;
        }
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        return age >= minAge;
    }
}
