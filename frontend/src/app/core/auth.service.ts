 

import { Injectable, inject, signal, computed } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Router } from "@angular/router";
import { tap } from "rxjs";
import { API } from "./api.service";
import { Sesion } from "./models";
@Injectable({ providedIn: "root" })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);
  sesion = signal<Sesion | null>(this.recuperar());
  esAdmin = computed(() => this.sesion()?.rol === "ADMIN");
  puedeVerPacientes = computed(() =>
    ["ADMIN", "QUIMICO", "ATENCION"].includes(this.sesion()?.rol ?? ""),
  );
  puedeGestionarPacientes = computed(() =>
    ["ADMIN", "ATENCION"].includes(this.sesion()?.rol ?? ""),
  );
  puedeGestionarFormulas = computed(() =>
    ["ADMIN", "QUIMICO"].includes(this.sesion()?.rol ?? ""),
  );
  puedeGestionarInsumos = computed(() =>
    ["ADMIN", "QUIMICO"].includes(this.sesion()?.rol ?? ""),
  );
  
  private recuperar(): Sesion | null {
    try {
      const guardada = JSON.parse(sessionStorage.getItem("sesion") || "null");
      const vence = Number(sessionStorage.getItem("vence"));
      return guardada?.token && vence > Date.now() ? guardada : null;
    } catch {
      return null;
    }
  }
  
  vigente(): boolean {
    return (
      this.sesion() !== null &&
      Number(sessionStorage.getItem("vence")) > Date.now()
    );
  }
  
  login(username: string, password: string) {
    return this.http
      .post<Sesion>(`${API}/auth/login`, { username, password })
      .pipe(
        tap((sesion) => {
          sessionStorage.setItem("sesion", JSON.stringify(sesion));
          sessionStorage.setItem(
            "vence",
            String(Date.now() + sesion.expiraEnSegundos * 1000),
          );
          this.sesion.set(sesion);
        }),
      );
  }
  
  registrar(username: string, password: string) {
    return this.http.post(`${API}/auth/registro`, { username, password });
  }
  
  salir() {
    sessionStorage.removeItem("sesion");
    sessionStorage.removeItem("vence");
    this.sesion.set(null);
    this.router.navigateByUrl("/login");
  }
}
