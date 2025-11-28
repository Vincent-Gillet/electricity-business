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

  // Méthodes user

  // Créer une réservation
  createBookingByPublicId(booking: any): Observable<any> {

    return this.http.post(`${this.apiUrl}/user`, booking);
  }

  // Mettre à jour une réservation
  updateBookingByPublicId(publicId: string, booking: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/publicId/${publicId}`, booking);
  }

  // Supprimer une réservation
  deleteBookingByPublicId(publicId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/publicId/${publicId}`);
  }

  // Récupérer les réservations d'un utilisateur spécifique
/*  getBookingsByUser(): Observable<any> {
    const token = localStorage.getItem('tokenStorage');
    const accessToken = token ? JSON.parse(token).accessToken : null;
    return this.http.get(`${this.apiUrl}/user/client`,
      {
        headers: {
          accept: 'application/json',
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      }
    );
  }*/

  // Récupérer les réservations d'un utilisateur spécifique avec paramètre
  getBookingsByUser(param: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/user/client?${param}`);
  }

  // Récupérer les réservations d'un utilisateur spécifique
  getRequestBookingsByUser(): Observable<any> {
    return this.http.get(`${this.apiUrl}/user/owner`);
  }

  // Mettre à jour la réservation par publicId
  updateStatusBookingByPublicId(publicId: string, booking: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/publicId/${publicId}/status`, booking);
  }

  // Télécharger le PDF de la réservation
  downloadBookingPdf(publicId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/publicId/${publicId}/pdf`);
  }

  // Télécharger le PDF de la réservation
  downloadBookingsExcel(): Observable<any> {
    return this.http.get(`${this.apiUrl}/excel`);
  }


  getStatusBooking(): Observable<any> {
    return this.http.get(`${this.apiUrl}/booking-status`);
  }
}
