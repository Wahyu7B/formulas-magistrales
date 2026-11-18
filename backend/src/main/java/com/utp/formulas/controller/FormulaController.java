package com.utp.formulas.controller;
import lombok.RequiredArgsConstructor;
import static com.utp.formulas.dto.ApiDtos.*;
import com.utp.formulas.service.FormulaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/formulas")
@PreAuthorize("hasAnyRole('ADMIN', 'QUIMICO', 'ATENCION', 'USUARIO')")
@RequiredArgsConstructor
public class FormulaController {
    private final FormulaService formulas;

    @GetMapping
    
    public List<FormulaResponse> listar(@RequestParam(defaultValue = "") String nombre, @RequestParam(required = false) Long pacienteId) {
        return formulas.listar(nombre, pacienteId);
    }
    @GetMapping("/{id}")
    
    public FormulaResponse obtener(@PathVariable Long id) {
        return formulas.obtener(id);
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUIMICO')")
    
    public ResponseEntity<FormulaResponse> crear(@Valid @RequestBody FormulaRequest req) {
        FormulaResponse f = formulas.guardar(null, req);
        return ResponseEntity.created(URI.create("/api/formulas/" + f.id())).body(f);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUIMICO')")
    
    public FormulaResponse editar(@PathVariable Long id, @Valid @RequestBody FormulaRequest req) {
        return formulas.guardar(id, req);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUIMICO')")
    
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        formulas.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
