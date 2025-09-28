import {Component, inject} from '@angular/core';
import {BookingService} from '../../../../services/booking/booking.service';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [],
  templateUrl: './my-bookings.component.html',
  styleUrl: './my-bookings.component.scss'
})
export class MyBookingsComponent {
  page= 1;
  pageSize = 2;

  bookings: any[] = [];

// INJECTION du service
  private bookingService: BookingService= inject(BookingService)


  ngOnInit() {
    this.chargerBooking();
  }

// APPEL DE LA MÉTHODE
  chargerBooking() {
    // subscribe
    this.bookingService.getBookings().subscribe(data => {
      console.log('API response:', data); // Check the response here

      //Affectation des datas
      this.bookings = data;
    });
  }
}
