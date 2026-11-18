package com.utp.formulas.controller;
import lombok.RequiredArgsConstructor;
import com.utp.formulas.dto.ApiDtos.DashboardResponse;
import com.utp.formulas.repository.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

@RestController
@PreAuthorize("hasAnyRole('ADMIN', 'QUIMICO', 'ATENCION', 'USUARIO')")
@RequiredArgsConstructor
public class DashboardController {
    private final PacienteRepository pacientes;
    private final FormulaRepository formulas;
    private final InsumoRepository insumos;

    @GetMapping("/api/dashboard")
    @Transactional(readOnly = true)
    
    public DashboardResponse resumen() {
        return new DashboardResponse(pacientes.countByActivoTrue(), formulas.countByActivoTrue(), insumos.contarBajoStock());
    }
}
