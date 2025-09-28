import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MediaService {

    private apiUrl: string = environment.apiUrl + '/medias';

// 1️⃣ INJECTION HttpClient
  constructor(private http: HttpClient) {}

// 2️⃣ MÉTHODES IMPLÉMENTÉES
  getMedias(): Observable<any> {
    let medias = this.http.get(this.apiUrl)
    console.log(medias);
    return medias;
// ↳ Retourne un Observable
  }

  getMedia(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createMedia(media: any): Observable<any> {
    return this.http.post(this.apiUrl, media);
  }

  updateMedia(id: number, media: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, media);
  }

  deleteMedia(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
