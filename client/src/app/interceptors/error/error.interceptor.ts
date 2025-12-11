import {HttpErrorResponse, HttpInterceptorFn} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';
import {GlobalErrorService} from '../../services/global-error/global-error.service';

export const errorHandlerInterceptor: HttpInterceptorFn = (req, next) => {
  const errorGlobal: GlobalErrorService = inject(GlobalErrorService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      return throwError(() => errorGlobal.handleError(error));
    })
  );
};

