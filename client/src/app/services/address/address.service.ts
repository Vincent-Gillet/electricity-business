import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AddressService {

  private apiUrl: string = environment.apiUrl + '/addresses';

  // INJECTION HttpClient
  constructor(private http: HttpClient) {}

  // Créer une adresse pour l'utilisateur connecté
  createAddress(address: any): Observable<any> {
    return this.http.post(this.apiUrl, address);
  }

  // Mettre à jour une voiture
  updateAddressPublicId(publicId: string, address: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/publicId/${publicId}`, address);
  }

  // Supprimer une adresse par son publicId
  deleteAddressPublicId(publicId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/publicId/${publicId}`);
  }

  // Récupérer les adresses d'un utilisateur spécifique
  getAddressesByUser(): Observable<any> {
    return this.http.get(`${this.apiUrl}/user`);
  }

  getAddressByIdPublic(publicId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/publicId/${publicId}`);
  }

  getAddresses(): Observable<any> {
    let addresses = this.http.get(this.apiUrl)
    console.log(addresses);
    return addresses;
// ↳ Retourne un Observable
  }

  getAddress(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  updateAddress(id: number, address: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, address);
  }

  deleteAddress(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
