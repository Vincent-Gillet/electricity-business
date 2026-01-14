import {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';

export function majorityValidator(minAge: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null; // Si le champ est vide, laissez les autres validateurs (comme `Validators.required`) gérer l'erreur.
    }

    const birthDate = new Date(control.value);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();

    // Ajustement si l'anniversaire n'a pas encore eu lieu cette année
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }

    return age <= minAge ? { majorityValidator: { requiredAge: minAge, actualAge: age }} : null;
  };
}
