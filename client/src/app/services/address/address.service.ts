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
    return this.http.post(this.apiUrl, address);
  }

  updateAddress(id: number, address: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, address);
  }

  deleteAddress(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
