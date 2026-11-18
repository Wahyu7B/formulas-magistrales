package com.utp.formulas.repository;

import com.utp.formulas.model.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findTop100ByOrderByCreadoEnDesc();
}
