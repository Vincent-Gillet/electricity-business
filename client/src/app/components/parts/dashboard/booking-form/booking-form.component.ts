import {Component, Inject, inject, OnInit} from '@angular/core';
import {ErrorFromComponent} from '../../error-from/error-from.component';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Router} from '@angular/router';
import {BookingService} from '../../../../services/booking/booking.service';
import {Booking} from '../../../../models/booking';
import {CarService} from '../../../../services/car/car.service';
import {Terminal} from '../../../../models/terminal';
import {empty} from 'rxjs';
import {OptionService} from '../../../../services/option/option.service';
import {BOOKING_STATUS_LABELS} from '../../../../constants/booking-status-labels';

@Component({
  selector: 'app-booking-form',
  standalone: true,
  imports: [
    ErrorFromComponent,
    FormsModule,
    ReactiveFormsModule
  ],
  templateUrl: './booking-form.component.html',
  styleUrl: './booking-form.component.scss'
})
export class BookingFormComponent implements OnInit {
  private dialogRef: MatDialogRef<BookingFormComponent> = inject(MatDialogRef<BookingFormComponent>);

  onNoClick(): void {
    this.dialogRef.close();
  }

  bookingService: BookingService = inject(BookingService);
  carService: CarService = inject(CarService);
  optionService: OptionService = inject(OptionService);

  // Propriété représentant le formulaire
  postBookingForm: FormGroup;
  // Booléens d'état
  isSubmitted = false;
  isLoading = false;
  private updateBooking = false;
  cars: any[] = [];
  options: any[] = [];

  public booking: Booking | null;
  public terminal: Terminal;
  public startingDateSearch: Date;
  public endingDateSearch: number;

  public minDate: Date = new Date();
  public maxDate: Date = new Date(this.minDate.getTime() + (1000 * 60 * 60 * 24 * 7 * 4)); // 4 semaines

  minDateTimeString = this.formatDateForInput(this.minDate);
  maxDateTimeString = this.formatDateForInput(this.maxDate);

  formatDateForInput(date: Date): string {
    const local = new Date(date);
    local.setMinutes(local.getMinutes() - local.getTimezoneOffset()); // Ajuste le fuseau
    return local.toISOString().slice(0, 16); // yyyy-MM-ddTHH:mm
  }

  constructor(
    private fb: FormBuilder,
    private router: Router,
    @Inject(MAT_DIALOG_DATA) public data: {
      booking: Booking | null,
      terminal: Terminal,
      startingDateSearch: Date,
      endingDateSearch: number
    }
  ) {
    this.booking = data.booking;
    this.terminal = data.terminal;
    this.startingDateSearch = data.startingDateSearch;
    this.endingDateSearch = data.endingDateSearch;

    this.postBookingForm = this.fb.group({
      publicId: [this.booking?.publicId || ''],
      startingDate: [this.booking?.startingDate || (this.startingDateSearch ? this.formatDateForInput(this.startingDateSearch) : ''), [Validators.required]],
      endingDate: [this.booking?.endingDate || (this.endingDateSearch ? this.endingDateSearch.toString() : ''), [Validators.required]],
      statusBooking: [this.booking?.statusBooking || 'EN_ATTENTE', [Validators.required]],
      publicIdCar: [this.booking?.publicIdCar || '', [Validators.required]],
      publicIdTerminal: [this.terminal?.publicId || '', [Validators.required]],
      publicIdOption: [this.booking?.publicIdOption || ''],
    });
    if (this.booking) {
      this.updateBooking = true;
    }
  }

  ngOnInit() {
    this.carService.getCarsByUser().subscribe(
      cars => {
        this.cars = cars;
        console.log("Cars dans le booking form : ", this.cars);
      }
    );
    this.optionService.getOptionsByTerminal(this.terminal.publicId).subscribe(
      options => {
        this.options = options;
        console.log("Terminal dans le booking form : ", this.options);
      }
    );
  }

  onSubmit():void {
    // TODO: Use EventEmitter with form value
    console.warn(this.postBookingForm.value);

    this.isSubmitted = true;

    console.log("MON FORM EST SOUMIS");
    console.log("postBookingForm.valid ",this.postBookingForm.valid);
    console.log("Toutes les valeurs des control du groupe -> postBookingForm.value ",this.postBookingForm.value);
    console.log("Recuperer un seul control avec postBookingForm.get('email')",this.postBookingForm.get("licensePlate"));
    console.log("Recuperer la validité d'un control avec postBookingForm.get('email').valid",this.postBookingForm.get("brand")?.valid);
    console.log("Recuperer les erreurs d'un control avec postBookingForm.get('motDePasse').errors",this.postBookingForm.get("licensePlate")?.errors);
    console.log("Recuperer un seul control avec postBookingForm.get('motDePasse')",this.postBookingForm.get("licensePlate"));


    if (this.postBookingForm.valid) {
      // 3. Activer le state de chargement
      this.isLoading = true;

      // 4. Récupérer les données du formulaire
      const bookingData = this.postBookingForm.value;

      // Convertir endingDate (durée en minutes)
      const durationMinutes = parseInt(bookingData.endingDate, 10);

      // Calculer la date de fin à partir de la date de début
      const startDate = new Date(bookingData.startingDate); // ex : "2025-11-02T19:50"
      const endDate = new Date(startDate.getTime() + durationMinutes * 60 * 1000);

      console.log("Date de début :", startDate);
      console.log("Durée en minutes :", durationMinutes);
      console.log("Date de fin calculée :", endDate);

      // 🟢 Conversion en ISO locale avec décalage horaire
      const pad = (n: number) => n.toString().padStart(2, '0');
      const offset = -endDate.getTimezoneOffset(); // en minutes
/*
      const sign = offset >= 0 ? '+' : '-';
      const hoursOffset = pad(Math.floor(Math.abs(offset) / 60));
      const minutesOffset = pad(Math.abs(offset) % 60);
*/

      // ISO avec offset
      bookingData.endingDate =
      `${endDate.getFullYear()}-${pad(endDate.getMonth()+1)}-${pad(endDate.getDate())}` +
      `T${pad(endDate.getHours())}:${pad(endDate.getMinutes())}`;

      // Remplacement de la valeur par la version avec fuseau
/*
      bookingData.endingDate = endDate;
*/

      console.log('Données bookingData : ', bookingData);

      // 5. Simuler un appel API avec setTimeout
      // (Dans un vrai projet, ça serait un appel HTTP)
      setTimeout(() => {
        if (this.updateBooking) {
          this.bookingService.updateBookingByPublicId(this.booking.publicId, bookingData).subscribe(
            {
              next: (response) => {
                console.log('Réservation mise à jour:', response);
                this.dialogRef.close();
                this.router.navigateByUrl('/tableau-de-bord/mes-reservations', { skipLocationChange: true }).then(() => {
                  this.router.navigate(['./tableau-de-bord/mes-reservations']);
                });
              },
              error: (error) => {
                console.error('Erreur lors de la mise à jour de la voiture:', error);
              }
            }
          );
          return;
        } else {
          this.bookingService.createBookingByPublicId(bookingData).subscribe(
            {
              next: (response) => {
                console.log('Voiture créée:', response);
                this.dialogRef.close();
                this.router.navigateByUrl('bookings', { skipLocationChange: true }).then(() => {
                  this.router.navigate(['./tableau-de-bord/mes-reservations']);
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

/*
  protected readonly empty = empty;
*/

  protected readonly bookingStatusLabels = BOOKING_STATUS_LABELS;
}
