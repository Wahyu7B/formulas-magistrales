 

import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { AuthService } from "./auth.service";
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.vigente() || inject(Router).createUrlTree(["/login"]);
};
export const pacientesGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  if (!auth.vigente()) return inject(Router).createUrlTree(["/login"]);
  return (
    auth.puedeVerPacientes() || inject(Router).createUrlTree(["/formulas"])
  );
};
