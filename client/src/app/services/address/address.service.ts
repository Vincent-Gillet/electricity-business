import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AddressService {

  private apiUrl: string = environment.apiUrl + '/addresses';

// 1️⃣ INJECTION HttpClient
  constructor(private http: HttpClient) {}

// 2️⃣ MÉTHODES IMPLÉMENTÉES
  getAddresses(): Observable<any> {
    let addresses = this.http.get(this.apiUrl)
    console.log(addresses);
    return addresses;
// ↳ Retourne un Observable
  }

  getAddress(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createAddress(address: any): Observable<any> {
    const token = localStorage.getItem('tokenStorage');
    const accessToken = token ? JSON.parse(token).accessToken : null;
    return this.http.post(this.apiUrl, address, {
      headers: {
        accept: 'application/json',
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      }
    });
  }

  updateAddress(id: number, address: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, address);
  }

  deleteAddress(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  // Mettre à jour une voiture
  updateAddressPublicId(publicId: string, address: any): Observable<any> {
    const token = localStorage.getItem('tokenStorage');
    const accessToken = token ? JSON.parse(token).accessToken : null;
    return this.http.put(`${this.apiUrl}/publicId/${publicId}`, address,
      {
        headers: {
          accept: 'application/json',
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      }
    );
  }

  // Supprimer une adresse par son publicId
  deleteAddressPublicId(publicId: string): Observable<any> {
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

  // Récupérer les adresses d'un utilisateur spécifique
  getAddresssByUser(): Observable<any> {
    const token = localStorage.getItem('tokenStorage');
    const accessToken = token ? JSON.parse(token).accessToken : null;
    let addresses = this.http.get(`${this.apiUrl}/user`,
      {
        headers: {
          accept: 'application/json',
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      }
    )
    return addresses;
  }
}
