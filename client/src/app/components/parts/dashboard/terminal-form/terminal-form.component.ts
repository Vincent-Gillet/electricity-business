import {Component, Inject, inject, OnInit} from '@angular/core';
import {ErrorFromComponent} from "../../error-from/error-from.component";
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Router} from '@angular/router';
import {Terminal} from '../../../../models/terminal';
import {TerminalService} from '../../../../services/terminal/terminal.service';
import { TERMINAL_STATUS_LABELS } from '../../../../constants/terminal-status-labels';
import {Place} from '../../../../models/place';

@Component({
  selector: 'app-terminal-form',
  standalone: true,
    imports: [
        ErrorFromComponent,
        ReactiveFormsModule
    ],
  templateUrl: './terminal-form.component.html',
  styleUrl: './terminal-form.component.scss'
})
export class TerminalFormComponent implements OnInit {
  private dialogRef: MatDialogRef<TerminalFormComponent> = inject(MatDialogRef<TerminalFormComponent>);

  onNoClick(): void {
    this.dialogRef.close();
  }

  private terminalService: TerminalService = inject(TerminalService);
  public terminalStatuses: any = [];

  // Propriété représentant le formulaire
  postTerminalForm: FormGroup;
  // Booléens d'état
  isSubmitted = false;
  isLoading = false;
  private updateTerminal = false;
  public terminalStatusLabels = TERMINAL_STATUS_LABELS;

  public terminal: Terminal | null;
  public place: Place;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    @Inject(MAT_DIALOG_DATA) public data: {
      terminal: Terminal | null,
      place: Place
    }
  ) {
    this.terminal = data.terminal;
    this.place = data.place;
    this.postTerminalForm = this.fb.group({
      publicId: [this.terminal?.publicId || ''],
      nameTerminal: [this.terminal?.nameTerminal || '', [Validators.required]],
      latitude: [this.terminal?.latitude || '', [Validators.required]],
      longitude: [this.terminal?.longitude || '', [Validators.required]],
      price: [this.terminal?.price || '', [Validators.required]],
      power: [this.terminal?.power || '', [Validators.required]],
      instructionTerminal: [this.terminal?.instructionTerminal || '', [Validators.required]],
      standing: [this.terminal?.standing || '', [Validators.required]],
      statusTerminal: [this.terminal?.statusTerminal || '', [Validators.required]],
      publicIdPlace: [this.place.publicId || '', [Validators.required]],
    });
    if (this.terminal) {
      console.log("this.place 1 : ", this.terminal);
      this.updateTerminal = true;
    }
  }

  onSubmit():void {
    // TODO: Use EventEmitter with form value
    console.warn(this.postTerminalForm.value);

    this.isSubmitted = true;

    console.log("MON FORM EST SOUMIS");
    console.log("postTerminalForm.valid ",this.postTerminalForm.valid);
    console.log("Toutes les valeurs des control du groupe -> postTerminalForm.value ",this.postTerminalForm.value);

    if (this.postTerminalForm.valid) {
      // 3. Activer le state de chargement
      this.isLoading = true;

      // 4. Récupérer les données du formulaire
      const terminalData = this.postTerminalForm.value;

      console.log('Données de connexion:', terminalData);

      if (this.updateTerminal) {
        console.log("this.place 2 : ", this.terminal);

        this.terminalService.updateTerminalByPublicId(this.terminal.publicId, terminalData).subscribe(
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
              console.error('Erreur lors de la mise à jour du lieu:', error);
            }
          }
        );
        return;
      } else {
        this.terminalService.createTerminalByPublicId(terminalData).subscribe(
          {
            next: (response) => {
              console.log('Borne créée:', response);
              this.dialogRef.close();
              const currentUrl = this.router.url;
              this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
                this.router.navigateByUrl(currentUrl);
              });
            },
            error: (error) => {
              console.error('Erreur lors de la création d\'un lieu:', error);
            }
          }
        );
      }

    }

  }

  ngOnInit(): void {
    this.terminalService.getTerminalStatuses().subscribe(
      terminalStatuses => {
        this.terminalStatuses = terminalStatuses;
      })
  }
}
