package com.utp.formulas.controller;

import com.utp.formulas.service.AgendaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','QUIMICO','ATENCION')")
public class AgendaController {
    private final AgendaService agenda;

    public record NuevaCita(@NotNull @Positive Long pacienteId,
        @NotNull @Future LocalDateTime fecha, @NotBlank @Size(max = 200) String motivo) {}
    public record EstadoCita(@NotBlank @Pattern(regexp = "ATENDIDA|CANCELADA") String estado) {}
    public record NuevaReceta(@NotNull @Positive Long formulaId,
        @NotNull @PastOrPresent LocalDate fecha, @NotBlank @Size(max = 100) String referencia) {}

    @GetMapping("/citas")
    public List<AgendaService.Cita> citas() { return agenda.citas(); }

    @PostMapping("/citas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','ATENCION')")
    public void agendar(@Valid @RequestBody NuevaCita datos) {
        agenda.agendar(datos.pacienteId(), datos.fecha(), datos.motivo());
    }

    @PutMapping("/citas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','ATENCION')")
    public void editarCita(@PathVariable long id, @Valid @RequestBody NuevaCita datos) {
        agenda.editarCita(id, datos.pacienteId(), datos.fecha(), datos.motivo());
    }

    @PatchMapping("/citas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','ATENCION')")
    public void cerrarCita(@PathVariable long id, @Valid @RequestBody EstadoCita datos) {
        agenda.cerrarCita(id, datos.estado());
    }

    @GetMapping("/recetas")
    public List<AgendaService.Receta> recetas() { return agenda.recetas(); }

    @PostMapping("/recetas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','QUIMICO')")
    public void registrar(@Valid @RequestBody NuevaReceta datos) {
        agenda.registrarReceta(datos.formulaId(), datos.fecha(), datos.referencia());
    }

    @PutMapping("/recetas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','QUIMICO')")
    public void editarReceta(@PathVariable long id, @Valid @RequestBody NuevaReceta datos) {
        agenda.editarReceta(id, datos.formulaId(), datos.fecha(), datos.referencia());
    }

    @DeleteMapping("/recetas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','QUIMICO')")
    public void darDeBajaReceta(@PathVariable long id) {
        agenda.darDeBajaReceta(id);
    }
}
