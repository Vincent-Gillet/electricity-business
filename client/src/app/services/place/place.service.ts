import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PlaceService {

  private apiUrl: string = environment.apiUrl + '/places';

// 1️⃣ INJECTION HttpClient
  constructor(private http: HttpClient) {}

// 2️⃣ MÉTHODES IMPLÉMENTÉES
  getPlaces(): Observable<any> {
    let places = this.http.get(this.apiUrl)
    console.log(places);
    return places;
// ↳ Retourne un Observable
  }

  getPlace(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createPlace(place: any): Observable<any> {
    return this.http.post(this.apiUrl, place);
  }

  updatePlace(id: number, place: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, place);
  }

  deletePlace(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  // Méthodes user

  createPlaceByPublicId(place: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/user`, place);
  }

  // Mettre à jour une voiture
  updatePlaceByPublicId(publicId: string, place: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/publicId/${publicId}`, place);
  }

  // Supprimer une voiture
  deletePlaceByPublicId(publicId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/publicId/${publicId}`);
  }

  // Récupérer les voitures d'un utilisateur spécifique
  getPlacesByUser(): Observable<any> {
    return this.http.get(`${this.apiUrl}/user`);
  }
}
