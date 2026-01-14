import {Component, Input} from '@angular/core';
import { FormGroup } from '@angular/forms';

@Component({
  selector: 'app-error-from',
  standalone: true,
  imports: [],
  templateUrl: './error-from.component.html',
  styleUrl: './error-from.component.scss'
})
export class ErrorFromComponent {
  @Input() fieldName: string;
  @Input() nameFormGroup: FormGroup;
  @Input() isSubmitted: boolean;

  isFieldInvalid(fieldName: string): boolean {

    const field = this.nameFormGroup.get(fieldName);

    return Boolean(field && field.invalid && this.isSubmitted);
  }

  renameInput: Record<string, string> = {
    //User
    'emailUser': 'Email',
    'passwordUser': 'Mot de passe',
    'firstName': 'Prénom',
    'surnameUser': 'Nom',
    'username': 'Pseudo',
    'dateOfBirth': 'Date de naissance',
    'phone': 'Téléphone',
    'passwordUserValidation': 'Confirmation mot de passe',
    'termsOfUse': 'Conditions d\'utilisation',
    //Car
    'licensePlate' : 'Plaque d\'immatriculation',
    'brand' : 'Marque',
    'model' : 'Modèle',
    'year' : 'Année',
    'batteryCapacity' : 'Capacité de la batterie',
    //Booking
    'startingDate' : 'Date de début',
    'endingDate' : 'Date de fin',
    'publicIdCar' : 'Véhicule'
  }

  getFieldError(fieldName: string): string {
    const field = this.nameFormGroup.get(fieldName);

    // Vérifier si le champ existe et a des erreurs
    if (field && field.errors) {
      if (field.errors['required']){
        const displayName = this.renameInput[fieldName] || fieldName;
        return `${displayName} obligatoire`;
      }
      if (field.errors['email']){
        return 'Format email invalide';
      }
      if (field.errors['minlength']) {
        return `Minimum ${field.errors['minlength'].requiredLength} caractères`;
      }

      if (field.errors['pattern']) {
        return 'Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial';
      }

      if (field.errors['passwordMismatch']) {
        return 'Les mots de passe ne correspondent pas';
      }

      if (field.errors['majorityValidator']) {
        return `Vous devez avoir au moins ${field.errors['majorityValidator'].requiredAge} ans`;
      }

    }
    return '';
  }
}
