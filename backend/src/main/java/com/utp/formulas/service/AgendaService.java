package com.utp.formulas.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgendaService {
    private final JdbcTemplate jdbc;

    public record Cita(long id, long pacienteId, String paciente, LocalDateTime fecha, String motivo, String estado) {}
    public record Receta(long id, long formulaId, String paciente, String formula, LocalDate fecha, String referencia) {}

    public List<Cita> citas() {
        return jdbc.query("""
            SELECT c.id, c.paciente_id, p.nombres || ' ' || p.apellidos AS paciente, c.fecha, c.motivo, c.estado
            FROM citas c JOIN pacientes p ON p.id = c.paciente_id
            ORDER BY c.fecha DESC, c.id DESC
            """, (rs, row) -> new Cita(rs.getLong("id"), rs.getLong("paciente_id"), rs.getString("paciente"),
                rs.getObject("fecha", LocalDateTime.class), rs.getString("motivo"), rs.getString("estado")));
    }

    public void agendar(long paciente, LocalDateTime fecha, String motivo) {
        int filas = jdbc.update("""
            INSERT INTO citas (paciente_id, fecha, motivo)
            SELECT id, ?, ? FROM pacientes WHERE id = ? AND activo
            """, fecha, motivo.trim(), paciente);
        if (filas == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente activo no encontrado");
    }

    public void cerrarCita(long id, String estado) {
        int filas = jdbc.update("UPDATE citas SET estado = ? WHERE id = ? AND estado = 'PROGRAMADA'", estado, id);
        if (filas == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "La cita ya no está programada");
    }

    public void editarCita(long id, long paciente, LocalDateTime fecha, String motivo) {
        int filas = jdbc.update("""
            UPDATE citas SET paciente_id = ?, fecha = ?, motivo = ?
            WHERE id = ? AND estado = 'PROGRAMADA'
              AND EXISTS (SELECT 1 FROM pacientes WHERE id = ? AND activo)
            """, paciente, fecha, motivo.trim(), id, paciente);
        if (filas == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "La cita o el paciente ya no están disponibles");
    }

    public List<Receta> recetas() {
        return jdbc.query("""
            SELECT r.id, r.formula_id, p.nombres || ' ' || p.apellidos AS paciente,
                   f.nombre AS formula, r.fecha, r.referencia
            FROM recetas r JOIN formulas f ON f.id = r.formula_id
            JOIN pacientes p ON p.id = f.paciente_id
            WHERE r.activo ORDER BY r.fecha DESC, r.id DESC
            """, (rs, row) -> new Receta(rs.getLong("id"), rs.getLong("formula_id"), rs.getString("paciente"),
                rs.getString("formula"), rs.getObject("fecha", LocalDate.class), rs.getString("referencia")));
    }

    public void registrarReceta(long formula, LocalDate fecha, String referencia) {
        int filas = jdbc.update("""
            INSERT INTO recetas (formula_id, fecha, referencia)
            SELECT f.id, ?, ? FROM formulas f JOIN pacientes p ON p.id = f.paciente_id
            WHERE f.id = ? AND f.activo AND p.activo
            """, fecha, referencia.trim(), formula);
        if (filas == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fórmula activa no encontrada");
    }

    public void editarReceta(long id, long formula, LocalDate fecha, String referencia) {
        int filas = jdbc.update("""
            UPDATE recetas SET formula_id = ?, fecha = ?, referencia = ?
            WHERE id = ? AND activo AND EXISTS (
                SELECT 1 FROM formulas f JOIN pacientes p ON p.id = f.paciente_id
                WHERE f.id = ? AND f.activo AND p.activo
            )
            """, formula, fecha, referencia.trim(), id, formula);
        if (filas == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "La receta o la fórmula ya no están disponibles");
    }

    public void darDeBajaReceta(long id) {
        int filas = jdbc.update("UPDATE recetas SET activo = FALSE WHERE id = ? AND activo", id);
        if (filas == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Receta activa no encontrada");
    }
}
