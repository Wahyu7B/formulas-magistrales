package com.utp.formulas.service;

import com.utp.formulas.model.Insumo;
import com.utp.formulas.repository.InsumoRepository;
import static com.utp.formulas.dto.ApiDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InsumoService {
    private final InsumoRepository insumos;

    public List<InsumoResponse> listar() {
        return insumos.findByActivoTrueOrderByNombreAsc()
            .stream().map(InsumoResponse::of).toList();
    }

    @Transactional
    public InsumoResponse guardar(Long id, InsumoRequest request) {
        String nombre = request.nombre().trim();
        Insumo insumo = id == null
            ? new Insumo()
            : insumos.findById(id).filter(Insumo::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insumo no encontrado"));

        boolean duplicado = id == null
            ? insumos.existsByNombreIgnoreCaseAndActivoTrue(nombre)
            : insumos.existsByNombreIgnoreCaseAndIdNotAndActivoTrue(nombre, id);
        if (duplicado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un insumo activo con ese nombre");
        }

        insumo.setNombre(nombre);
        insumo.setStock(request.stock());
        insumo.setStockMinimo(request.stockMinimo());
        insumo.setUnidad(request.unidad());
        return InsumoResponse.of(insumos.saveAndFlush(insumo));
    }

    @Transactional
    public void eliminar(Long id) {
        Insumo insumo = insumos.findById(id).filter(Insumo::isActivo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Insumo no encontrado"));
        insumo.setActivo(false);
    }
}
