// Intercepta llamadas HTTP para inyectar Authorization: Bearer y renueva automáticamente el token ante errores 401
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { catchError, switchMap, throwError } from 'rxjs';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // No inyectar Authorization a servicios externos de Google Identity Toolkit
  if (req.url.includes('googleapis.com')) {
    return next(req);
  }

  let authReq = req;
  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Si el backend devuelve 401 Unauthorized y tenemos Refresh Token, lo renovamos en caliente
      if (error.status === 401 && authService.hasRefreshToken()) {
        return authService.refreshToken().pipe(
          switchMap((newToken: string) => {
            const retriedReq = req.clone({
              setHeaders: {
                Authorization: `Bearer ${newToken}`
              }
            });
            return next(retriedReq);
          }),
          catchError((refreshErr) => {
            authService.logout();
            return throwError(() => refreshErr);
          })
        );
      }
      return throwError(() => error);
    })
  );
};