import {CanActivateFn, Router} from '@angular/router';
import {AuthService} from '../services/auth/auth.service';
import {inject} from '@angular/core';
import {catchError, map, of, switchMap, take} from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const publicRoutes = ['/connexion', '/inscription', '/mot-de-passe-oublie'];
  const isPublicRoute = publicRoutes.some(publicRoute =>
    state.url.startsWith(publicRoute)
  );


  if (isPublicRoute) {
    return true;
  }

  return authService.initialized$.pipe(
    take(1), // Prend la première émission et se désabonne
    switchMap(() => {
      // Si l'utilisateur est déjà chargé en mémoire
      if (authService.user) {
        return of(true);
      }

      // Sinon, vérifie le token via une requête API
      return authService.verifyAuth().pipe(
        map(user => {
          if (user) {
            // Accès autorisé
            return true;
          } else {
            // Redirige vers la page de connexion avec l'URL de retour
            return router.createUrlTree(['/connexion'], {
              queryParams: { returnUrl: state.url }
            });
          }
        }),
        catchError(() => {
          // En cas d'erreur, redirige aussi
          return of(router.createUrlTree(['/connexion'], {
            queryParams: { returnUrl: state.url }
          }));
        })
      );
    })
  );
}
