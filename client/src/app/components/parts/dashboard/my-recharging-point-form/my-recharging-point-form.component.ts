import {Component, Inject, inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {PlaceService} from '../../../../services/place/place.service';
import {Place} from '../../../../models/place';
import {ErrorFromComponent} from '../../error-from/error-from.component';
import {AddressService} from '../../../../services/address/address.service';
import {Address} from '../../../../models/address';

@Component({
  selector: 'app-my-recharging-point-form',
  standalone: true,
  imports: [
    ErrorFromComponent,
    ReactiveFormsModule
  ],
  templateUrl: './my-recharging-point-form.component.html',
  styleUrl: './my-recharging-point-form.component.scss'
})
export class MyRechargingPointFormComponent implements OnInit {
  private dialogRef: MatDialogRef<MyRechargingPointFormComponent> = inject(MatDialogRef<MyRechargingPointFormComponent>);

  onNoClick(): void {
    this.dialogRef.close();
  }

  private placeService: PlaceService = inject(PlaceService);
  private addressService: AddressService = inject(AddressService);
  public addresses: Address[] = [];

  // Propriété représentant le formulaire
  postPlaceForm: FormGroup;
  // Booléens d'état
  isSubmitted = false;
  isLoading = false;
  private updatePlace = false;

  constructor(private fb: FormBuilder, private router: Router, @Inject(MAT_DIALOG_DATA) public place: Place) {
    this.postPlaceForm = this.fb.group({
      publicId: [this.place?.publicId || ''],
      instructionPlace: [this.place?.instructionPlace || '', [Validators.required]],
      publicIdAddress: [this.place?.publicIdAddress || '', [Validators.required]],
    });
    if (this.place) {
      console.log("this.place 1 : ", this.place);
      this.updatePlace = true;
    }
  }

  onSubmit():void {
    // TODO: Use EventEmitter with form value
    console.warn(this.postPlaceForm.value);

    this.isSubmitted = true;

    console.log("MON FORM EST SOUMIS");
    console.log("postPlaceForm.valid ",this.postPlaceForm.valid);
    console.log("Toutes les valeurs des control du groupe -> postPlaceForm.value ",this.postPlaceForm.value);

    if (this.postPlaceForm.valid) {
      // 3. Activer le state de chargement
      this.isLoading = true;

      // 4. Récupérer les données du formulaire
      const placeData = this.postPlaceForm.value;

      console.log('Données de connexion:', placeData);

      // 5. Simuler un appel API avec setTimeout
      // (Dans un vrai projet, ça serait un appel HTTP)
      setTimeout(() => {
        if (this.updatePlace) {
          console.log("this.place 2 : ", this.place);

          this.placeService.updatePlaceByPublicId(this.place.publicId, placeData).subscribe(
            {
              next: (response) => {
                console.log('Voiture mise à jour:', response);
                this.dialogRef.close();
                this.router.navigateByUrl('places', { skipLocationChange: true }).then(() => {
                  this.router.navigate(['./tableau-de-bord/lieux-de-recharge']);
                });
              },
              error: (error) => {
                console.error('Erreur lors de la mise à jour du lieu:', error);
              }
            }
          );
          return;
        } else {
          this.placeService.createPlaceByPublicId(placeData).subscribe(
            {
              next: (response) => {
                console.log('Voiture créée:', response);
                this.dialogRef.close();
                this.router.navigateByUrl('places', { skipLocationChange: true }).then(() => {
                  this.router.navigate(['./tableau-de-bord/lieux-de-recharge']);
                });
              },
              error: (error) => {
                console.error('Erreur lors de la création d\'un lieu:', error);
              }
            }
          );
        }
      }, 1000);

    }

  }

  ngOnInit(): void {
    this.addressService.getAddressesByUser().subscribe(
      addresses => {
        this.addresses = addresses;
    })
  }
}
