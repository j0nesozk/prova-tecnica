import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const detail = error.error?.detail ?? error.error?.title ?? error.message ?? 'Erro inesperado';
      const message = `${error.status ? error.status + ' — ' : ''}${detail}`;
      snackBar.open(message, 'Fechar', {
        duration: 5000,
        horizontalPosition: 'right',
        verticalPosition: 'top',
        panelClass: ['error-snackbar']
      });
      return throwError(() => error);
    })
  );
};
