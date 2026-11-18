import { IngredientesEditorComponent } from "./ingredientes-editor.component";
 

import { Component, inject, signal, OnInit } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { finalize } from "rxjs";
import { ApiService, mensajeError } from "../core/api.service";
import { AuthService } from "../core/auth.service";
import { Formula, Paciente, Ingrediente } from "../core/models";
@Component({
  selector: "app-formulas",
  imports: [ReactiveFormsModule, IngredientesEditorComponent],
  templateUrl: "./formulas.component.html",
})
export class FormulasComponent implements OnInit {
  private api = inject(ApiService);
  private fb = inject(FormBuilder);
  auth = inject(AuthService);
  lista = signal<Formula[]>([]);
  pacientes = signal<Paciente[]>([]);
  mostrando = signal(false);
  editando = signal<number | null>(null);
  detalle = signal<Formula | null>(null);
  baja = signal<Formula | null>(null);
  ocupado = signal(false);
  error = signal("");
  aviso = signal("");
  filtros = this.fb.nonNullable.group({ nombre: "", pacienteId: "" });
  form = this.fb.nonNullable.group({
    nombre: [
      "",
      [
        Validators.required,
        Validators.maxLength(120),
        Validators.pattern(/\S/),
      ],
    ],
    presentacion: [
      "",
      [
        Validators.required,
        Validators.maxLength(100),
        Validators.pattern(/\S/),
      ],
    ],
    indicaciones: [
      "",
      [
        Validators.required,
        Validators.maxLength(2000),
        Validators.pattern(/\S/),
      ],
    ],
    pacienteId: [0, [Validators.required, Validators.min(1)]],
    ingredientes: this.fb.array([this.nuevoIngrediente()]),
  });
  get ingredientes() {
    return this.form.controls.ingredientes;
  }
  
  nuevoIngrediente(item?: Ingrediente) {
    return this.fb.nonNullable.group({
      nombre: [
        item?.nombre ?? "",
        [
          Validators.required,
          Validators.maxLength(100),
          Validators.pattern(/\S/),
        ],
      ],
      cantidad: [
        item?.cantidad ?? 1,
        [
          Validators.required,
          Validators.min(0.0001),
          Validators.max(99999999),
          Validators.pattern(/^[0-9]+(\.[0-9]{1,4})?$/),
        ],
      ],
      unidad: [
        item?.unidad ?? "g",
        [Validators.required, Validators.pattern("g|ml|unidad")],
      ],
    });
  }
  
  ngOnInit() {
    this.cargar();
    if (this.auth.puedeVerPacientes())
      this.api
        .pacientes()
        .subscribe({
          next: (lista) => this.pacientes.set(lista),
          error: (e) => this.error.set(mensajeError(e)),
        });
  }
  
  cargar() {
    const { nombre, pacienteId } = this.filtros.getRawValue();
    this.api
      .formulas(nombre, pacienteId)
      .subscribe({
        next: (lista) => this.lista.set(lista),
        error: (e) => this.error.set(mensajeError(e)),
      });
  }
  
  limpiar() {
    this.filtros.reset();
    this.cargar();
  }
  
  abrir(f?: Formula) {
    this.error.set("");
    this.aviso.set("");
    this.editando.set(f?.id ?? null);
    this.form.reset({
      nombre: f?.nombre ?? "",
      presentacion: f?.presentacion ?? "",
      indicaciones: f?.indicaciones ?? "",
      pacienteId: f?.pacienteId ?? 0,
    });
    this.ingredientes.clear();
    if (f)
      f.ingredientes.forEach((item) =>
        this.ingredientes.push(this.nuevoIngrediente(item)),
      );
    else this.ingredientes.push(this.nuevoIngrediente());
    this.mostrando.set(true);
  }
  
  agregarIngrediente() {
    if (this.ingredientes.length < 30)
      this.ingredientes.push(this.nuevoIngrediente());
  }
  
  quitarIngrediente(index: number) {
    if (this.ingredientes.length > 1) this.ingredientes.removeAt(index);
  }
  
  guardar() {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.ocupado()) return;
    this.error.set("");
    this.ocupado.set(true);
    this.api
      .guardarFormula(this.editando(), this.form.getRawValue())
      .pipe(finalize(() => this.ocupado.set(false)))
      .subscribe({
        next: () => {
          this.mostrando.set(false);
          this.aviso.set("Fórmula e ingredientes guardados correctamente.");
          this.cargar();
        },
        error: (e) => this.error.set(mensajeError(e)),
      });
  }
  
  eliminar() {
    const f = this.baja();
    if (!f || this.ocupado()) return;
    this.ocupado.set(true);
    this.api
      .eliminarFormula(f.id)
      .pipe(finalize(() => this.ocupado.set(false)))
      .subscribe({
        next: () => {
          this.baja.set(null);
          this.aviso.set("Fórmula dada de baja.");
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
