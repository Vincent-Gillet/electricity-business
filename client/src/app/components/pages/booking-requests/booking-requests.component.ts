import {Component, inject, OnInit} from '@angular/core';
import {CommonModule, NgForOf, SlicePipe} from "@angular/common";
import {
  NgbPagination,
  NgbPaginationEllipsis,
  NgbPaginationFirst,
  NgbPaginationLast, NgbPaginationModule,
  NgbPaginationNext, NgbPaginationNumber, NgbPaginationPrevious
} from "@ng-bootstrap/ng-bootstrap";
import {BookingService} from '../../../../services/booking/booking.service';
import {Booking, BookingStatus} from '../../../../models/booking';
import {Router} from '@angular/router';
import {BOOKING_STATUS_LABELS} from '../../../../constants/booking-status-labels';

@Component({
  selector: 'app-booking-requests',
  standalone: true,
    imports: [
      CommonModule,
      NgbPaginationModule
    ],
  templateUrl: './booking-requests.component.html',
  styleUrl: './booking-requests.component.scss'
})
export class BookingRequestsComponent implements OnInit {

  public bookings : any[] = [];
  private bookingService = inject(BookingService);

  bookingStatus: BookingStatus;
  private router: Router = inject(Router);

  currentUrl: string = this.router.url;

  // Pagination
  page= 1;
  pageSize = 10;

  ngOnInit(): void {
    this.loadBookings();
  }

  private loadBookings(): void {
    this.bookingService.getRequestBookingsByUser().subscribe(
      bookings => {
        this.bookings = bookings;
        const maxPage = Math.max(1, Math.ceil(this.numElement / this.pageSize));
        if (this.page > maxPage) this.page = maxPage;
      }
    );
  }

  public get numElement(): number {
    return this.bookings.length;
  }

  done() {}

  // Validation ou refus d'une réservation

  accepted(publicId: string, page: number) {
    this.bookingStatus = { statusBooking: "ACCEPTEE" };
    console.log(this.bookingStatus.statusBooking);
    console.log(this.bookingStatus);
    this.page= page;
    this.changeStatus(publicId, this.bookingStatus);
  }

  refused(publicId: string, page: number) {
    this.bookingStatus = { statusBooking: "REFUSEE" };
    console.log(this.bookingStatus.statusBooking);
    console.log(this.bookingStatus);
    this.page= page;
    this.changeStatus(publicId, this.bookingStatus);
  }

  changeStatus(publicId: string, bookingStatus: BookingStatus): void {
    this.bookingService.updateStatusBookingByPublicId(publicId, bookingStatus).subscribe({
      next: () => {
        this.loadBookings();
        console.log("Requête de réservation mise à jour avec succès.");
      },
      error: (err: any) => {
        console.error("Erreur lors du refus de la réservation :", err);
      }
    });
  }

  protected readonly bookingStatusLabels = BOOKING_STATUS_LABELS;
}
