/*
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
/!*
        console.error('Interceptor: Une autre erreur HTTP non 401 est survenue.', error);
*!/
        return throwError(() => error);
      }
    })
  );
};

*/


import { HttpContextToken, HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../../services/auth/auth.service';
import { BehaviorSubject, catchError, filter, finalize, switchMap, take, throwError } from 'rxjs';
import { Router } from '@angular/router';

// Défini les routes publiques avec un contexte
export const IS_PUBLIC = new HttpContextToken<boolean>(() => false);

// Utilisation d'un singleton pour éviter les variables globales
const refreshTokenSubject = new BehaviorSubject<string | null>(null);
let isRefreshingToken = false;

// Intercepteur HTTP pour ajouter le token d'authentification
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Ignorer les requêtes publiques ou les requêtes vers /auth/refresh
  if (req.context.get(IS_PUBLIC) || req.url.includes('/auth/refresh')) {
    return next(req);
  }

  const authService = inject(AuthService);
  const router = inject(Router);
  const accessToken = authService.getAccessToken();

  // Cloner la requête avec le token d'autorisation
  const authReq = accessToken
    ? req.clone({
      setHeaders: { Authorization: `Bearer ${accessToken}` },
    })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      console.error('[AuthInterceptor] Erreur interceptée:', error.status, error.url);

      // Gérer uniquement les erreurs 401 (Unauthorized)
      if (error.status !== 401) {
        return throwError(() => error);
      }

      // Si le token est déjà en cours de rafraîchissement, attendre le nouveau token
      if (isRefreshingToken) {
        console.log('[AuthInterceptor] Token en cours de rafraîchissement. Attente...');
        return refreshTokenSubject.pipe(
          filter((token): token is string => token !== null),
          take(1),
          switchMap((newToken) => {
            console.log('[AuthInterceptor] Nouveau token reçu. Rejouer la requête...');
            return next(
              req.clone({
                setHeaders: { Authorization: `Bearer ${newToken}` },
              })
            );
          })
        );
      }

      // Sinon, initier le rafraîchissement du token
      console.log('[AuthInterceptor] 401 détecté. Rafraîchissement du token...');
      isRefreshingToken = true;
      refreshTokenSubject.next(null);

      return authService.refreshToken().pipe(
        switchMap((response) => {
          const newAccessToken = response.accessToken;
          authService.saveAccessToken(newAccessToken);
          refreshTokenSubject.next(newAccessToken);

          console.log('[AuthInterceptor] Token rafraîchi. Rejouer la requête...');
          return next(
            req.clone({
              setHeaders: { Authorization: `Bearer ${newAccessToken}` },
            })
          );
        }),
        catchError((refreshError) => {
          console.error('[AuthInterceptor] Échec du rafraîchissement:', refreshError);
          authService.logout();
          router.navigate(['/connexion']);
          return throwError(() => refreshError);
        }),
        finalize(() => {
          isRefreshingToken = false;
        })
      );
    })
  );
};
