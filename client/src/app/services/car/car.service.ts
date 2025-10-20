import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CarService {

  private apiUrl: string = environment.apiUrl + '/cars';

  // Injecter HttpClient
  constructor(private http: HttpClient) {}

  // Récupérer toutes les voitures
  getCars(): Observable<any> {
    let cars = this.http.get(this.apiUrl)
    return cars;
  }

  // Récupérer une voiture par son ID
  getCar(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createCar(car: any): Observable<any> {
    const token = localStorage.getItem('tokenStorage');
    const accessToken = token ? JSON.parse(token).accessToken : null;
/*
    const accessToken = token.access_token;
*/
    return this.http.post(this.apiUrl, car,
      {
        headers: {
          accept: 'application/json',
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      }
    );
  }

  // Mettre à jour une voiture
  updateCar(publicId: string, car: any): Observable<any> {
    const token = localStorage.getItem('tokenStorage');
    const accessToken = token ? JSON.parse(token).accessToken : null;
    return this.http.put(`${this.apiUrl}/publicId/${publicId}`, car,
      {
        headers: {
          accept: 'application/json',
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      }
    );
  }

  // Supprimer une voiture
  deleteCar(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  // Supprimer une voiture
  deleteCarPublicId(publicId: string): Observable<any> {
    const token = localStorage.getItem('tokenStorage');
    const accessToken = token ? JSON.parse(token).accessToken : null;
    return this.http.delete(`${this.apiUrl}/publicId/${publicId}`,
      {
        headers: {
          accept: 'application/json',
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      }
    );
  }

  // Récupérer les voitures d'un utilisateur spécifique
  getCarsByUser(): Observable<any> {
    const token = localStorage.getItem('tokenStorage');
    const accessToken = token ? JSON.parse(token).accessToken : null;
    let cars = this.http.get(`${this.apiUrl}/user`,
      {
        headers: {
          accept: 'application/json',
            'Authorization': `Bearer ${accessToken}`,
            'Content-Type': 'application/json'
        }
      }
    )
    return cars;
  }
}
