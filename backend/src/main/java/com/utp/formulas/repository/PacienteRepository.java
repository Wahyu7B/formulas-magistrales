package com.utp.formulas.repository;
import com.utp.formulas.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    List<Paciente> findByActivoTrueOrderByApellidosAsc();
    boolean existsByDniAndIdNot(String dni, Long id);
    long countByActivoTrue();
}
