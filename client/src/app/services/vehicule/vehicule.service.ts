import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class VehiculeService {

  private apiUrl: string = environment.apiUrl + '/vehicules';

// 1️⃣ INJECTION HttpClient
  constructor(private http: HttpClient) {}

// 2️⃣ MÉTHODES IMPLÉMENTÉES
  getVehicules(): Observable<any> {
    let vehicules = this.http.get(this.apiUrl)
    console.log(vehicules);
    return vehicules;
// ↳ Retourne un Observable
  }

  getVehicule(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createVehicule(vehicule: any): Observable<any> {
    return this.http.post(this.apiUrl, vehicule);
  }

  updateVehicule(id: number, vehicule: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, vehicule);
  }

  deleteVehicule(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
