import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TerminalService {

    private apiUrl: string = environment.apiUrl + '/terminals';

// 1️⃣ INJECTION HttpClient
  constructor(private http: HttpClient) {}

// 2️⃣ MÉTHODES IMPLÉMENTÉES
  getTerminals(): Observable<any> {
    let terminals = this.http.get(this.apiUrl)
    console.log(terminals);
    return terminals;
// ↳ Retourne un Observable
  }

  getTerminal(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createTerminal(terminal: any): Observable<any> {
    return this.http.post(this.apiUrl, terminal);
  }

  updateTerminal(id: number, terminal: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, terminal);
  }

  deleteTerminal(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  getTerminalsNearby(param: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/search-terminals?${param}`);
  }

}
