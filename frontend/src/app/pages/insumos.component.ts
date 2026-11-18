import { Component, OnInit, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { finalize } from "rxjs";
import { ApiService, mensajeError } from "../core/api.service";
import { AuthService } from "../core/auth.service";
import { Insumo } from "../core/models";

@Component({
  selector: "app-insumos",
  imports: [ReactiveFormsModule],
  templateUrl: "./insumos.component.html",
})
export class InsumosComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);
  readonly auth = inject(AuthService);
  readonly lista = signal<Insumo[]>([]);
  readonly editando = signal<number | null>(null);
  readonly mostrando = signal(false);
  readonly cargando = signal(false);
  readonly error = signal("");
  readonly aviso = signal("");

  readonly form = this.fb.nonNullable.group({
    nombre: ["", [Validators.required, Validators.maxLength(100), Validators.pattern(/\S/)]],
    stock: [0, [Validators.required, Validators.min(0), Validators.max(99999999)]],
    stockMinimo: [0, [Validators.required, Validators.min(0), Validators.max(99999999)]],
    unidad: ["g", [Validators.required, Validators.pattern("g|ml|unidad")]],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.api.insumos().pipe(finalize(() => this.cargando.set(false))).subscribe({
      next: (lista) => this.lista.set(lista),
      error: (e) => this.error.set(mensajeError(e)),
    });
  }

  abrir(insumo?: Insumo): void {
    this.error.set("");
    this.aviso.set("");
    this.editando.set(insumo?.id ?? null);
    this.form.reset({
      nombre: insumo?.nombre ?? "",
      stock: insumo?.stock ?? 0,
      stockMinimo: insumo?.stockMinimo ?? 0,
      unidad: insumo?.unidad ?? "g",
    });
    this.mostrando.set(true);
  }

  guardar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.cargando()) return;
    this.cargando.set(true);
    this.api.guardarInsumo(
      this.editando(),
      this.form.getRawValue() as Omit<Insumo, "id" | "bajoStock">,
    )
      .pipe(finalize(() => this.cargando.set(false)))
      .subscribe({
        next: () => {
          this.mostrando.set(false);
          this.aviso.set("Insumo guardado correctamente.");
          this.cargar();
        },
        error: (e) => this.error.set(mensajeError(e)),
      });
  }

  eliminar(insumo: Insumo): void {
    if (!confirm("¿Dar de baja el insumo " + insumo.nombre + "?")) return;
    this.api.eliminarInsumo(insumo.id).subscribe({
      next: () => {
        this.aviso.set("Insumo dado de baja.");
        this.cargar();
      },
      error: (e) => this.error.set(mensajeError(e)),
    });
  }
}
