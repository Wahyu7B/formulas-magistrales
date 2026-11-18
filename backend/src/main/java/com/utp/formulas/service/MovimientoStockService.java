package com.utp.formulas.service;

import com.utp.formulas.model.Insumo;
import com.utp.formulas.model.MovimientoStock;
import com.utp.formulas.repository.InsumoRepository;
import com.utp.formulas.repository.MovimientoStockRepository;
import static com.utp.formulas.dto.ApiDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MovimientoStockService {
    private final InsumoRepository insumos;
    private final MovimientoStockRepository movimientos;

    public List<MovimientoStockResponse> listar() {
        return movimientos.findTop100ByOrderByCreadoEnDesc()
            .stream().map(MovimientoStockResponse::of).toList();
    }

    @Transactional
    public MovimientoStockResponse registrar(MovimientoStockRequest request) {
        Insumo insumo = insumos.findById(request.insumoId()).filter(Insumo::isActivo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insumo no encontrado"));

        BigDecimal nuevoStock = request.tipo().equals("INGRESO")
            ? insumo.getStock().add(request.cantidad())
            : insumo.getStock().subtract(request.cantidad());

        if (nuevoStock.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El egreso supera el stock disponible");
        }

        insumo.setStock(nuevoStock);
        insumos.save(insumo);

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setInsumo(insumo);
        movimiento.setTipo(request.tipo());
        movimiento.setCantidad(request.cantidad());
        movimiento.setMotivo(request.motivo().trim());
        movimiento.setStockResultante(nuevoStock);
        return MovimientoStockResponse.of(movimientos.save(movimiento));
    }
}
