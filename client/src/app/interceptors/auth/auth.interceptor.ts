import {HttpContextToken, HttpErrorResponse, HttpInterceptorFn, HttpRequest} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from '../../services/auth/auth.service';
import {BehaviorSubject, catchError, filter, finalize, switchMap, take, throwError} from 'rxjs';
import {Router} from '@angular/router';

// Défini les routes publiques avec un context
export const IS_PUBLIC = new HttpContextToken<boolean>(() => false);

let isRefreshingToken = false;
let refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

// Intercepteur HTTP pour ajouter le token d'authentification
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.context.get(IS_PUBLIC)) {
    return next(req);
  }
  const authService: AuthService = inject(AuthService);
  const router: Router = inject(Router);
  let accessToken = authService.getAccessToken();

  //Vérifier
/*
  if (token && !req.headers.has('Authorization')) {
    const authReq = req.clone({
      setHeaders: {
        "Authorization": "Bearer " + token,
      }
    })
    return next(authReq);
  }
  return next(req);
*/

  const addToken = (request: HttpRequest<any>, token: string | null): HttpRequest<any> => {
    return token ? request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    }) : request;
  };

  let handledRequest = addToken(req, accessToken);

  return next(handledRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('Interceptor: Erreur de requête interceptée.', error);

      // Si l'erreur est un 401 (Unauthorized)
      if (error.status === 401) {
        // Si le refresh token est déjà en cours
        if (isRefreshingToken) {
          console.log('Interceptor: Token déjà en rafraîchissement, en attente du nouveau token.');
          // Attendre que le nouveau token soit disponible
          return refreshTokenSubject.pipe(
            filter(token => token !== null), // Attendre qu'un nouveau token soit émis
            take(1), // Prendre la première valeur
            switchMap(newAccessToken => {
              console.log('Interceptor: Re-tentative de la requête avec le nouveau token.');
              return next(addToken(req, newAccessToken)); // Rejouer la requête avec le nouveau token
            })
          );
        } else {
          // Si ce n'est pas déjà en rafraîchissement, initier le processus
          isRefreshingToken = true;
          refreshTokenSubject.next(null); // Vider le subject en attendant le nouveau token

          console.log('Interceptor: 401 détecté. Tentative de rafraîchissement du token...');

          return authService.refreshToken().pipe(
            switchMap((response: any) => {
              isRefreshingToken = false;
              if (response && response.accessToken) {
                authService.saveAccessToken(response.accessToken); // Sauvegarder le nouveau access token
                refreshTokenSubject.next(response.accessToken); // Émettre le nouveau token
                console.log('Interceptor: Token rafraîchi avec succès. Re-tentative de la requête.');
                return next(addToken(req, response.accessToken)); // Rejouer la requête originale
              } else {
                // Le refresh token a échoué (pas de nouveau access token)
                console.error('Interceptor: Le rafraîchissement du token a échoué. Déconnexion...');
                authService.logout();
                router.navigate(['/connexion']); // Rediriger si le rafraîchissement échoue
                return throwError(() => new Error('Refresh token failed.')); // Propager l'erreur
              }
            }),
            catchError((refreshError) => {
              // Gérer les erreurs pendant le rafraîchissement du token lui-même
              isRefreshingToken = false;
              console.error('Interceptor: Erreur lors du rafraîchissement du token. Déconnexion...', refreshError);
              authService.logout();
              router.navigate(['/connexion']); // Rediriger si le rafraîchissement échoue
              return throwError(() => refreshError); // Propager l'erreur
            }),
            finalize(() => {
              isRefreshingToken = false; // S'assurer que le flag est réinitialisé même en cas de succès/échec
            })
          );
        }
      } else {
        // Pour toutes les autres erreurs HTTP, propager l'erreur
/*
        console.error('Interceptor: Une autre erreur HTTP non 401 est survenue.', error);
*/
        return throwError(() => error);
      }
    })
  );
};



/*
import { HttpContextToken, HttpErrorResponse, HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpEvent } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth/auth.service';
import { catchError, switchMap, filter, take, finalize } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { Router } from '@angular/router';

export const IS_PUBLIC = new HttpContextToken<boolean>(() => false);

export const authInterceptor: HttpInterceptorFn = (request: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> => {
  const authService: AuthService = inject(AuthService);
  const router = inject(Router);

  console.log('INTERCEPTOR: Nouvelle requête interceptée. URL:', request.url); // LOG 1

  // Ne pas intercepter les requêtes publiques (ex: login, inscription)
  if (request.context.get(IS_PUBLIC)) {
    console.log('INTERCEPTOR: Requête marquée comme publique, ignorée.'); // LOG 2
    return next(request);
  }

  let accessToken = authService.getAccessToken();
  let handledRequest = request; // Initialise handledRequest

  if (accessToken) {
    console.log('INTERCEPTOR: Access Token trouvé, ajout à l\'en-tête Authorization.'); // LOG 3
    handledRequest = request.clone({
      setHeaders: {
        Authorization: `Bearer ${accessToken}`
      }
    });
  } else {
    console.log('INTERCEPTOR: Pas d\'Access Token, requête envoyée sans Authorization.'); // LOG 4
  }

  return next(handledRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('INTERCEPTOR: Erreur de requête détectée.', error); // LOG 5

      // Si l'erreur est un 401 (Unauthorized)
      if (error.status === 401) {
        console.log('INTERCEPTOR: Status 401 Unauthorized détecté.'); // LOG 6

        // Si une tentative de rafraîchissement est déjà en cours
        if (authService.isRefreshing) {
          console.log('INTERCEPTOR: Le rafraîchissement du token est déjà en cours, en attente...'); // LOG 7
          return authService.refreshTokenSubject.pipe(
            filter(token => token !== null), // Attendre qu'un nouveau token soit émis (non null)
            take(1), // Prendre la première émission après le filtre
            switchMap((token) => {
              if (token) {
                console.log('INTERCEPTOR: Nouveau token reçu après attente, rejoue la requête originale.'); // LOG 8
                // Rejouer la requête originale avec le nouvel Access Token
                const newRequest = request.clone({ // Important: cloner la *requête originale*
                  setHeaders: {
                    Authorization: `Bearer ${token}`
                  }
                });
                return next(newRequest);
              } else {
                console.error('INTERCEPTOR: Refresh token échoué pendant l\'attente, déconnexion.'); // LOG 9
                authService.logout();
                router.navigate(['/connexion']);
                return throwError(() => new Error('Refresh token failed during pending requests.'));
              }
            })
          );
        } else {
          // Ce block ne sera exécuté que par la *première* requête 401 qui arrive
          console.log('INTERCEPTOR: Démarrage du processus de rafraîchissement du token.'); // LOG 10
          authService.isRefreshing = true;
          authService.refreshTokenSubject.next(null); // Réinitialise le sujet pour les requêtes en attente

          return authService.refreshToken().pipe(
            switchMap((response: any) => {
              console.log('INTERCEPTOR: Rafraîchissement réussi, nouvel Access Token obtenu.'); // LOG 11
              authService.isRefreshing = false;
              authService.refreshTokenSubject.next(response.accessToken); // Emet le nouveau token
              // Rejouer la requête originale avec le nouvel Access Token
              const newRequest = request.clone({ // Important: cloner la *requête originale*
                setHeaders: {
                  Authorization: `Bearer ${response.accessToken}`
                }
              });
              return next(newRequest);
            }),
            catchError((refreshError) => {
              console.error('INTERCEPTOR: Échec du rafraîchissement du token.', refreshError); // LOG 12
              authService.logout(); // Déconnecte l'utilisateur
              router.navigate(['/connexion']); // Redirige
              return throwError(() => refreshError); // Propager l'erreur
            }),
            finalize(() => {
              console.log('INTERCEPTOR: Processus de rafraîchissement terminé (réussi ou échoué).'); // LOG 13
              authService.isRefreshing = false; // Assurer que le flag est reset
            })
          );
        }
      } else {
        // Si l'erreur n'est pas 401, propager l'erreur directement
        console.log('INTERCEPTOR: Erreur non-401, propagation.', error); // LOG 14
        return throwError(() => error);
      }
    })
  );
};
*/
