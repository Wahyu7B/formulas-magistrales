 

import { Injectable, inject } from "@angular/core";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { Dashboard, Formula, FormulaRequest, Insumo, MovimientoStock, Paciente } from "./models";
export const API = "http://localhost:8082/api";
export function mensajeError(error: HttpErrorResponse): string {
  if (error.status === 0)
    return "No se pudo conectar con el servicio. Inténtalo nuevamente.";
  const campos = error.error?.campos;
  const detalle = campos
    ? Object.entries(campos)
        .map(([campo, mensaje]) => `${campo}: ${mensaje}`)
        .join(" · ")
    : "";
  return [error.error?.mensaje || "No se pudo completar la operación", detalle]
    .filter(Boolean)
    .join(". ");
}
@Injectable({ providedIn: "root" })
export class ApiService {
  private http = inject(HttpClient);
  dashboard() {
    return this.http.get<Dashboard>(`${API}/dashboard`);
  }
  insumos() {
    return this.http.get<Insumo[]>(`${API}/insumos`);
  }
  guardarInsumo(id: number | null, datos: Omit<Insumo, "id" | "bajoStock">) {
    return id === null
      ? this.http.post<Insumo>(`${API}/insumos`, datos)
      : this.http.put<Insumo>(`${API}/insumos/${id}`, datos);
  }
  eliminarInsumo(id: number) {
    return this.http.delete<void>(`${API}/insumos/${id}`);
  }
  movimientosStock() {
    return this.http.get<MovimientoStock[]>(`${API}/movimientos-stock`);
  }
  registrarMovimiento(datos: { insumoId: number; tipo: "INGRESO" | "EGRESO"; cantidad: number; motivo: string }) {
    return this.http.post<MovimientoStock>(`${API}/movimientos-stock`, datos);
  }
  pacientes() {
    return this.http.get<Paciente[]>(`${API}/pacientes`);
  }
  guardarPaciente(id: number | null, datos: Omit<Paciente, "id" | "activo">) {
    return id === null
      ? this.http.post<Paciente>(`${API}/pacientes`, datos)
      : this.http.put<Paciente>(`${API}/pacientes/${id}`, datos);
  }
  eliminarPaciente(id: number) {
    return this.http.delete<void>(`${API}/pacientes/${id}`);
  }
  formulas(nombre = "", pacienteId = "") {
    const params: Record<string, string> = { nombre };
    if (pacienteId) params["pacienteId"] = pacienteId;
    return this.http.get<Formula[]>(`${API}/formulas`, { params });
  }
  guardarFormula(id: number | null, datos: FormulaRequest) {
    return id === null
      ? this.http.post<Formula>(`${API}/formulas`, datos)
      : this.http.put<Formula>(`${API}/formulas/${id}`, datos);
  }
  eliminarFormula(id: number) {
    return this.http.delete<void>(`${API}/formulas/${id}`);
  }
}
