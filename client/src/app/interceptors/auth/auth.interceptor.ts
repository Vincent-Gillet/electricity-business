import { HttpContextToken, HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../../services/auth/auth.service';
import { BehaviorSubject, catchError, filter, finalize, switchMap, take, throwError } from 'rxjs';
import { Router } from '@angular/router';
import {GlobalErrorService} from '../../services/global-error/global-error.service';

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
  const errorGlobal = inject(GlobalErrorService);

  // Cloner la requête avec le token d'autorisation
  const authReq = accessToken
    ? req.clone({
      setHeaders: { Authorization: `Bearer ${accessToken}` },
    })
    : req;

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {

      if (error.status === 401) {
        if (isRefreshingToken) {
          return refreshTokenSubject.pipe(
            filter(token => token !== null),
            take(1),
            switchMap((newToken) => next(req.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } })))
          );
        }

        isRefreshingToken = true;
        refreshTokenSubject.next(null);

        return authService.refreshToken().pipe(
          switchMap((response) => {
            const newToken = response.accessToken;
            authService.saveAccessToken(newToken);
            refreshTokenSubject.next(newToken);
            return next(req.clone({ setHeaders: { Authorization: `Bearer ${newToken}` } }));
          }),
          catchError((refreshError) => {
            // Si le REFRESH échoue, là on traite l'erreur et on déconnecte
            isRefreshingToken = false;
            authService.logout();
            router.navigate(['/connexion']);
            // Optionnel: ne pas afficher de popup globale pour la déconnexion
            return throwError(() => refreshError);
          }),
          finalize(() => isRefreshingToken = false)
        );
      }

      const errorHandled = errorGlobal.handleError(error);
      return throwError(() => errorHandled);
    })
  );
};
