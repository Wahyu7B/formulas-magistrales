package com.utp.formulas.repository;

import com.utp.formulas.model.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {
    List<Insumo> findByActivoTrueOrderByNombreAsc();
    boolean existsByNombreIgnoreCaseAndActivoTrue(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNotAndActivoTrue(String nombre, Long id);
    @Query("select count(i) from Insumo i where i.activo = true and i.stock <= i.stockMinimo")
    long contarBajoStock();
}
