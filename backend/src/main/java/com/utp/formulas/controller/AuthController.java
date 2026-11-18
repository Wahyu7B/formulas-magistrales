package com.utp.formulas.controller;
import lombok.RequiredArgsConstructor;
import static com.utp.formulas.dto.ApiDtos.*;
import com.utp.formulas.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService auth;

    @PostMapping("/login")
    
    public SesionResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }
    @PostMapping("/registro")
    
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest req) {
        return ResponseEntity.status(201).body(auth.registrar(req));
    }
}
