import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import { inject } from '@angular/core';
import { ErrorHandlerService } from '../../services/error/error-handler.service';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

export const errorHandlerInterceptor: HttpInterceptorFn = (req, next) => {
  const errorHandler = inject(ErrorHandlerService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // Laisser l'ErrorHandlerService gérer l'erreur
      return throwError(() => errorHandler.handleError(error));
    })
  );
};
