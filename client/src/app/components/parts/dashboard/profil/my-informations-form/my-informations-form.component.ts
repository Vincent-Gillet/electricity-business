import {Component, Inject, inject} from '@angular/core';
import {ErrorFromComponent} from '../../../error-from/error-from.component';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {CarService} from '../../../../../services/car/car.service';
import {Router} from '@angular/router';
import {User} from '../../../../../models/user';
import {UserService} from '../../../../../services/user/user.service';

@Component({
  selector: 'app-my-informations-form',
  standalone: true,
  imports: [
    ErrorFromComponent,
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './my-informations-form.component.html',
  styleUrl: './my-informations-form.component.scss'
})
export class MyInformationsFormComponent {
  private dialogRef: MatDialogRef<MyInformationsFormComponent> = inject(MatDialogRef<MyInformationsFormComponent>);

  onNoClick(): void {
    this.dialogRef.close();
  }

  userService: UserService = inject(UserService);

  // Propriété représentant le formulaire
  updateUserForm: FormGroup;
  // Booléens d'état
  isSubmitted = false;
  isLoading = false;

  constructor(private fb: FormBuilder, private router: Router, @Inject(MAT_DIALOG_DATA) public user: User) {
    this.updateUserForm = this.fb.group({
      surnameUser: [this.user?.surnameUser || '', [Validators.required]],
      firstName: [this.user?.firstName || '', [Validators.required]],
      pseudo: [this.user?.pseudo || '', [Validators.required]],
      emailUser: [this.user?.emailUser || '', [Validators.required]],
      dateOfBirth: [this.user?.dateOfBirth || '', [Validators.required]],
      phone: [this.user?.phone || '', [Validators.required]],
      iban: [this.user?.iban || '', [Validators.required]]
    });
  }

  onSubmit():void {
    // TODO: Use EventEmitter with form value
    console.warn(this.updateUserForm.value);

    this.isSubmitted = true;

    console.log("MON FORM EST SOUMIS");
    console.log("updateUserForm.valid ",this.updateUserForm.valid);
    console.log("Toutes les valeurs des control du groupe -> updateUserForm.value ",this.updateUserForm.value);
    console.log("Recuperer un seul control avec updateUserForm.get('email')",this.updateUserForm.get("surnameUser"));
    console.log("Recuperer la validité d'un control avec updateUserForm.get('email').valid",this.updateUserForm.get("firstName")?.valid);
    console.log("Recuperer les erreurs d'un control avec updateUserForm.get('motDePasse').errors",this.updateUserForm.get("pseudo")?.errors);
    console.log("Recuperer un seul control avec updateUserForm.get('motDePasse')",this.updateUserForm.get("emailUser"));


    if (this.updateUserForm.valid) {
      // 3. Activer le state de chargement
      this.isLoading = true;

      // 4. Récupérer les données du formulaire
      const userData = this.updateUserForm.value;

      console.log('Données de connexion:', userData);

      // 5. Simuler un appel API avec setTimeout
      // (Dans un vrai projet, ça serait un appel HTTP)
      setTimeout(() => {
        this.userService.updateUserByToken(userData).subscribe(
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
