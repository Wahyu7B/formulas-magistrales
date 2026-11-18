 

import { Component, inject, signal, OnInit, computed } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { finalize } from "rxjs";
import { ApiService, mensajeError } from "../core/api.service";
import { Paciente } from "../core/models";
import { AuthService } from "../core/auth.service";
@Component({
  selector: "app-pacientes",
  imports: [ReactiveFormsModule],
  templateUrl: "./pacientes.component.html",
})
export class PacientesComponent implements OnInit {
  private api = inject(ApiService);
  private fb = inject(FormBuilder);
  auth = inject(AuthService);
  lista = signal<Paciente[]>([]);
  busqueda = signal("");
  filtrados = computed(() =>
    this.lista().filter((p) =>
      `${p.dni} ${p.nombres} ${p.apellidos}`
        .toLowerCase()
        .includes(this.busqueda().toLowerCase()),
    ),
  );
  mostrando = signal(false);
  editando = signal<number | null>(null);
  ocupado = signal(false);
  error = signal("");
  aviso = signal("");
  baja = signal<Paciente | null>(null);
  form = this.fb.nonNullable.group({
    dni: ["", [Validators.required, Validators.pattern("[0-9]{8}")]],
    nombres: [
      "",
      [
        Validators.required,
        Validators.maxLength(100),
        Validators.pattern(/\S/),
      ],
    ],
    apellidos: [
      "",
      [
        Validators.required,
        Validators.maxLength(100),
        Validators.pattern(/\S/),
      ],
    ],
    telefono: ["", [Validators.required, Validators.pattern("[0-9]{9}")]],
    direccion: [
      "",
      [
        Validators.required,
        Validators.maxLength(250),
        Validators.pattern(/\S/),
      ],
    ],
  });
  
  ngOnInit() {
    this.cargar();
  }
  
  cargar() {
    this.api
      .pacientes()
      .subscribe({
        next: (lista) => this.lista.set(lista),
        error: (e) => this.error.set(mensajeError(e)),
      });
  }
  
  abrir(p?: Paciente) {
    this.error.set("");
    this.aviso.set("");
    this.editando.set(p?.id ?? null);
    this.form.reset(
      p
        ? {
            dni: p.dni,
            nombres: p.nombres,
            apellidos: p.apellidos,
            telefono: p.telefono,
            direccion: p.direccion,
          }
        : { dni: "", nombres: "", apellidos: "", telefono: "", direccion: "" },
    );
    this.mostrando.set(true);
  }
  
  guardar() {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.ocupado()) return;
    this.ocupado.set(true);
    this.error.set("");
    this.api
      .guardarPaciente(this.editando(), this.form.getRawValue())
      .pipe(finalize(() => this.ocupado.set(false)))
      .subscribe({
        next: () => {
          this.mostrando.set(false);
          this.aviso.set("Paciente guardado correctamente.");
          this.cargar();
        },
        error: (e) => this.error.set(mensajeError(e)),
      });
  }
  
  eliminar() {
    const p = this.baja();
    if (!p || this.ocupado()) return;
    this.ocupado.set(true);
    this.api
      .eliminarPaciente(p.id)
      .pipe(finalize(() => this.ocupado.set(false)))
      .subscribe({
        next: () => {
          this.baja.set(null);
          this.aviso.set("Paciente dado de baja.");
          this.error.set("");
          this.cargar();
        },
        error: (e) => {
          this.baja.set(null);
          this.error.set(mensajeError(e));
        },
      });
  }
}
