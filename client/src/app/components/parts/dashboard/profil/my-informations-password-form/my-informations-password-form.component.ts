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
      passwordUser: ['', [Validators.required]]
    });
  }

  onSubmit():void {
    // TODO: Use EventEmitter with form value
    console.warn(this.updateUserForm.value);

    this.isSubmitted = true;

    console.log("MON FORM EST SOUMIS");
    console.log("updateUserForm.valid ",this.updateUserForm.valid);
    console.log("Toutes les valeurs des control du groupe -> updateUserForm.value ",this.updateUserForm.value);

    if (this.updateUserForm.valid) {
      // 3. Activer le state de chargement
      this.isLoading = true;

      // 4. Récupérer les données du formulaire
      const userData = this.updateUserForm.value;

      console.log('Données de connexion:', userData);

      // 5. Simuler un appel API avec setTimeout
      // (Dans un vrai projet, ça serait un appel HTTP)
      setTimeout(() => {
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
      }, 1000);
    }
  }
}
