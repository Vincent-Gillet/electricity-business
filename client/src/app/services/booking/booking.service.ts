import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BookingService {

  private apiUrl: string = environment.apiUrl + '/bookings';

// 1️⃣ INJECTION HttpClient
  constructor(private http: HttpClient) {}

// 2️⃣ MÉTHODES IMPLÉMENTÉES
  getBookings(): Observable<any> {
    let bookings = this.http.get(this.apiUrl)
    console.log(bookings);
    return bookings;
// ↳ Retourne un Observable
  }

  getBooking(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createBooking(booking: any): Observable<any> {
    return this.http.post(this.apiUrl, booking);
  }

  updateBooking(id: number, booking: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, booking);
  }

  deleteBooking(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
