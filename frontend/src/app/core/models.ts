 

export interface Sesion {
  token: string;
  username: string;
  rol: "ADMIN" | "QUIMICO" | "ATENCION" | "USUARIO";
  expiraEnSegundos: number;
}
export interface Paciente {
  id: number;
  dni: string;
  nombres: string;
  apellidos: string;
  telefono: string;
  direccion: string;
  activo: boolean;
}
export interface Ingrediente {
  id?: number;
  nombre: string;
  cantidad: number;
  unidad: string;
}
export interface Formula {
  id: number;
  nombre: string;
  presentacion: string;
  indicaciones: string;
  pacienteId: number;
  pacienteNombre: string;
  activo: boolean;
  ingredientes: Ingrediente[];
}
export interface FormulaRequest {
  nombre: string;
  presentacion: string;
  indicaciones: string;
  pacienteId: number;
  ingredientes: Ingrediente[];
}
export interface Dashboard {
  totalPacientes: number;
  totalFormulas: number;
  insumosBajoStock: number;
}
export interface Insumo {
  id: number;
  nombre: string;
  stock: number;
  stockMinimo: number;
  unidad: "g" | "ml" | "unidad";
  bajoStock: boolean;
}
export interface MovimientoStock {
  id: number;
  insumoId: number;
  insumo: string;
  tipo: "INGRESO" | "EGRESO";
  cantidad: number;
  unidad: string;
  stockResultante: number;
  motivo: string;
  creadoEn: string;
}
