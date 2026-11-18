package com.utp.formulas.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import com.utp.formulas.model.*;

public final class ApiDtos {
    private ApiDtos() {}
    public record LoginRequest(
        @NotBlank @Size(max = 40) String username,
        @NotBlank @Size(max = 72) String password
    ) {}
    public record RegistroRequest(
        @NotBlank @Pattern(regexp = "[a-zA-Z0-9_]{4,40}") String username,
        @NotBlank @Size(min = 8, max = 72) String password
    ) {}
    public record SesionResponse(String token, String username, String rol, long expiraEnSegundos) {}
    public record UsuarioResponse(String username, String rol) {}
    public record PacienteRequest(
        @NotBlank @Pattern(regexp = "[0-9]{8}") String dni,
        @NotBlank @Size(max = 100) String nombres,
        @NotBlank @Size(max = 100) String apellidos,
        @NotBlank @Pattern(regexp = "[0-9]{9}") String telefono,
        @NotBlank @Size(max = 250) String direccion
    ) {}
    public record PacienteResponse(Long id, String dni, String nombres, String apellidos, String telefono, String direccion, boolean activo) {
        public static PacienteResponse of(Paciente p) {
            return new PacienteResponse(p.getId(), p.getDni(), p.getNombres(), p.getApellidos(), p.getTelefono(), p.getDireccion(), p.isActivo());
        }
    }
    public record IngredienteRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 8, fraction = 4) BigDecimal cantidad,
        @NotBlank @Pattern(regexp = "g|ml|unidad") String unidad
    ) {}
    public record FormulaRequest(
        @NotBlank @Size(max = 120) String nombre,
        @NotBlank @Size(max = 100) String presentacion,
        @NotBlank @Size(max = 2000) String indicaciones,
        @NotNull @Positive Long pacienteId,
        @NotEmpty @Size(max = 30) List<@NotNull @Valid IngredienteRequest> ingredientes
    ) {}
    public record IngredienteResponse(Long id, String nombre, BigDecimal cantidad, String unidad) {}
    public record FormulaResponse(Long id, String nombre, String presentacion, String indicaciones, Long pacienteId, String pacienteNombre, boolean activo, List<IngredienteResponse> ingredientes) {
        public static FormulaResponse of(Formula f) {
            List<IngredienteResponse> items = f.getIngredientes().stream()
                .map(i -> new IngredienteResponse(i.getId(), i.getNombre(), i.getCantidad(), i.getUnidad())).toList();
            return new FormulaResponse(f.getId(), f.getNombre(), f.getPresentacion(), f.getIndicaciones(), f.getPaciente().getId(), f.getPaciente().getNombres() + " " + f.getPaciente().getApellidos(), f.isActivo(), items);
        }
    }
    public record InsumoRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotNull @DecimalMin("0.0") @Digits(integer = 8, fraction = 4) BigDecimal stock,
        @NotNull @DecimalMin("0.0") @Digits(integer = 8, fraction = 4) BigDecimal stockMinimo,
        @NotBlank @Pattern(regexp = "g|ml|unidad") String unidad
    ) {}
    public record InsumoResponse(Long id, String nombre, BigDecimal stock, BigDecimal stockMinimo, String unidad, boolean bajoStock) {
        public static InsumoResponse of(Insumo insumo) {
            return new InsumoResponse(insumo.getId(), insumo.getNombre(), insumo.getStock(), insumo.getStockMinimo(), insumo.getUnidad(), insumo.getStock().compareTo(insumo.getStockMinimo()) <= 0);
        }
    }
    public record MovimientoStockRequest(
        @NotNull @Positive Long insumoId,
        @NotBlank @Pattern(regexp = "INGRESO|EGRESO") String tipo,
        @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 8, fraction = 4) BigDecimal cantidad,
        @NotBlank @Size(max = 250) String motivo
    ) {}
    public record MovimientoStockResponse(Long id, Long insumoId, String insumo, String tipo, BigDecimal cantidad, String unidad, BigDecimal stockResultante, String motivo, java.time.Instant creadoEn) {
        public static MovimientoStockResponse of(MovimientoStock m) {
            return new MovimientoStockResponse(m.getId(), m.getInsumo().getId(), m.getInsumo().getNombre(), m.getTipo(), m.getCantidad(), m.getInsumo().getUnidad(), m.getStockResultante(), m.getMotivo(), m.getCreadoEn());
        }
    }
    public record DashboardResponse(long totalPacientes, long totalFormulas, long insumosBajoStock) {}
}
