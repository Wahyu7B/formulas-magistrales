package com.utp.formulas.controller;

import com.utp.formulas.service.MovimientoStockService;
import static com.utp.formulas.dto.ApiDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos-stock")
@RequiredArgsConstructor
public class MovimientoStockController {
    private final MovimientoStockService movimientos;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUIMICO', 'ATENCION', 'USUARIO')")
    public List<MovimientoStockResponse> listar() {
        return movimientos.listar();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUIMICO')")
    public MovimientoStockResponse registrar(@Valid @RequestBody MovimientoStockRequest request) {
        return movimientos.registrar(request);
    }
}
