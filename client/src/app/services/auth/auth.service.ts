import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpContext, HttpErrorResponse} from '@angular/common/http';
import {BehaviorSubject, catchError, filter, finalize, Observable, of, switchMap, take, tap, throwError} from 'rxjs';
import {UserService} from '../user/user.service';
import {Router} from '@angular/router';
import {User} from '../../models/user';
import {environment} from '../../../environments/environment';
import {IS_PUBLIC} from '../../interceptors/auth/auth.interceptor';

interface AuthResponse {
  accessToken: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl: string = environment.apiUrl + '/auth'
  private userService: UserService = inject(UserService);
  private readonly ACCESS_TOKEN_KEY = 'access_token';

  private isRefreshing: boolean = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

  constructor(private http: HttpClient) {
    this.verifyAuth().subscribe();
    this.setupStorageListener();
  }

  authenticate(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${this.apiUrl}/login`,
      { emailUser: email, passwordUser: password },
      {
        context: new HttpContext().set(IS_PUBLIC, true),
        withCredentials: true
      }
    ).pipe(
      tap(response => {
        this.saveAccessToken(response.accessToken);
      })
    );
  }

  refreshToken(): Observable<AuthResponse> {
    if (this.isRefreshing) {
      return this.refreshTokenSubject.pipe(
        filter((token): token is string => token !== null),
        take(1),
        switchMap((token) => of({ accessToken: token }))
      );
    }

    this.isRefreshing = true;
    this.refreshTokenSubject.next(null);

    return this.http.post<AuthResponse>(
      `${this.apiUrl}/refresh`,
      {},
      { context: new HttpContext().set(IS_PUBLIC, true), withCredentials: true } // <-- options
    ).pipe(
      tap((response) => {
        if (!response?.accessToken) {
          throw new Error('Refresh response missing accessToken');
        }
        this.saveAccessToken(response.accessToken);
        this.refreshTokenSubject.next(response.accessToken);
      }),
      catchError((error) => {
        this.clearAuthState();
        return throwError(() => error);
      }),
      finalize(() => {
        this.isRefreshing = false;
      })
    );
  }

  private router: Router = inject(Router);

  private _initialized = new BehaviorSubject<boolean>(false);
  private _user = new BehaviorSubject<User | null>(null);

  initialized$ = this._initialized.asObservable();
  user$ = this._user.asObservable();

  verifyAuth(): Observable<User | null> {
    const token = this.getAccessToken();
    if (!token) {
      this.setUser(null);
      return of(null);
    }

    if (this.isTokenExpired(token)) {
      this.clearAuthState();
      return of(null);
    }

    try {
      const user = this.decodeToken();
      this.setUser(user);
      return of(user);
    } catch (error) {
      console.error('Token decoding failed:', error);
      this.clearAuthState();
      return of(null);
    }
  }

  // Vérifie si le token JWT est expiré
  isTokenExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }

  // Déconnecte l'utilisateur
  logout(): void {
    this.http.post(`${this.apiUrl}/logout`, {}, { withCredentials: true })
      .pipe(finalize(() => this.clearAuthState()))
      .subscribe();
  }

  // Réinitialise l'état d'authentification
  private clearAuthState(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    this.setUser(null);
    this.router.navigate(['/connexion']);
  }

  // Sauvegarde l'accessToken dans localStorage
  public saveAccessToken(token: string): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, token);
  }

  // Récupère l'accessToken depuis localStorage
  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  // Vérifie si l'utilisateur est authentifié (accessToken présent)
  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  // Met à jour l'utilisateur courant
  private setUser(user: User | null): void {
    this._user.next(user);
    this._initialized.next(true);
  }

  // Getter pour l'utilisateur courant
  get user(): User | null {
    return this._user.value;
  }

  // Décode le token JWT pour extraire les informations utilisateur
  private decodeToken(): User | null {
    const token = this.getAccessToken();
    if (!token) return null;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return {
        pseudo: payload.sub,
        emailUser: payload.emailUser,
        surnameUser: payload.surnameUser,
        firstName: payload.firstName,
        dateOfBirth: payload.dateOfBirth,
        phone: payload.phone,
      };
    } catch {
      return null;
    }
  }

  // Vérifie et met à jour l'utilisateur courant à partir du token
  verifyToken(): void {
    const user = this.decodeToken();
    this.setUser(user);
  }

  // Écoute les changements de localStorage pour synchroniser l'état d'authentification
  private setupStorageListener(): void {
    window.addEventListener('storage', (event) => {
      if (event.key === this.ACCESS_TOKEN_KEY) {
        if (!event.newValue) {
          this.clearAuthState();
        } else {
          this.saveAccessToken(event.newValue);
          this.verifyToken();
        }
      }
    });
  }
}
