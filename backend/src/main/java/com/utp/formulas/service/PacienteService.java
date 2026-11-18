package com.utp.formulas.service;
import lombok.RequiredArgsConstructor;
import com.utp.formulas.model.Paciente;
import com.utp.formulas.repository.*;
import static com.utp.formulas.dto.ApiDtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PacienteService {
    private final PacienteRepository pacientes;
    private final FormulaRepository formulas;

    public List<PacienteResponse> listar() {
        return pacientes.findByActivoTrueOrderByApellidosAsc().stream().map(PacienteResponse::of).toList();
    }
    
    public Paciente obtenerActivo(Long id) {
        return pacientes.findById(id).filter(Paciente::isActivo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado o inactivo"));
    }
    @Transactional
    
    public PacienteResponse guardar(Long id, PacienteRequest req) {
        Paciente p = id == null ? new Paciente() : obtenerActivo(id);
        if (pacientes.existsByDniAndIdNot(req.dni(), id == null ? -1L : id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un paciente con ese DNI");
        }
        p.setDni(req.dni());
        p.setNombres(req.nombres().trim());
        p.setApellidos(req.apellidos().trim());
        p.setTelefono(req.telefono());
        p.setDireccion(req.direccion().trim());
        return PacienteResponse.of(pacientes.saveAndFlush(p));
    }
    @Transactional
    
    public void eliminar(Long id) {
        Paciente p = obtenerActivo(id);
        if (formulas.existsByPacienteIdAndActivoTrue(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Primero da de baja las fórmulas activas de este paciente");
        }
        p.setActivo(false);
    }
}
