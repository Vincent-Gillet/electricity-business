import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {User} from '../../models/user';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private apiUrl: string = environment.apiUrl + '/users';

// 1️⃣ INJECTION HttpClient
  constructor(private http: HttpClient) {}

// 2️⃣ MÉTHODES IMPLÉMENTÉES
  getUsers(): Observable<any> {
    return this.http.get(this.apiUrl);
// ↳ Retourne un Observable
  }

  getUser(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createUser(user: any): Observable<any> {
    return this.http.post(this.apiUrl, user
/*
      , {headers : { 'Content-Type': 'application/json' }, withCredentials: true}
*/
    );
  }

  updateUser(id: number, user: any): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, user);
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  updateUserByToken(user: User): Observable<any> {
    const token = localStorage.getItem('tokenStorage');
    const accessToken = token ? JSON.parse(token).accessToken : null;
    return this.http.put(`${this.apiUrl}/token`, user,
      {
        headers: {
          accept: 'application/json',
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      }
    );
  }

  updatePasswordByToken(user: User): Observable<any> {
    const token = localStorage.getItem('tokenStorage');
    const accessToken = token ? JSON.parse(token).accessToken : null;
    return this.http.put(`${this.apiUrl}/password`, user,
      {
        headers: {
          accept: 'application/json',
          'Authorization': `Bearer ${accessToken}`,
          'Content-Type': 'application/json'
        }
      }
    );
  }

}
