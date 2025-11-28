import {CanActivateFn, Router} from '@angular/router';
import {AuthService} from '../services/auth/auth.service';
import {inject} from '@angular/core';
import {catchError, of, switchMap, take} from 'rxjs';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isPublicUrl = () => state.url === '/' || state.url.includes('/connexion');

  return authService.initialized$.pipe(
    take(1),
    switchMap(() => {
      return authService.user || isPublicUrl()
        ? of(true)
        : of(router.createUrlTree(['/connexion'], {
          queryParams: { returnUrl: state.url }
        }));
    }),
    catchError(() => of(router.createUrlTree(['/connexion'])))
  );

/*  return authService.initialized$.pipe(
    take(1),
    switchMap(() => {
      if (authService.user) {
        return of(true);
      }

      // Si l'utilisateur n'est pas authentifié
      if (isPublicUrl()) {
        return of(true);
      }

      // Si l'utilisateur vient de se reconnecter, on vérifie s'il y a une URL de retour
      const returnUrl = state.root.queryParams['returnUrl'] || state.url;

      // Redirige vers la page de connexion avec l'URL de retour
      return of(router.createUrlTree(['/connexion'], {
        queryParams: { returnUrl: returnUrl }
      }));
    }),
    catchError(() => {
      // En cas d'erreur, rediriger vers la page de connexion
      return of(router.createUrlTree(['/connexion']));
    })
  );*/
}



/*import {CanActivateFn, Router} from '@angular/router';
import {AuthService} from '../services/auth/auth.service';
import {inject} from '@angular/core';
import {catchError, map, of, switchMap, take} from 'rxjs'; // Ajoutez 'map' ici

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isPublicUrl = () => state.url === '/' || state.url.includes('/connexion');

  return authService.initialized$.pipe(
    take(1), // Attendre que l'AuthService ait fini d'initialiser (vérifier le token)
    switchMap(() => authService.user$), // Passer de l'état d'initialisation à l'état de l'utilisateur
    take(1), // Prendre la dernière valeur de l'utilisateur
    map(user => { // Utiliser l'opérateur 'map' pour décider de la redirection
      if (user) {
        // Si un utilisateur est authentifié, autoriser l'accès
        return true;
      }

      // Si l'utilisateur n'est pas authentifié
      if (isPublicUrl()) {
        // Autoriser l'accès aux URLs publiques même sans authentification
        return true;
      }

      // Si l'utilisateur n'est pas authentifié et l'URL n'est pas publique,
      // rediriger vers la page de connexion avec l'URL de retour
      const returnUrl = state.root.queryParams['returnUrl'] || state.url;
      return router.createUrlTree(['/connexion'], {
        queryParams: { returnUrl: returnUrl }
      });
    }),
    catchError(() => {
      // En cas d'erreur lors de l'initialisation ou de la récupération de l'utilisateur,
      // rediriger vers la page de connexion par sécurité
      return of(router.createUrlTree(['/connexion']));
    })
  );
};*/

/*export const authGuard: CanActivateFn = (route, state) => {
  const authService: AuthService = inject(AuthService);
  const router: Router = inject(Router);

  return authService.user$.pipe(
    take(1), // Prend la dernière valeur et se désabonne
    switchMap((initialized) => {
      if (!initialized) {
        // Attendre que l'authentification soit vérifiée
        return authService.initialized$.pipe(
          take(1),
          map(() => {
            const user = authService.user;
            if (user) {
              return true;
            } else {
              // Vérifier si l'utilisateur est sur la page d'accueil
              if (state.url === '/') {
                return true;
              }
              // Vérifier si l'utilisateur est en train d'essayer d'accéder à la page de connexion
              if (state.url.includes('/connexion')) {
                return true;
              }
              // Redirige vers la page de connexion avec l'URL de retour
              router.navigate(['/connexion'], { queryParams: { returnUrl: state.url } });
              return false;
            }
          })
        );
      } else {
        const user = authService.user;
        if (user) {
          return of(true);
        } else {
          // Vérifier si l'utilisateur est sur la page d'accueil
          if (state.url === '/') {
            return of(true);
          }
          // Vérifier si l'utilisateur est en train d'essayer d'accéder à la page de connexion
          if (state.url.includes('/connexion')) {
            return of(true);
          }
          // Redirige vers la page de connexion avec l'URL de retour
          router.navigate(['/connexion'], { queryParams: { returnUrl: state.url } });
          return of(false);
        }
      }
    })

  );
};*/



