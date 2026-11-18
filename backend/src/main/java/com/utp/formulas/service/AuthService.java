package com.utp.formulas.service;
import lombok.RequiredArgsConstructor;
import com.utp.formulas.model.Usuario;
import com.utp.formulas.repository.UsuarioRepository;
import static com.utp.formulas.dto.ApiDtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder passwords;
    private final JwtEncoder jwt;

    public SesionResponse login(LoginRequest req) {
        Usuario u = usuarios.findByUsername(req.username().trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos"));
        if (!passwords.matches(req.password(), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
        }
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("formulas-avance2").subject(u.getUsername())
            .issuedAt(ahora).expiresAt(ahora.plusSeconds(3600)).claim("rol", u.getRol()).build();
        String token = jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new SesionResponse(token, u.getUsername(), u.getRol(), 3600);
    }
    @Transactional
    
    public UsuarioResponse registrar(RegistroRequest req) {
        String nombre = req.username().toLowerCase(Locale.ROOT);
        if (usuarios.existsByUsername(nombre)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese usuario ya está registrado");
        }
        Usuario u = new Usuario();
        u.setUsername(nombre);
        u.setPassword(passwords.encode(req.password()));
        u.setRol("USUARIO");
        usuarios.saveAndFlush(u);
        return new UsuarioResponse(u.getUsername(), u.getRol());
    }
}
