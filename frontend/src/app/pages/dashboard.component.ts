import { Component, inject, signal, OnInit } from "@angular/core";
import { DecimalPipe } from "@angular/common";
import { RouterLink } from "@angular/router";
import { finalize } from "rxjs";
import { ApiService, mensajeError } from "../core/api.service";
import { AuthService } from "../core/auth.service";
import { Dashboard, Formula } from "../core/models";

@Component({
  selector: "app-dashboard",
  imports: [RouterLink, DecimalPipe],
  templateUrl: "./dashboard.component.html",
  styleUrl: "./dashboard.component.scss",
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly auth = inject(AuthService);
  readonly datos = signal<Dashboard | null>(null);
  readonly formulasRecientes = signal<Formula[]>([]);
  readonly cargando = signal(false);
  readonly error = signal("");

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    if (this.cargando()) return;

    this.cargando.set(true);
    this.error.set("");

    this.api
      .dashboard()
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: (datos) => {
          this.datos.set(datos);
          this.cargarFormulasRecientes();
        },
        error: (error) => this.error.set(mensajeError(error)),
      });
  }

  private cargarFormulasRecientes(): void {
    this.api.formulas().subscribe({
      next: (formulas) =>
        this.formulasRecientes.set(
          [...formulas].sort((a, b) => b.id - a.id).slice(0, 5),
        ),
      error: (error) => this.error.set(mensajeError(error)),
    });
  }

  get formulasPorPaciente(): number {
    const pacientes = this.datos()?.totalPacientes ?? 0;
    return pacientes
      ? (this.datos()?.totalFormulas ?? 0) / pacientes
      : 0;
  }
}
