import {Component, Inject, inject, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {ErrorFromComponent} from '../../error-from/error-from.component';
import {CarService} from '../../../../services/car/car.service';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {AuthService} from '../../../../services/auth/auth.service';
import {Car} from '../../../../models/car';

@Component({
  selector: 'app-car-form',
  standalone: true,
  imports: [
    ErrorFromComponent,
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './car-form.component.html',
  styleUrl: './car-form.component.scss'
})
export class CarFormComponent {

  private dialogRef: MatDialogRef<CarFormComponent> = inject(MatDialogRef<CarFormComponent>);

  onNoClick(): void {
    this.dialogRef.close();
  }

  carService: CarService = inject(CarService);
/*
  authService: AuthService = inject(AuthService);
*/

  // Propriété représentant le formulaire
  postCarForm: FormGroup;
  // Booléens d'état
  isSubmitted = false;
  isLoading = false;
  private updateCar = false;

  constructor(private fb: FormBuilder, private router: Router, @Inject(MAT_DIALOG_DATA) public car: Car) {
    this.postCarForm = this.fb.group({
      publicId: [this.car?.publicId || ''],
      licensePlate: [this.car?.licensePlate || '', [Validators.required]],
      brand: [this.car?.brand || '', [Validators.required]],
      model: [this.car?.model || '', [Validators.required]],
      year: [this.car?.year || '', [Validators.required]],
      batteryCapacity: [this.car?.batteryCapacity || '', [Validators.required]]
    });
    if (this.car) {
      this.updateCar = true;
    }
  }

  onSubmit():void {
    // TODO: Use EventEmitter with form value
    console.warn(this.postCarForm.value);

    this.isSubmitted = true;

    console.log("MON FORM EST SOUMIS");
    console.log("postCarForm.valid ",this.postCarForm.valid);
    console.log("Toutes les valeurs des control du groupe -> postCarForm.value ",this.postCarForm.value);
    console.log("Recuperer un seul control avec postCarForm.get('email')",this.postCarForm.get("licensePlate"));
    console.log("Recuperer la validité d'un control avec postCarForm.get('email').valid",this.postCarForm.get("brand")?.valid);
    console.log("Recuperer les erreurs d'un control avec postCarForm.get('motDePasse').errors",this.postCarForm.get("licensePlate")?.errors);
    console.log("Recuperer un seul control avec postCarForm.get('motDePasse')",this.postCarForm.get("licensePlate"));


    if (this.postCarForm.valid) {
      // 3. Activer le state de chargement
      this.isLoading = true;

      // 4. Récupérer les données du formulaire
      const carData = this.postCarForm.value;

      console.log('Données de connexion:', carData);

      // 5. Simuler un appel API avec setTimeout
      // (Dans un vrai projet, ça serait un appel HTTP)
      setTimeout(() => {
        if (this.updateCar) {
          this.carService.updateCar(this.car.publicId, carData).subscribe(
            {
              next: (response) => {
                console.log('Voiture mise à jour:', response);
                this.dialogRef.close();
                this.router.navigateByUrl('cars', { skipLocationChange: true }).then(() => {
                  this.router.navigate(['./tableau-de-bord/mes-voitures']);
                });
              },
              error: (error) => {
                console.error('Erreur lors de la mise à jour de la voiture:', error);
              }
            }
          );
          return;
        } else {
          this.carService.createCar(carData).subscribe(
            {
              next: (response) => {
                console.log('Voiture créée:', response);
                this.dialogRef.close();
                this.router.navigateByUrl('cars', { skipLocationChange: true }).then(() => {
                  this.router.navigate(['./tableau-de-bord/mes-voitures']);
                });
              },
              error: (error) => {
                console.error('Erreur lors de la création d\'une voiture:', error);
              }
            }
          );
        }
      }, 1000);

    }

  }
}
