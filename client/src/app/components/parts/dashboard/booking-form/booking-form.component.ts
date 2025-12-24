import {Component, Inject, inject, OnInit} from '@angular/core';
import {ErrorFromComponent} from '../../error-from/error-from.component';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Router} from '@angular/router';
import {BookingService} from '../../../../services/booking/booking.service';
import {Booking} from '../../../../models/booking';
import {CarService} from '../../../../services/car/car.service';
import {Terminal} from '../../../../models/terminal';
import {forkJoin} from 'rxjs';
import {OptionService} from '../../../../services/option/option.service';
import {BOOKING_STATUS_LABELS} from '../../../../constants/booking-status-labels';
import {Option} from '../../../../models/option';
import {Car} from '../../../../models/car';

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
  private bookingService: BookingService = inject(BookingService);
  private carService: CarService = inject(CarService);
  private optionService: OptionService = inject(OptionService);
  private fb: FormBuilder = inject(FormBuilder);
  private router: Router = inject(Router);

  onNoClick(): void {
    this.dialogRef.close();
  }

  postBookingForm: FormGroup;
  isSubmitted = false;
  isLoading = false;
  private updateBooking = false;
  cars: Car[] = [];
  options: Option[] = [];

  public booking: Booking | null;
  public terminal: Terminal;
  public startingDateSearch: Date;
  public endingDateSearch: number;

  public minDate: Date = new Date();
  public maxDate: Date = new Date(this.minDate.getTime() + (1000 * 60 * 60 * 24 * 7 * 4));

  minDateTimeString = this.formatDateForInput(this.minDate);
  maxDateTimeString = this.formatDateForInput(this.maxDate);

  formatDateForInput(date: Date): string {
    const local = new Date(date);
    local.setMinutes(local.getMinutes() - local.getTimezoneOffset());
    return local.toISOString().slice(0, 16);
  }

  constructor(
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
    this.isLoading = true;
    forkJoin({
      cars: this.carService.getCarsByUser(),
      options: this.optionService.getOptionsByTerminal(this.terminal.publicId)
    }).subscribe({
      next: ({ cars, options }) => {
        this.cars = cars;
        this.options = options;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors du chargement des données :', error);
        this.isLoading = false;
      }
    });
  }

  onSubmit():void {
    this.isSubmitted = true;

    if (this.postBookingForm.valid) {
      this.isLoading = true;

      // Récupérer les données du formulaire
      const bookingData = this.postBookingForm.value;

      // Convertir endingDate (durée en minutes)
      const durationMinutes = parseInt(bookingData.endingDate, 10);

      // Calculer la date de fin à partir de la date de début
      const startDate = new Date(bookingData.startingDate);
      const endDate = new Date(startDate.getTime() + durationMinutes * 60 * 1000);

      // Conversion en ISO locale avec décalage horaire
      const pad = (n: number) => n.toString().padStart(2, '0');

      // ISO avec offset
      bookingData.endingDate =
      `${endDate.getFullYear()}-${pad(endDate.getMonth()+1)}-${pad(endDate.getDate())}` +
      `T${pad(endDate.getHours())}:${pad(endDate.getMinutes())}`;

      const operation$ = this.updateBooking
        ? this.bookingService.updateBookingByPublicId(this.booking!.publicId, bookingData)
        : this.bookingService.createBookingByPublicId(bookingData);

      operation$.subscribe({
        next: () => {
          this.dialogRef.close();
          this.router.navigate(['/tableau-de-bord/mes-reservations']);
        },
        error: () => {
          this.isLoading = false;
        }
      });
    }
  }

  protected readonly bookingStatusLabels = BOOKING_STATUS_LABELS;
}
