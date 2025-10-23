import {Component, Inject, inject} from '@angular/core';
import {ErrorFromComponent} from '../../../error-from/error-from.component';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Router} from '@angular/router';
import {AddressService} from '../../../../../services/address/address.service';
import {Address} from '../../../../../models/address';

@Component({
  selector: 'app-address-form',
  standalone: true,
  imports: [
    ErrorFromComponent,
    ReactiveFormsModule
  ],
  templateUrl: './address-form.component.html',
  styleUrl: './address-form.component.scss'
})
export class AddressFormComponent {
  private dialogRef: MatDialogRef<AddressFormComponent> = inject(MatDialogRef<AddressFormComponent>);

  onNoClick(): void {
    this.dialogRef.close();
  }

  addressService: AddressService = inject(AddressService);

  // Propriété représentant le formulaire
  postAddressForm: FormGroup;
  // Booléens d'état
  isSubmitted = false;
  isLoading = false;
  private updateAddress = false;

  constructor(private fb: FormBuilder, private router: Router, @Inject(MAT_DIALOG_DATA) public address: Address) {
    this.postAddressForm = this.fb.group({
      nameAddress: [this.address?.nameAddress || '', [Validators.required]],
      address: [this.address?.address || '', [Validators.required]],
      postCode: [this.address?.postCode || '', [Validators.required]],
      city: [this.address?.city || '', [Validators.required]],
      region: [this.address?.region || '', [Validators.required]],
      country: [this.address?.country || '', [Validators.required]],
      complement: [this.address?.complement || ''],
      floor: [this.address?.floor || '']
    });
    if (this.address) {
      this.updateAddress = true;
    }
  }

  onSubmit():void {
    // TODO: Use EventEmitter with form value
    console.warn(this.postAddressForm.value);

    this.isSubmitted = true;

    console.log("MON FORM EST SOUMIS");
    console.log("postAddressForm.valid ",this.postAddressForm.valid);
    console.log("Toutes les valeurs des control du groupe -> postAddressForm.value ",this.postAddressForm.value);
    console.log("Recuperer un seul control avec postAddressForm.get('email')",this.postAddressForm.get("licensePlate"));
    console.log("Recuperer la validité d'un control avec postAddressForm.get('email').valid",this.postAddressForm.get("brand")?.valid);
    console.log("Recuperer les erreurs d'un control avec postAddressForm.get('motDePasse').errors",this.postAddressForm.get("licensePlate")?.errors);
    console.log("Recuperer un seul control avec postAddressForm.get('motDePasse')",this.postAddressForm.get("licensePlate"));


    if (this.postAddressForm.valid) {
      // 3. Activer le state de chargement
      this.isLoading = true;

      // 4. Récupérer les données du formulaire
      const addressData = this.postAddressForm.value;

      console.log('Données de connexion:', addressData);

      // 5. Simuler un appel API avec setTimeout
      // (Dans un vrai projet, ça serait un appel HTTP)
      setTimeout(() => {
        if (this.updateAddress) {
          this.addressService.updateAddressPublicId(this.address.publicId, addressData).subscribe(
            {
              next: (response) => {
                console.log('Adresse mise à jour:', response);
                this.dialogRef.close();
                this.router.navigateByUrl('addresses', { skipLocationChange: true }).then(() => {
                  this.router.navigate(['./tableau-de-bord/mes-adresses']);
                });
              },
              error: (error) => {
                console.error('Erreur lors de la mise à jour de l\'adresse:', error);
              }
            }
          );
          return;
        } else {
          this.addressService.createAddress(addressData).subscribe(
            {
              next: (response) => {
                console.log('Voiture créée:', response);
                this.dialogRef.close();
                this.router.navigateByUrl('addresses', { skipLocationChange: true }).then(() => {
                  this.router.navigate(['./tableau-de-bord/mes-adresses']);
                });
              },
              error: (error) => {
                console.error('Erreur lors de la création d\'une adresse:', error);
              }
            }
          );
        }
      }, 1000);

    }

  }
}
