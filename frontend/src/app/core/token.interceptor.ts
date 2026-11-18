 

import { inject } from "@angular/core";
import { HttpInterceptorFn } from "@angular/common/http";
import { catchError, throwError } from "rxjs";
import { AuthService } from "./auth.service";
import { API } from "./api.service";
export const tokenInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const protegida =
    request.url.startsWith(API + "/") && !request.url.includes("/auth/");
  const token = auth.sesion()?.token;
  const enviada =
    protegida && token
      ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
      : request;
  return next(enviada).pipe(
    catchError((error) => {
      if (protegida && error.status === 401) auth.salir();
      return throwError(() => error);
    }),
  );
};
