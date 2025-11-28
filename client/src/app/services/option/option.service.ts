import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OptionService {

  private apiUrl: string = environment.apiUrl + '/options';

// 1️⃣ INJECTION HttpClient
  constructor(private http: HttpClient) {}

// 2️⃣ MÉTHODES IMPLÉMENTÉES
  getOptions(): Observable<any> {
    let options = this.http.get(this.apiUrl)
    console.log(options);
    return options;
// ↳ Retourne un Observable
  }

  getOption(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createOption(option: any): Observable<any> {
    return this.http.post(this.apiUrl, option);
  }

  updateOption(id: number, option: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, option);
  }

  deleteOption(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  // Méthodes user

  createOptionByPublicId(option: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/user`, option);
  }

  // Mettre à jour une voiture
  updateOptionByPublicId(publicId: string, option: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/publicId/${publicId}`, option);
  }

  // Supprimer une voiture
  deleteOptionByPublicId(publicId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/publicId/${publicId}`);
  }

  // Récupérer les voitures d'un utilisateur spécifique
  getOptionsByUser(): Observable<any> {
    return this.http.get(`${this.apiUrl}/user`);
  }

  getOptionsByTerminal(terminal: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/terminal/${terminal}`);
  }
}
