import { Component, inject, OnInit, signal } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { API, ApiService, mensajeError } from "../core/api.service";
import { AuthService } from "../core/auth.service";

interface Registro {
  id: number;
  paciente: string;
  pacienteId?: number;
  formula?: string;
  formulaId?: number;
  fecha: string;
  motivo?: string;
  referencia?: string;
  estado?: string;
}

@Component({
  selector: "app-agenda",
  imports: [FormsModule],
  templateUrl: "./agenda.component.html",
})
export class AgendaComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  readonly cita = inject(ActivatedRoute).snapshot.data["modulo"] === "citas";
  readonly titulo = this.cita ? "Citas" : "Recetas";
  private readonly url = API + (this.cita ? "/citas" : "/recetas");
  readonly lista = signal<Registro[]>([]);
  readonly opciones = signal<{ id: number; nombre: string }[]>([]);
  readonly error = signal("");
  readonly aviso = signal("");
  ocupado = false;
  id: number | null = null;
  relacionado = 0;
  fecha = "";
  detalle = "";

  get puedeEditar(): boolean {
    return this.cita ? this.auth.puedeGestionarPacientes() : this.auth.puedeGestionarFormulas();
  }

  ngOnInit(): void {
    this.cargar();
    if (!this.puedeEditar) return;
    if (this.cita) {
      this.api.pacientes().subscribe({
        next: items => this.opciones.set(items.map(p => ({ id: p.id, nombre: p.nombres + " " + p.apellidos }))),
        error: e => this.error.set(mensajeError(e)),
      });
    } else {
      this.api.formulas().subscribe({
        next: items => this.opciones.set(items.map(f => ({ id: f.id, nombre: f.nombre + " · " + f.pacienteNombre }))),
        error: e => this.error.set(mensajeError(e)),
      });
    }
  }

  cargar(): void {
    this.http.get<Registro[]>(this.url).subscribe({
      next: lista => this.lista.set(lista),
      error: e => this.error.set(mensajeError(e)),
    });
  }

  editar(item: Registro): void {
    this.id = item.id;
    this.relacionado = (this.cita ? item.pacienteId : item.formulaId) ?? 0;
    this.fecha = this.cita ? item.fecha.slice(0, 16) : item.fecha;
    this.detalle = (this.cita ? item.motivo : item.referencia) ?? "";
  }

  limpiar(): void {
    this.id = null;
    this.relacionado = 0;
    this.fecha = "";
    this.detalle = "";
  }

  guardar(): void {
    if (this.ocupado || !this.relacionado || !this.fecha || !this.detalle.trim()) return;
    const datos = this.cita
      ? { pacienteId: this.relacionado, fecha: this.fecha, motivo: this.detalle }
      : { formulaId: this.relacionado, fecha: this.fecha, referencia: this.detalle };
    const solicitud = this.id === null
      ? this.http.post(this.url, datos)
      : this.http.put(this.url + "/" + this.id, datos);
    this.ocupado = true;
    this.error.set("");
    this.aviso.set("");
    solicitud.subscribe({
      next: () => {
        this.ocupado = false;
        this.limpiar();
        this.aviso.set("Registro guardado.");
        this.cargar();
      },
      error: e => {
        this.ocupado = false;
        this.error.set(mensajeError(e));
      },
    });
  }

  quitar(item: Registro, estado: "ATENDIDA" | "CANCELADA" = "CANCELADA"): void {
    const pregunta = !this.cita
      ? "¿Dar de baja esta receta?"
      : estado === "ATENDIDA" ? "¿Marcar esta cita como atendida?" : "¿Cancelar esta cita?";
    if (this.ocupado || !confirm(pregunta)) return;
    const url = this.url + "/" + item.id;
    this.error.set("");
    const solicitud = this.cita
      ? this.http.patch(url, { estado })
      : this.http.delete(url);
    this.ocupado = true;
    solicitud.subscribe({
      next: () => {
        this.ocupado = false;
        this.limpiar();
        this.aviso.set(!this.cita ? "Receta dada de baja." : estado === "ATENDIDA" ? "Cita marcada como atendida." : "Cita cancelada.");
        this.cargar();
      },
      error: e => {
        this.ocupado = false;
        this.error.set(mensajeError(e));
      },
    });
  }
}
