package com.utp.formulas.config;
import lombok.RequiredArgsConstructor;
import com.utp.formulas.model.Usuario;
import com.utp.formulas.repository.UsuarioRepository;
import com.utp.formulas.service.*;
import static com.utp.formulas.dto.ApiDtos.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.math.BigDecimal;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DatosIniciales implements CommandLineRunner {
    private final UsuarioRepository usuarios;
    private final com.utp.formulas.repository.PacienteRepository pacientesRepository;
    private final PasswordEncoder passwords;
    private final PacienteService pacientes;
    private final FormulaService formulas;

    @Override
    @Transactional
    public void run(String... args) {
        
        crearUsuario("admin", "ADMIN", "Admin2026!");
        
        crearUsuario("usuario", "USUARIO", "Usuario2026!");
        
        crearUsuario("quimico", "QUIMICO", "Quimico2026!");
        
        crearUsuario("atencion", "ATENCION", "Atencion2026!");
        if (pacientesRepository.count() == 0) {
            PacienteResponse p = pacientes.guardar(null, new PacienteRequest("99000001", "Ana", "Torres", "900000001", "Lima"));
            pacientes.guardar(null, new PacienteRequest("99000002", "Luis", "Rojas", "900000002", "Arequipa"));
            formulas.guardar(null, new FormulaRequest("Preparación tópica", "Envase de 100 g", "Presentación en envase de 100 g.", p.id(), List.of(
                new IngredienteRequest("Base vehicular", new BigDecimal("95"), "g"),
                new IngredienteRequest("Componente A", new BigDecimal("5"), "g"))));
        }
    }
    
    private void crearUsuario(String nombre, String rol, String password) {
        if (!usuarios.existsByUsername(nombre)) {
            Usuario u = new Usuario();
            u.setUsername(nombre);
            u.setRol(rol);
            u.setPassword(passwords.encode(password));
            usuarios.save(u);
        }
    }
}
