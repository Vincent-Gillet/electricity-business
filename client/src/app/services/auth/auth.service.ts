import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import {UserService} from '../user/user.service';
import {Router} from '@angular/router';
import {User} from '../../models/user';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl: string = environment.apiUrl + '/auth'

  constructor(private http: HttpClient) {

    this.verifyAuth();

  }

  authenticate(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, user);
  }

  authenticateRefresh(refreshToken: any): Observable<any> {
    return this.http.get(`${this.apiUrl}/refresh`, {
      headers: {
        accept: 'application/json',
        'X-Refresh-Token': `${refreshToken}`
      },
      withCredentials: true
    });
  }

  getUserWithToken(token: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/me`, {
      headers: {
        accept: 'application/json',
        'Authorization': `Bearer ${token}`
      },
      withCredentials: true
    });
  }















  private router : Router = inject(Router); // Pour la redirection

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



  //Metre des proprietes a certains état pour suivre le statu de connexion
  verifyAuth(redirectRoute: string | null = null){
    //Vérifier si un token est présent dans le storage
    const tokenData:string | null = localStorage.getItem("tokenStorage");
    let accessToken: string | null = null;
    let refreshToken: string | null = null;
    if (tokenData) {
      try {
        const parsed = JSON.parse(tokenData);
        accessToken = parsed.accessToken;
        refreshToken = parsed.refreshToken;

        console.log("parsed.accessToken : ", parsed.accessToken);

      } catch (e) {
        accessToken = null;
      }
    }
    if(accessToken){
      // Je test le token en récupérant le User associé
      this.getUserWithToken(accessToken).subscribe({
        // Un user à été récuperer
        next:(data: User)=>{
          // Je stocke mon User et renseigne l'état de l'application car User n'est plus null
          const user: User = data;
          console.log(user);
          //Initialisation du User
          this.setUser(user);

          if(redirectRoute){
            //Redirection Home / profile
            this.router.navigate([redirectRoute]);
          }
        },
        error:(error)=>{
          this.authenticateRefresh(refreshToken).subscribe({
            next:(data: any)=>{
              console.log("data", data);
              const parsed = JSON.parse(tokenData);
              parsed.accessToken = data.accessToken;
              console.log("parsed.accessToken : ", parsed.accessToken);
              const newDataToken = JSON.stringify(parsed);
              console.log("newDataToken : ", newDataToken);
              localStorage.setItem("tokenStorage", newDataToken);
              /*
                            console.log("parsed : ", parsed);
              */

              /*              accessToken =
                            tokenData = JSON.stringify(responseToken)*/
              this.verifyAuth(redirectRoute = null);
            }, error:(error)=>{
              console.log(error);
              console.log("Token expiré ou invalide");
              this.logout();
            }
          })
        }
      });
    }else{
      this.setUser(null);
    }
  }

  logout(){
    // Suppression du token qui n'a pas fonctionné pour /me
    localStorage.removeItem("tokenStorage");
    this.setUser(null);
    this.router.navigate(["connexion"]);
  }
}
