import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpContext, HttpErrorResponse} from '@angular/common/http';
import {BehaviorSubject, catchError, Observable, switchMap, tap, throwError} from 'rxjs';
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
    this.verifyAuth();
  }

  authenticate(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, user, {
      context: new HttpContext().set(IS_PUBLIC, true),
    });
  }

/*  authenticate(credentials: any): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, credentials, {
      context: new HttpContext().set(IS_PUBLIC, true),
      withCredentials: true // Très important pour envoyer et recevoir les cookies (dont HttpOnly)
    }).pipe(
      tap((response: AuthResponse) => {
        // Stocke uniquement l'accessToken, le refreshToken est géré par le cookie HttpOnly
        sessionStorage.setItem('tokenStorage', JSON.stringify({ accessToken: response.accessToken }));
      }),
      catchError((error: HttpErrorResponse) => {
        // Gérer les erreurs spécifiques de login ici si nécessaire
        return throwError(() => error);
      })
    );
  }*/

/*  login(credentials: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/authenticate`, credentials).pipe(
      tap(response => {
        if (response && response.accessToken) {
          this.saveAccessToken(response.accessToken);
          // Si le backend renvoie le refresh token dans le corps de la réponse
          // (ce qui est moins courant avec des cookies HttpOnly),
          // alors vous le sauveriez ici.
          // if (response.refreshToken) {
          //   this.saveRefreshToken(response.refreshToken);
          // }
          console.log('Login réussi. Access Token sauvegardé.');
        }
      })
    );
  }*/

  authenticateRefresh(refreshToken: any): Observable<any> {
    return this.http.get(`${this.apiUrl}/refresh`, {
      context: new HttpContext().set(IS_PUBLIC, true),
      headers: {
        accept: 'application/json',
        'X-Refresh-Token': `${refreshToken}`
      },
      withCredentials: true
    });
  }

  refreshToken(): Observable<any> {
    console.log('AuthService: Appel au endpoint de rafraîchissement du token.');
    // Ce endpoint doit lire le refresh token du cookie HttpOnly de la requête
    // et renvoyer un nouvel access token.
    return this.http.post<any>(`${this.apiUrl}/refresh-token`, {}).pipe( // Le corps est vide car le refresh token est dans le cookie
      tap(response => {
        if (response && response.accessToken) {
          this.saveAccessToken(response.accessToken);
          console.log('AuthService: Nouveau Access Token reçu et sauvegardé.');
        } else {
          // Si le backend ne renvoie pas d'accessToken, cela signifie que le refresh token est probablement invalide
          // ou expiré côté serveur.
          console.error('AuthService: Le rafraîchissement du token n\'a pas retourné de nouvel Access Token.');
          this.logout(); // Déconnecter l'utilisateur si le refresh échoue
          throw new Error('Refresh token response missing accessToken.');
        }
      }),
      catchError(error => {
        console.error('AuthService: Erreur lors du rafraîchissement du token.', error);
        this.logout(); // Toujours déconnecter en cas d'échec du refresh token
        return throwError(() => error);
      })
    );
  }

  getAccessToken(): string | null {
    const tokenData = sessionStorage.getItem("tokenStorage");
    return tokenData ? JSON.parse(tokenData).accessToken : null;
  }

  private router: Router = inject(Router); // Pour la redirection

  private initializedSubject = new BehaviorSubject<boolean>(false);
  public initialized$ = this.initializedSubject.asObservable();
  //Utilisation d'un observable pour partager la prop User aux autre composants
  private userSubject = new BehaviorSubject<User | null>(null);
  public user$ = this.userSubject.asObservable();
  //Le $ en fin de variable est une convention pour identifier les data observable

  //Getter pour la simplicité d'utilisation
  get user(): User | null {
    return this.userSubject.value;
  }

  // Setter pour mettre à jour l'observable utilisateur
  setUser(user: User | null): void {
    this.userSubject.next(user);
  }


  verifyAuth(redirectRoute: string | null = null) {
    const tokenData: string | null = sessionStorage.getItem('tokenStorage');
    let accessToken: string | null = null;
    let refreshToken: string | null = null;

    if (tokenData) {
      try {
        const parsed = JSON.parse(tokenData);
        accessToken = parsed.accessToken;
        refreshToken = parsed.refreshToken;
      } catch (e) {
        accessToken = null;
      }
    }

    if (accessToken) {
      this.userService.getUserWithToken(accessToken).subscribe({
        next: (data: User | null) => {
          if (data) {
            this.setUser(data);
            this.initializedSubject.next(true);
            if (redirectRoute) this.router.navigate([redirectRoute]);
          } else {
            // Gérer le cas où l'utilisateur n'est pas trouvé
            this.logout();
            this.initializedSubject.next(true);
          }
        },
        error: (error) => {
          // try refresh
          if (refreshToken) {
            this.authenticateRefresh(refreshToken).subscribe({
              next: (data: any) => {
                const parsed = tokenData ? JSON.parse(tokenData) : {};
                parsed.accessToken = data.accessToken;
                sessionStorage.setItem('tokenStorage', JSON.stringify(parsed));
                // re-run verifyAuth to fetch user with new token
                this.verifyAuth(redirectRoute);
              },
              error: () => {
                this.logout();
                this.initializedSubject.next(true);
              }
            });
          } else {
            this.logout();
            this.initializedSubject.next(true);
          }
        }
      });
    } else {
      this.setUser(null);
      this.initializedSubject.next(true);
    }
  }

  logout(){
    // Suppression du token qui n'a pas fonctionné pour /me
    sessionStorage.removeItem("tokenStorage");
    this.setUser(null);
    this.router.navigate(["connexion"]);
  }

  saveAccessToken(token: string): void {
    sessionStorage.setItem(this.ACCESS_TOKEN_KEY, token);
  }

  isAuthenticated(): boolean {
    const token = this.getAccessToken();
    // Vous devriez aussi vérifier si le token est encore valide (non expiré)
    // Pour une vérification simple, nous nous basons juste sur sa présence.
    return !!token;
  }
}
