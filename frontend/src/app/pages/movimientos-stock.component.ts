import { Component, OnInit, inject, signal } from "@angular/core";
import { DatePipe } from "@angular/common";
import { RouterLink } from "@angular/router";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { finalize } from "rxjs";
import { ApiService, mensajeError } from "../core/api.service";
import { AuthService } from "../core/auth.service";
import { Insumo, MovimientoStock } from "../core/models";

@Component({
  selector: "app-movimientos-stock",
  imports: [ReactiveFormsModule, DatePipe, RouterLink],
  templateUrl: "./movimientos-stock.component.html",
})
export class MovimientosStockComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);
  readonly auth = inject(AuthService);
  readonly insumos = signal<Insumo[]>([]);
  readonly movimientos = signal<MovimientoStock[]>([]);
  readonly cargando = signal(false);
  readonly error = signal("");
  readonly aviso = signal("");

  readonly form = this.fb.nonNullable.group({
    insumoId: [0, [Validators.required, Validators.min(1)]],
    tipo: ["INGRESO", [Validators.required]],
    cantidad: [1, [Validators.required, Validators.min(0.0001)]],
    motivo: ["", [Validators.required, Validators.maxLength(250), Validators.pattern(/\S/)]],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set("");
    this.api.movimientosStock().subscribe({
      next: (items) => this.movimientos.set(items),
      error: (e) => this.error.set(mensajeError(e)),
      complete: () => this.cargando.set(false),
    });
    this.api.insumos().subscribe({
      next: (items) => this.insumos.set(items),
      error: (e) => {
        this.error.set(mensajeError(e));
        this.cargando.set(false);
      },
      complete: () => this.cargando.set(false),
    });
  }

  registrar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.cargando()) return;
    const datos = this.form.getRawValue();
    this.cargando.set(true);
    this.api.registrarMovimiento({
      ...datos,
      insumoId: Number(datos.insumoId),
      tipo: datos.tipo as "INGRESO" | "EGRESO",
    }).pipe(finalize(() => this.cargando.set(false))).subscribe({
      next: () => {
        this.aviso.set("Movimiento registrado y stock actualizado.");
        this.form.controls.cantidad.setValue(1);
        this.form.controls.motivo.setValue("");
        this.cargar();
      },
      error: (e) => this.error.set(mensajeError(e)),
    });
  }
}
