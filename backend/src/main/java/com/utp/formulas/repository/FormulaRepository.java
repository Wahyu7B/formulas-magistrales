package com.utp.formulas.repository;
import com.utp.formulas.model.Formula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FormulaRepository extends JpaRepository<Formula, Long> {
    @Query("select f from Formula f where f.activo = true and lower(f.nombre) like lower(concat('%', :nombre, '%')) order by f.nombre")
    List<Formula> buscarPorNombre(@Param("nombre") String nombre);
    @Query("select f from Formula f join f.paciente p where f.activo = true and p.id = :pacienteId and lower(f.nombre) like lower(concat('%', :nombre, '%')) order by f.nombre")
    List<Formula> buscarPorPaciente(@Param("pacienteId") Long pacienteId, @Param("nombre") String nombre);
    boolean existsByPacienteIdAndActivoTrue(Long pacienteId);
    long countByActivoTrue();
}
