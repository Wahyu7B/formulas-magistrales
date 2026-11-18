package com.utp.formulas.controller;

import com.utp.formulas.service.InsumoService;
import static com.utp.formulas.dto.ApiDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/insumos")
@RequiredArgsConstructor
public class InsumoController {
    private final InsumoService insumos;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUIMICO', 'ATENCION', 'USUARIO')")
    public List<InsumoResponse> listar() {
        return insumos.listar();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUIMICO')")
    public ResponseEntity<InsumoResponse> crear(@Valid @RequestBody InsumoRequest request) {
        InsumoResponse insumo = insumos.guardar(null, request);
        return ResponseEntity.created(URI.create("/api/insumos/" + insumo.id())).body(insumo);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUIMICO')")
    public InsumoResponse editar(@PathVariable Long id, @Valid @RequestBody InsumoRequest request) {
        return insumos.guardar(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUIMICO')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        insumos.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
