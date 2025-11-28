import {Component, inject, OnInit} from '@angular/core';
import {BookingService} from '../../../../services/booking/booking.service';
import {CurrencyPipe, DatePipe, NgForOf, SlicePipe} from '@angular/common';
import {
  NgbPagination, NgbPaginationEllipsis,
  NgbPaginationFirst,
  NgbPaginationLast,
  NgbPaginationNext, NgbPaginationNumber,
  NgbPaginationPrevious
} from '@ng-bootstrap/ng-bootstrap';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {BOOKING_STATUS_LABELS} from '../../../../constants/booking-status-labels';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [
    DatePipe,
    NgbPagination,
    NgbPaginationFirst,
    NgbPaginationLast,
    NgbPaginationPrevious,
    NgbPaginationNext,
    NgbPaginationEllipsis,
    NgbPaginationNumber,
    CurrencyPipe,
    ReactiveFormsModule
  ],
  templateUrl: './my-bookings.component.html',
  styleUrl: './my-bookings.component.scss'
})
export class MyBookingsComponent implements OnInit {
  page= 1;
  pageSize = 10;

  bookings: any[] = [];
  private bookingService: BookingService= inject(BookingService)

  searchBookingForm: FormGroup;

  statusBookings: any[] = [];

  constructor(private fb: FormBuilder, private router: Router) {
    //Création du form
    this.searchBookingForm = this.fb.group(
      {
        startingDate: [''],
        endingDate: [''],
        orderBooking: ['ASC'],
        statusBooking: [''],
      }
    );
  }

  ngOnInit() {
    this.loadBookings();

    this.bookingService.getStatusBooking().subscribe({
      next: (data) => {
        console.log("data status booking : ", data);
        this.statusBookings = data;
      },
      error: (err) => {
        console.error("Erreur lors de la récupération des status de réservation :", err);
      }
    });
  }

  private loadBookings(): void {
/*    this.bookingService.getBookingsByUser().subscribe(
      bookings => {
        this.bookings = bookings;
        const maxPage = Math.max(1, Math.ceil(this.numElement / this.pageSize));
        if (this.page > maxPage) this.page = maxPage;
      }
    );*/

    this.chargerBookings(this.getDefaultParams());
/*    setTimeout(() => {
      this.chargerBookings(this.getDefaultParams());
    }, 2000);*/
  }

  private buildParams(): any {
    const params = {...this.searchBookingForm.value};

    params.orderBooking = this.searchBookingForm.get('orderBooking').value ? 'ASC' : 'DESC';

    return params;
  }

  onSubmit() {
    if (this.searchBookingForm.valid) {


      console.log("MON FORM EST SOUMIS");
      console.log("loginForm.valid ", this.searchBookingForm.valid);
      console.log("Toutes les valeurs des control du groupe -> loginForm.value ",this.searchBookingForm.value);

      console.log("this.searchBorneForm.value : ", this.searchBookingForm.value);


      const params = this.buildParams();

      this.chargerBookings(params);



/*      const encodeAddress = encodeURIComponent(address).replace(/%20/g, '+');
      console.log("encodeAddress : ", encodeAddress);
      this.nominatimService.getGeoLocationWithAdress(encodeAddress).subscribe(
        {
          next: (response) => {
            this.long = response.features[0].geometry.coordinates[0];
            this.lat = response.features[0].geometry.coordinates[1];
            const params = this.buildParams(this.long, this.lat);
            this.chargerTerminals(params);
          },
          error: (error) => {
            console.error('Error login user:', error);
          }
        }
      )
    } else {
      this.geolocation.subscribe(data => {
        this.lat = data.coords.latitude;
        this.long = data.coords.longitude;
        const params = this.buildParams(this.long, this.lat);
        this.chargerTerminals(params);
      })*/
    }



  };



  getDefaultParams() {
    return {
      startingDate : this.searchBookingForm.get('startingDate')?.value,
      endingDate : this.searchBookingForm.get('endingDate')?.value,
      orderBooking : this.searchBookingForm.get('orderBooking')?.value === true ? false : undefined
    };
  }

  // Méthode pour charger les réservations
  chargerBookings(paramsObj: any) {
    console.log("paramsObj : ", paramsObj);
    const params = Object.entries(paramsObj)
      .map(([key, value]) => {
        if (value !== null && value !== undefined && value !== '') {
          return `${key}=${value}`;
        }
        return undefined;
      })
      .filter(Boolean)
      .join('&');
    console.log("params : ", params);

    // Appel du service pour récupérer les bornes
    this.bookingService.getBookingsByUser(params).subscribe(bookings => {
      //Affectation des datas
      this.bookings = bookings;
      const maxPage = Math.max(1, Math.ceil(this.numElement / this.pageSize));
      if (this.page > maxPage) this.page = maxPage;
    });
  }

  onReset() {
    this.searchBookingForm.reset();
  }

  // Gestion de la pagination
  done() {}

  public get numElement(): number {
    return this.bookings.length;
  }

  // Téléchargement PDF
  downloadPdf(publicId: string) {
    this.bookingService.downloadBookingPdf(publicId).subscribe({
      next: (response: Blob) => {
        const url = window.URL.createObjectURL(response);
        const a = document.createElement('a');
        a.href = url;
        a.download = `booking_${publicId}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err: any) => {
        console.error('Error downloading PDF:', err);
      }
    });
  }

  // Téléchargement Excel
  downloadExcel() {
    this.bookingService.downloadBookingsExcel().subscribe({
      next: (response: Blob) => {
        const url = window.URL.createObjectURL(response);
        const a = document.createElement('a');
        a.href = url;
        a.download = `booking.xlsx`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      },
      error: (err: any) => {
        console.error('Error downloading PDF:', err);
      }
    });
  }

  protected readonly bookingStatusLabels = BOOKING_STATUS_LABELS;
}
