package com.utp.formulas.controller;
import lombok.RequiredArgsConstructor;
import static com.utp.formulas.dto.ApiDtos.*;
import com.utp.formulas.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@PreAuthorize("hasAnyRole('ADMIN', 'QUIMICO', 'ATENCION')")
@RequiredArgsConstructor
public class PacienteController {
    private final PacienteService pacientes;

    @GetMapping
    
    public List<PacienteResponse> listar() {
        return pacientes.listar();
    }
    @GetMapping("/{id}")
    
    public PacienteResponse obtener(@PathVariable Long id) {
        return PacienteResponse.of(pacientes.obtenerActivo(id));
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENCION')")
    
    public ResponseEntity<PacienteResponse> crear(@Valid @RequestBody PacienteRequest req) {
        PacienteResponse p = pacientes.guardar(null, req);
        return ResponseEntity.created(URI.create("/api/pacientes/" + p.id())).body(p);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENCION')")
    
    public PacienteResponse editar(@PathVariable Long id, @Valid @RequestBody PacienteRequest req) {
        return pacientes.guardar(id, req);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATENCION')")
    
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pacientes.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
