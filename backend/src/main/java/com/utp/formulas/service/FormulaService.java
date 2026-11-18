package com.utp.formulas.service;
import lombok.RequiredArgsConstructor;
import com.utp.formulas.model.*;
import com.utp.formulas.repository.FormulaRepository;
import static com.utp.formulas.dto.ApiDtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FormulaService {
    private final FormulaRepository formulas;
    private final PacienteService pacientes;

    public List<FormulaResponse> listar(String nombre, Long pacienteId) {
        List<Formula> lista = pacienteId == null ? formulas.buscarPorNombre(nombre) : formulas.buscarPorPaciente(pacienteId, nombre);
        return lista.stream().map(FormulaResponse::of).toList();
    }
    
    public Formula obtenerActivo(Long id) {
        return formulas.findById(id).filter(Formula::isActivo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fórmula no encontrada o inactiva"));
    }
    
    public FormulaResponse obtener(Long id) {
        return FormulaResponse.of(obtenerActivo(id));
    }
    @Transactional
    
    public FormulaResponse guardar(Long id, FormulaRequest req) {
        Formula f = id == null ? new Formula() : obtenerActivo(id);
        f.setPaciente(pacientes.obtenerActivo(req.pacienteId()));
        f.setNombre(req.nombre().trim());
        f.setPresentacion(req.presentacion().trim());
        f.setIndicaciones(req.indicaciones().trim());
        f.getIngredientes().clear();
        formulas.saveAndFlush(f);
        for (IngredienteRequest item : req.ingredientes()) {
            if (item.cantidad() == null || item.cantidad().signum() <= 0) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Todo ingrediente debe tener una cantidad positiva");
            }
            Ingrediente ingrediente = new Ingrediente();
            ingrediente.setNombre(item.nombre().trim());
            ingrediente.setCantidad(item.cantidad());
            ingrediente.setUnidad(item.unidad());
            ingrediente.setFormula(f);
            f.getIngredientes().add(ingrediente);
        }
        return FormulaResponse.of(formulas.saveAndFlush(f));
    }
    @Transactional
    
    public void eliminar(Long id) {
        
        obtenerActivo(id).setActivo(false);
    }
}
