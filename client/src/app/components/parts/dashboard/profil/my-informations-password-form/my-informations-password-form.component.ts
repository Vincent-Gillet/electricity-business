import {Component, Inject, inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {UserService} from '../../../../../services/user/user.service';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {User} from '../../../../../models/user';
import {ErrorFromComponent} from '../../../error-from/error-from.component';

@Component({
  selector: 'app-my-informations-password-form',
  standalone: true,
  imports: [
    ErrorFromComponent,
    ReactiveFormsModule
  ],
  templateUrl: './my-informations-password-form.component.html',
  styleUrl: './my-informations-password-form.component.scss'
})
export class MyInformationsPasswordFormComponent {
  private dialogRef: MatDialogRef<MyInformationsPasswordFormComponent> = inject(MatDialogRef<MyInformationsPasswordFormComponent>);

  onNoClick(): void {
    this.dialogRef.close();
  }

  userService: UserService = inject(UserService);

  // Propriété représentant le formulaire
  updateUserForm: FormGroup;
  // Booléens d'état
  isSubmitted = false;
  isLoading = false;

  constructor(private fb: FormBuilder, private router: Router) {
    this.updateUserForm = this.fb.group({
      passwordUser: ['', [Validators.required, Validators.minLength(8), Validators.pattern('^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$')]], // contrainte MINLENGTH 8 et pattern (au moins une majuscule, une minuscule, un chiffre et un caractère spécial)
      passwordUserValidation: ['', [Validators.required, Validators.minLength(8), Validators.pattern('^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$')]]
    }, {
      validators: this.passwordMatchValidator
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('passwordUser')?.value;
    const confirmPassword = form.get('passwordUserValidation')?.value;
    if (password !== confirmPassword) {
      // Ajouter l'erreur au champ "passwordUserValidation"
      form.get('passwordUserValidation')?.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true }; // Erreur au niveau du groupe (optionnel)
    } else {
      // Supprimer l'erreur si les mots de passe correspondent
      form.get('passwordUserValidation')?.setErrors(null);
      return null;
    }
    return null;
  }

  onSubmit():void {
    // TODO: Use EventEmitter with form value
    console.warn(this.updateUserForm.value);

    this.isSubmitted = true;

    console.log("MON FORM EST SOUMIS");
    console.log("updateUserForm.valid ",this.updateUserForm.valid);
    console.log("Toutes les valeurs des control du groupe -> updateUserForm.value ",this.updateUserForm.value);

    if (this.updateUserForm.valid) {
      // Activer le state de chargement
      this.isLoading = true;

      // Récupérer les données du formulaire
      const userData = this.updateUserForm.value;
      delete userData.passwordUserValidation;

      console.log('Données de connexion:', userData);

      this.userService.updatePasswordByToken(userData).subscribe(
        {
          next: (response) => {
            console.log('Informations modifiées :', response);
            this.dialogRef.close();
            this.router.navigateByUrl('my-informations', { skipLocationChange: true }).then(() => {
              this.router.navigate(['./tableau-de-bord/mes-informations']);
            });
          },
          error: (error) => {
            console.error('Erreur lors de la modification de vos données:', error);
          }
        }
      );
    }
  }
}
