package com.electricitybusiness.api.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Annotation de validation personnalisée pour vérifier qu'un utilisateur a un âge minimum spécifié.
 */
@Documented
@Constraint(validatedBy = MinAgeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface MinAge {
    String message() default "L'utilisateur doit avoir au moins {minAge} ans";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int minAge();
}
