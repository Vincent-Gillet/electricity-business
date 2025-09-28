import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class RepairerService {

  private apiUrl: string = environment.apiUrl + '/repairers';

// 1️⃣ INJECTION HttpClient
  constructor(private http: HttpClient) {}

// 2️⃣ MÉTHODES IMPLÉMENTÉES
  getRepairers(): Observable<any> {
    let repairers = this.http.get(this.apiUrl)
    console.log(repairers);
    return repairers;
// ↳ Retourne un Observable
  }

  getRepairer(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createRepairer(repairer: any): Observable<any> {
    return this.http.post(this.apiUrl, repairer);
  }

  updateRepairer(id: number, repairer: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, repairer);
  }

  deleteRepairer(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
