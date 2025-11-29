import {inject, Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {HttpErrorResponse} from '@angular/common/http';

export interface GlobalError {
  status: number;
  message: string;
  details?: any;
}

@Injectable({
  providedIn: 'root'
})
export class GlobalErrorService {
  private errorSubject = new BehaviorSubject<GlobalError | null>(null);
  error$ = this.errorSubject.asObservable();

  setError(error: GlobalError | null) {
    this.errorSubject.next(error);
  }

  clearError() {
    this.setError(null);
  }

  handleError(error: HttpErrorResponse): void {
    const { status, error: errorDetails, url } = error;
    let message = 'Une erreur est survenue.';
    let details: any = null;

    // Personnalisation des messages en fonction du statut
    switch (status) {
      /*      case 401:
              message = 'Votre session a expiré. Veuillez vous reconnecter.';
              break;*/
      case 404:
        message = `La ressource demandée (${url?.split('/').pop()}) est introuvable.`;
        details = errorDetails;
        break;
      case 500:
        message = 'Une erreur serveur est survenue.';
        details = errorDetails?.message || 'Aucun détail disponible';
        break;
      default:
        message = error.message || 'Erreur inconnue';
    }

    // Déclencher l'erreur globale
    this.setError({
      status,
      message,
      details
    });

    throw error; // Re-lance l'erreur pour que les composants puissent aussi la gérer localement si besoin
  }
}
