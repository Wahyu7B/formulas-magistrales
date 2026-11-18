 

import { Component, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { Router } from "@angular/router";
import { finalize } from "rxjs";
import { AuthService } from "../core/auth.service";
import { mensajeError } from "../core/api.service";
@Component({
  selector: "app-login",
  imports: [ReactiveFormsModule],
  templateUrl: "./login.component.html",
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  registro = signal(false);
  ocupado = signal(false);
  error = signal("");
  aviso = signal("");
  form = this.fb.nonNullable.group({
    username: [
      "",
      [Validators.required, Validators.pattern("[a-zA-Z0-9_]{4,40}")],
    ],
    password: [
      "",
      [Validators.required, Validators.minLength(8), Validators.maxLength(72)],
    ],
  });
  
  cambiarModo() {
    this.registro.update((valor) => !valor);
    this.error.set("");
    this.aviso.set("");
  }
  
  enviar() {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.ocupado()) return;
    this.ocupado.set(true);
    this.error.set("");
    const { username, password } = this.form.getRawValue();
    if (this.registro()) {
      this.auth
        .registrar(username, password)
        .pipe(finalize(() => this.ocupado.set(false)))
        .subscribe({
          next: () => {
            this.registro.set(false);
            this.aviso.set(
              "Cuenta de consulta creada. Ya puedes iniciar sesión.",
            );
          },
          error: (e) => this.error.set(mensajeError(e)),
        });
    } else {
      this.auth
        .login(username, password)
        .pipe(finalize(() => this.ocupado.set(false)))
        .subscribe({
          next: () => this.router.navigateByUrl("/dashboard"),
          error: (e) => this.error.set(mensajeError(e)),
        });
    }
  }
}
