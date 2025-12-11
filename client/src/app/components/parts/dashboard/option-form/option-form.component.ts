import {Component, Inject, inject, OnInit} from '@angular/core';
import {ErrorFromComponent} from '../../error-from/error-from.component';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Router} from '@angular/router';
import {OptionService} from '../../../../services/option/option.service';
import {Option} from '../../../../models/option';
import {TERMINAL_STATUS_LABELS} from '../../../../constants/terminal-status-labels';
import {PlaceService} from '../../../../services/place/place.service';

@Component({
  selector: 'app-option-form',
  standalone: true,
  imports: [
    ErrorFromComponent,
    ReactiveFormsModule
  ],
  templateUrl: './option-form.component.html',
  styleUrl: './option-form.component.scss'
})
export class OptionFormComponent implements OnInit {
  private dialogRef: MatDialogRef<OptionFormComponent> = inject(MatDialogRef<OptionFormComponent>);

  onNoClick(): void {
    this.dialogRef.close();
  }

  optionService: OptionService = inject(OptionService);
  placeService: PlaceService = inject(PlaceService);

  // Propriété représentant le formulaire
  postOptionForm: FormGroup;
  // Booléens d'état
  isSubmitted = false;
  isLoading = false;
  private updateOption = false;
  public places: any[] = [];

  constructor(
    private fb: FormBuilder,
    private router: Router,
    @Inject(MAT_DIALOG_DATA) public option: Option
  ) {
    this.postOptionForm = this.fb.group({
      publicId: [this.option?.publicId || ''],
      nameOption: [this.option?.nameOption || '', [Validators.required]],
      priceOption: [this.option?.priceOption || '', [Validators.required]],
      descriptionOption: [this.option?.descriptionOption || '', [Validators.required]],
      publicIdPlace: [this.option?.publicIdPlace || '', [Validators.required]],
    });
    if (this.option) {
      this.updateOption = true;
    }
  }

  ngOnInit() {
    this.placeService.getPlacesByUser().subscribe(places => {
      this.places = places;
    })
  }

  onSubmit():void {
    // TODO: Use EventEmitter with form value
    console.warn(this.postOptionForm.value);

    this.isSubmitted = true;

    console.log("MON FORM EST SOUMIS");
    console.log("postOptionForm.valid ",this.postOptionForm.valid);
    console.log("Toutes les valeurs des control du groupe -> postOptionForm.value ",this.postOptionForm.value);
    console.log("Recuperer un seul control avec postOptionForm.get('email')",this.postOptionForm.get("licensePlate"));
    console.log("Recuperer la validité d'un control avec postOptionForm.get('email').valid",this.postOptionForm.get("brand")?.valid);
    console.log("Recuperer les erreurs d'un control avec postOptionForm.get('motDePasse').errors",this.postOptionForm.get("licensePlate")?.errors);
    console.log("Recuperer un seul control avec postOptionForm.get('motDePasse')",this.postOptionForm.get("licensePlate"));


    if (this.postOptionForm.valid) {
      // 3. Activer le state de chargement
      this.isLoading = true;

      // 4. Récupérer les données du formulaire
      const optionData = this.postOptionForm.value;

      console.log('Données de connexion:', optionData);

      // 5. Simuler un appel API avec setTimeout
      // (Dans un vrai projet, ça serait un appel HTTP)
      if (this.updateOption) {
        this.optionService.updateOptionByPublicId(this.option.publicId, optionData).subscribe(
          {
            next: (response) => {
              console.log('Voiture mise à jour:', response);
              this.dialogRef.close();
              const currentUrl = this.router.url;
              this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
                this.router.navigateByUrl(currentUrl);
              });
            },
            error: (error) => {
              console.error('Erreur lors de la mise à jour de la voiture:', error);
            }
          }
        );
        return;
      } else {
        this.optionService.createOptionByPublicId(optionData).subscribe(
          {
            next: (response) => {
              console.log('Option créée:', response);
              this.dialogRef.close();
              const currentUrl = this.router.url;
              this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
                this.router.navigateByUrl(currentUrl);
              });
            },
            error: (error) => {
              console.error('Erreur lors de la création d\'une voiture:', error);
            }
          }
        );
      }

    }

  }

  protected readonly terminalStatusLabels = TERMINAL_STATUS_LABELS;
}
