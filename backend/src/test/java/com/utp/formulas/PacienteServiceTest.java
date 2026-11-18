package com.utp.formulas;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.utp.formulas.model.Paciente;
import com.utp.formulas.repository.*;
import com.utp.formulas.service.PacienteService;
import com.utp.formulas.dto.ApiDtos.PacienteRequest;
import org.junit.jupiter.api.Test;
import java.util.Optional;

class PacienteServiceTest {
    private final PacienteRepository repositorio = mock(PacienteRepository.class);
    private final FormulaRepository formulas = mock(FormulaRepository.class);
    private final PacienteService servicio = new PacienteService(repositorio, formulas);
    private PacienteRequest datos() {
        return new PacienteRequest("99000201", "  Ana Demo  ", "  Torres  ", "900000001", "  Dirección ficticia  ");
    }
    @Test
    void noGuardaUnDniDuplicado() {
        when(repositorio.existsByDniAndIdNot("99000201", -1L)).thenReturn(true);
        assertThatThrownBy(() -> servicio.guardar(null, datos())).hasMessageContaining("ese DNI");
        verify(repositorio, never()).saveAndFlush(any(Paciente.class));
    }
    @Test
    void rechazaPacienteInactivo() {
        Paciente p = new Paciente();
        p.setActivo(false);
        when(repositorio.findById(1L)).thenReturn(Optional.of(p));
        assertThatThrownBy(() -> servicio.obtenerActivo(1L)).hasMessageContaining("inactivo");
    }
    @Test
    void normalizaEspaciosAntesDeGuardar() {
        when(repositorio.saveAndFlush(any(Paciente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        var respuesta = servicio.guardar(null, datos());
        assertThat(respuesta.nombres()).isEqualTo("Ana Demo");
        assertThat(respuesta.apellidos()).isEqualTo("Torres");
        assertThat(respuesta.direccion()).isEqualTo("Dirección ficticia");
    }
}
