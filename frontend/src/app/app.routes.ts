 

import { Routes } from "@angular/router";
import { authGuard, pacientesGuard } from "./core/auth.guard";
import { LoginComponent } from "./pages/login.component";
import { DashboardComponent } from "./pages/dashboard.component";
import { PacientesComponent } from "./pages/pacientes.component";
import { FormulasComponent } from "./pages/formulas.component";
import { InsumosComponent } from "./pages/insumos.component";
import { MovimientosStockComponent } from "./pages/movimientos-stock.component";
import { AgendaComponent } from "./pages/agenda.component";
export const routes: Routes = [
  { path: "login", component: LoginComponent },
  {
    path: "dashboard",
    component: DashboardComponent,
    canActivate: [authGuard],
  },
  {
    path: "pacientes",
    component: PacientesComponent,
    canActivate: [pacientesGuard],
  },
  { path: "formulas", component: FormulasComponent, canActivate: [authGuard] },
  { path: "insumos", component: InsumosComponent, canActivate: [authGuard] },
  { path: "citas", component: AgendaComponent, canActivate: [pacientesGuard], data: { modulo: "citas" } },
  { path: "recetas", component: AgendaComponent, canActivate: [pacientesGuard], data: { modulo: "recetas" } },
  { path: "movimientos-stock", component: MovimientosStockComponent, canActivate: [authGuard] },
  { path: "", redirectTo: "dashboard", pathMatch: "full" },
  { path: "**", redirectTo: "dashboard" },
];
