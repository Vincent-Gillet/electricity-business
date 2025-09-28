import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CarService {

  private apiUrl: string = environment.apiUrl + '/cars';

// 1️⃣ INJECTION HttpClient
  constructor(private http: HttpClient) {}

// 2️⃣ MÉTHODES IMPLÉMENTÉES
  getCars(): Observable<any> {
    let cars = this.http.get(this.apiUrl)
    console.log(cars);
    return cars;
// ↳ Retourne un Observable
  }

  getCar(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createCar(car: any): Observable<any> {
    return this.http.post(this.apiUrl, car);
  }

  updateCar(id: number, car: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, car);
  }

  deleteCar(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
